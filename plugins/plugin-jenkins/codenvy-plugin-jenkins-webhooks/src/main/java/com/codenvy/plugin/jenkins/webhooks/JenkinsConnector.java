/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.plugin.jenkins.webhooks;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.inject.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.ws.rs.core.HttpHeaders;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.xml.transform.TransformerFactory.newInstance;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

/**
 * Client for Jenkins API.
 * One {@link JenkinsConnector} is configured for one Jenkins job.
 *
 * @author Stephane Tournie
 * @author Igor Vinokur
 */
public class JenkinsConnector {

    private static final Logger LOG = LoggerFactory.getLogger(JenkinsConnector.class);

    private static final String JENKINS_CONNECTOR_PREFIX_PATTERN    = "env.CODENVY_JENKINS_CONNECTOR_.+";
    private static final String JENKINS_CONNECTOR_URL_SUFFIX        = "_URL";
    private static final String JENKINS_CONNECTOR_JOB_NAME_SUFFIX   = "_JOB_NAME";
    private static final String JENKINS_CONNECTOR_FACTORY_ID_SUFFIX = "_FACTORY_ID";
    private static final String PROPERTY_NOT_FOUND_ERROR_MESSAGE    = "No connector properties were found for url %s and job %s";
    private static final String FACTORY_LINK_DISPLAYED_MESSAGE      = "factory link %s already displayed in description of Jenkins job %s";

    @VisibleForTesting
    String url;

    private final String                  jobName;
    private final ConfigurationProperties configurationProperties;

    @Inject
    public JenkinsConnector(@Assisted("url") String url,
                            @Assisted("jobName") String jobName,
                            ConfigurationProperties configurationProperties) throws ServerException {
        this.url = url;
        this.jobName = jobName;
        this.configurationProperties = configurationProperties;
    }

    /**
     * Returns {@link JenkinsConnector} object with updated url from connector url property that contains credentials inside.
     */
    public JenkinsConnector updateUrlWithCredentials() throws ServerException {
        Optional<String> propertyPrefixOptional = getJenkinsConnectorPropertyPrefix();
        if (propertyPrefixOptional.isPresent()) {
            Map<String, String> properties = configurationProperties.getProperties(JENKINS_CONNECTOR_PREFIX_PATTERN);
            this.url = properties.get(propertyPrefixOptional.get() + JENKINS_CONNECTOR_URL_SUFFIX);
        } else {
            throw new ServerException(String.format(PROPERTY_NOT_FOUND_ERROR_MESSAGE, url, jobName));
        }
        return this;
    }

    /**
     * Update Jenkins job description with head factory link.
     *
     * @param factoryUrl
     *         head factory url that will be set as a url for link in jenkins job description
     * @throws IOException
     *         if any i/o error occurs.
     * @throws ServerException
     *         if any other error occurs.
     */
    public void addHeadFactoryLink(String factoryUrl) throws IOException, ServerException {
        Document configDocument = xmlToDocument(getCurrentJenkinsJobConfiguration());
        Node descriptionNode = configDocument.getDocumentElement().getElementsByTagName("description").item(0);
        String content = descriptionNode.getTextContent();
        if (!content.contains(factoryUrl)) {
            descriptionNode.setTextContent("Dev Workspace (HEAD): <a href=\"" + factoryUrl + "\">" + factoryUrl + "</a>");
            updateJenkinsJobDescription(factoryUrl, configDocument);
        } else {
            LOG.debug(String.format(FACTORY_LINK_DISPLAYED_MESSAGE, factoryUrl, jobName));
        }
    }

    /**
     * Update Jenkins job description with build failed factory link.
     *
     * @param factoryUrl
     *         build failed factory url that will be set as a url for link in jenkins job description
     * @throws IOException
     *         if any i/o error occurs.
     * @throws ServerException
     *         if any other error occurs.
     */
    public void addFailedBuildFactoryLink(String factoryUrl) throws IOException, ServerException {
        Document configDocument = xmlToDocument(getCurrentJenkinsJobConfiguration());
        Node descriptionNode = configDocument.getDocumentElement().getElementsByTagName("description").item(0);
        String content = descriptionNode.getTextContent();
        if (!content.contains(factoryUrl)) {
            String startPoint = "<br>Dev Workspace (Failed Build): <a href=\"";
            content = content.contains(startPoint) ? content.substring(0, content.indexOf(startPoint)) : content;
            descriptionNode.setTextContent(content + startPoint + factoryUrl + "\">" + factoryUrl + "</a>");
            updateJenkinsJobDescription(factoryUrl, configDocument);
        } else {
            LOG.debug(String.format(FACTORY_LINK_DISPLAYED_MESSAGE, factoryUrl, jobName));
        }
    }

    /**
     * Returns build's latest commit id.
     *
     * @param buildId
     *         id of the build
     * @throws IOException
     *         if any i/o error occurs.
     * @throws ServerException
     *         if any other error occurs.
     */
    public String getCommitId(int buildId) throws IOException, ServerException {
        String requestUrl = url + "/job/" + jobName + "/" + buildId + "/api/json";
        String request = doRequest(GET, requestUrl, APPLICATION_XML, null);
        //It is not possible to use Gson parser here because the given JSON contains objects in camel-case and upper-case at he same time.
        return request.substring(request.indexOf("SHA1") + 7).substring(0, 40);
    }

    /**
     * Returns related factory Id, configured for current connector by connector properties.
     */
    String getFactoryId() throws ServerException {
        Optional<String> propertyPrefixOptional = getJenkinsConnectorPropertyPrefix();
        if (propertyPrefixOptional.isPresent()) {
            Map<String, String> properties = configurationProperties.getProperties(JENKINS_CONNECTOR_PREFIX_PATTERN);
            return properties.get(propertyPrefixOptional.get() + JENKINS_CONNECTOR_FACTORY_ID_SUFFIX);
        } else {
            throw new ServerException(String.format(PROPERTY_NOT_FOUND_ERROR_MESSAGE, url, jobName));
        }
    }

    private Optional<String> getJenkinsConnectorPropertyPrefix() {
        Map<String, String> properties = configurationProperties.getProperties(JENKINS_CONNECTOR_PREFIX_PATTERN);
        return properties.entrySet()
                         .stream()
                         .filter(e -> e.getValue().contains(url.substring(url.indexOf("://") + 3, url.length() - 1)))
                         .filter(entry -> properties
                                 .get(entry.getKey().replace(JENKINS_CONNECTOR_URL_SUFFIX, JENKINS_CONNECTOR_JOB_NAME_SUFFIX))
                                 .equals(jobName))
                         .map(entry -> entry.getKey().substring(0, entry.getKey().lastIndexOf(JENKINS_CONNECTOR_URL_SUFFIX)))
                         .findAny();
    }

    private String getCurrentJenkinsJobConfiguration() throws IOException, ServerException {
        String requestUrl = url + "/job/" + jobName + "/config.xml";
        return doRequest(GET, requestUrl, APPLICATION_XML, null);
    }

    private void updateJenkinsJobDescription(String factoryUrl, Document configDocument) throws IOException, ServerException {
        String requestUrl = url + "/job/" + jobName + "/config.xml";
        doRequest(POST, requestUrl, APPLICATION_XML, documentToXml(configDocument));
        LOG.debug("factory link {} successfully added on description of Jenkins job ", factoryUrl, jobName);
    }

    private String documentToXml(Document configDocument) throws ServerException {
        try {
            StringWriter writer = new StringWriter();
            newInstance().newTransformer().transform(new DOMSource(configDocument), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw new ServerException(e.getMessage());
        }
    }

    private Document xmlToDocument(String jobConfigXml) throws ServerException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(jobConfigXml.getBytes()));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ServerException(e.getMessage());
        }
    }

    private String doRequest(String requestMethod,
                             String requestUrl,
                             String contentType,
                             String data) throws IOException, ServerException {
        URL url = new URL(requestUrl + "/job/" + jobName + "/config.xml");
        HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
        try {
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(url.getUserInfo().getBytes()));
            httpConnection.setRequestProperty("Authorization", basicAuth);
            httpConnection.setRequestMethod(requestMethod);
            httpConnection.addRequestProperty(HttpHeaders.CONTENT_TYPE, contentType);
            httpConnection.setDoOutput(true);

            if (!isNullOrEmpty(data)) {
                try (OutputStream outputStream = httpConnection.getOutputStream()) {
                    outputStream.write(data.getBytes());
                }
            }
            final int responseCode = httpConnection.getResponseCode();
            InputStream inputStream = httpConnection.getInputStream();
            if ((responseCode / 100) != 2) {
                InputStream errorStream = httpConnection.getErrorStream();
                throw new ServerException(readAndCloseQuietly(errorStream != null ? errorStream : inputStream));
            }
            return readAndCloseQuietly(inputStream);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }
}
