<%--

     [2012] - [2017] Codenvy, S.A.
     All Rights Reserved.

    NOTICE:  All information contained herein is, and remains
    the property of Codenvy S.A. and its suppliers,
    if any.  The intellectual and technical concepts contained
    herein are proprietary to Codenvy S.A.
    and its suppliers and may be covered by U.S. and Foreign Patents,
    patents in process, and are protected by trade secret or copyright law.
    Dissemination of this information or reproduction of this material
    is strictly forbidden unless prior written permission is obtained
    from Codenvy S.A..

--%>
<%@ page
        language="java"
        contentType="text/html; charset=UTF-8"
        pageEncoding="UTF-8"

        import="com.google.gson.JsonArray"
        import="com.google.gson.JsonObject"
        import="com.google.gson.JsonParser"
        import="java.io.BufferedReader"
        %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%!
    public String getFactoryJSON(String factoryURL) throws Exception {
        URL url = new URL(factoryURL);
        URLConnection conn = url.openConnection();

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer html = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            html.append(inputLine);
        }
        in.close();

        return html.toString();
    }

    public String getLink(JsonObject factoryObj, String rel) {
        JsonArray linksArr = factoryObj.get("links").getAsJsonArray();
        for (int i = 0; i < linksArr.size(); i++) {
            JsonObject linkObj = linksArr.get(i).getAsJsonObject();
            if (rel.equals(linkObj.get("rel").getAsString())) {
                return linkObj.get("href").getAsString();
            }
        }

        return null;
    }
%>
<%
    String _error_message = null;

    String _server = "";
    String _factory_id = "";
    String _share_page_url = "";
    String _title = "";
    String _description = "";
    String _image_url = "";
    String _create_project_url = "";

    try {
        _factory_id = request.getRequestURI();

        if (_factory_id == null || _factory_id.trim().isEmpty() || "/".equals(_factory_id.trim())) {
            _error_message = "Factory is not specified";
            throw new Exception();
        }

        while (_factory_id.endsWith("/")) {
            _factory_id = _factory_id.substring(0, _factory_id.length() - 1);
        }

        _factory_id = _factory_id.substring(_factory_id.lastIndexOf("/") + 1);

        _server = request.getScheme() + "://" + request.getServerName() +
                  (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());

        _share_page_url = _server + "/ide-resources/share/factory/" + _factory_id;

        String factoryURL = _server + "/api/factory/" + _factory_id;
        String jsonText = getFactoryJSON(factoryURL);

        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonText).getAsJsonObject();

        _title = "Codenvy | Provision, Share and Scale Developer Workspaces";

        _description = "Develop faster and release more frequently with Codenvy developer environments. Command Docker microservices to " +
                       "build, run, and deploy projects.";

        _image_url = getLink(json, "image");
        if (_image_url == null) {
            _image_url = _server + "/factory/resources/codenvy.png";
        }

        _create_project_url = getLink(json, "create-project");
%>
<!DOCTYPE html>
<html prefix="og: http://ogp.me/ns#">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <title><%=_title%>
    </title>
    <meta name="title" content="<%=_title%>"/>
    <meta property="og:title" content="<%=_title%>"/>

    <meta name="description" content="<%=_description%>"/>
    <meta property="og:description" content="<%=_description%>"/>

    <link rel="image_src" href="<%=_image_url%>"/>
    <meta property="og:image" content="<%=_image_url%>"/>

    <meta property="og:url" content="<%=_share_page_url%>">
    <meta property="og:type" content="website"/>
</head>

<body></body>

<script>
    setTimeout(function () {
        window.location.href = "<%=_create_project_url%>";
    }, 1);
</script>

</html>
<%
    } catch (Exception e) {
        if (_error_message != null) {
            response.sendError(404, _error_message);
        } else {
            response.sendError(500, e.getMessage());
        }
    }
%>
