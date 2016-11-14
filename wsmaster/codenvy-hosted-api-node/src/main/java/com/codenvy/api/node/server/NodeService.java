/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.api.node.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.api.node.shared.dto.AddNodeDto;
import com.google.common.base.MoreObjects;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Defines Node REST API (managing node to register)
 *
 * @author Florent Benoit
 * @author Alexander Garagatyi
 */
@Api(value = "/nodes")
@Path("/nodes")
public class NodeService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeService.class);

    @GET
    @Path("/script")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get script for adding a Swarm Node",
                  responseContainer = "sh")
    @ApiResponses({@ApiResponse(code = 200, message = "The script is provided"),
                   @ApiResponse(code = 500, message = "Internal server error occurred during domains fetching")})
    public Response getAddNodeScript(@Context HttpServletRequest req) throws ServerException {

        java.nio.file.Path path = Paths.get("/opt/codenvy-data/conf/add-node.sh");
        if (!Files.exists(path)) {
            throw new ServerException("No script found.");
        }

        // get Server name
        String requestedServerName = req.getServerName();

        String content;
        try {
             content = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new ServerException("Unable to grab script content", e);
        }


        // update content with requested IP
        content = content.replace("###MASTERHOST###", requestedServerName);

        Response.ResponseBuilder response = Response.ok(content);
        response.type("text/plain");
        response.header("Content-Disposition", "attachment; filename=\"add-node.sh\"");
        return response.build();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @ApiOperation("Add a node to swarm")
    @ApiResponses({@ApiResponse(code = 200, message = "The node successfully added"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 500, message = "Internal server error occurred during permissions storing")})
    public String registerNode(@Context HttpServletRequest req,
                               @ApiParam(value = "The permissions to store", required = true)
                               AddNodeDto addNodeDto) throws ServerException,
                                                             BadRequestException {
        String ip;
        int port;
        if (addNodeDto != null) {
            ip = MoreObjects.firstNonNull(addNodeDto.getIp(), req.getRemoteAddr());
            port = MoreObjects.firstNonNull(addNodeDto.getPort(), 2375);
        } else {
            ip = req.getRemoteAddr();
            port = 2375;
        }

        if (ip == null) {
            throw new ServerException("IP address not provided so unable to guess it as well");
        }

        LOGGER.info("Request to add a node with IP " + ip + ":" + port);

        checkSwarmConnection(ip, port);

        // add the node on the puppet file
        java.nio.file.Path path = Paths.get("/puppet-configuration/codenvy.env");
        if (!Files.exists(path)) {
            throw new ServerException("No puppet data found.");
        }

        // update the property named  $machine_extra_hosts
        StringBuilder sb = new StringBuilder();
        try (Stream<String> lines = Files.lines(path)) {
            lines.map(s -> {
                if (s.startsWith("CODENVY_SWARM_NODES=")) {
                    return s + "," + ip + ":" + port;
                }
                return s;
            })
                 .forEach(s -> sb.append(s).append("\n"));
        } catch (IOException ex) {
            throw new ServerException("Unable to update property", ex);
        }

        // override the file
        try {
            Files.write(path, sb.toString().getBytes());
        } catch (IOException e) {
            throw new ServerException("Unable to add host", e);
        }

        // TODO get from property
        // Launch the puppet agent
        ProcessBuilder processBuilder = new ProcessBuilder().command(
                "docker", "run", "--rm",
                "-e", "CODENVY_IP=" + System.getenv("CODENVY_IP"),
                "-e", "PUPPET_SOURCE=" +  System.getenv("PUPPET_SOURCE"),
                "-e", "PUPPET_DESTINATION=" + System.getenv("PUPPET_DESTINATION"),
                "-v", System.getenv("PUPPET_DESTINATION") + ":/opt/codenvy:rw",
                "-v", System.getenv("PUPPET_SOURCE") + "/manifests:/etc/puppet/manifests:ro",
                "-v", System.getenv("PUPPET_SOURCE") + "/modules:/etc/puppet/modules:ro",
                "-t", "puppet/puppet-agent-alpine",
                "apply", "--modulepath", "/etc/puppet/modules/", "/etc/puppet/manifests/codenvy.pp");

        Process process;
        try {
            process = processBuilder.inheritIO().start();
        } catch (IOException e) {
            throw new ServerException("Unable to update Puppet configuration", e);
        }

        int processDockerErrorCode;
        try {
            processDockerErrorCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new ServerException("Error when updating Puppet configuration", e);
        }

        if (processDockerErrorCode != 0) {
            throw new ServerException("Failure in updating puppet configuration");
        }

        return "Swarm node '" + ip + ":" + port +"' has been added to configuration. It will take some time for refresh";
    }

    protected void checkSwarmConnection(String ip, int port) throws ServerException {

        // try to connect
        String url = "http://" + ip + ":" + port + "/info";

        URL obj;
        try {
            obj = new URL(url);
        } catch (MalformedURLException e) {
            throw new ServerException("Unable to validate given IP when using URL '" + url + "'", e);
        }

        HttpURLConnection con;
        try {
            con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5));
            con.setRequestMethod("GET");
            con.connect();
        } catch (IOException e) {
            throw new ServerException("Unable to connect using URL '" + url + "'", e);
        }

        int responseCode;
        try {
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            throw new ServerException("Unable to get response code", e);
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new ServerException("The given IP:PORT '" + ip + ":" + port + " is invalid as it provides response code '" + responseCode + "'.");
        }
    }
}
