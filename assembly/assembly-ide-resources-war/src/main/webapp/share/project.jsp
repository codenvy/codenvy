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

        %>
<%!

%>
<%
    String _server = "";
    String _share_page_url = "";
    String _title = "";
    String _description = "";
    String _image_url = "";
    String _project_url = "";

    try {

        String path = request.getPathInfo() == null ? "" : request.getPathInfo();
        path = path.startsWith("/") ? path.substring(1) : path;
        path = path.endsWith("/") ? path.substring(0, path.length()) : path;

        if (path.split("/").length != 2) {
            response.sendError(404, "Project and workspace names are not specified");
        }

        _server = request.getScheme() + "://" + request.getServerName() +
                  (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());

        _share_page_url = _server + request.getRequestURI();

        _title = "Codenvy | Provision, Share and Scale Developer Workspaces";

        _description = "Develop faster and release more frequently with Codenvy developer environments. Command Docker microservices to " +
                       "build, run, and deploy projects.";

        _image_url = _server + "/factory/resources/codenvy.png";

        _project_url = _server + "/ws" + request.getPathInfo();
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
        window.location.href = "<%=_project_url%>";
    }, 1);
</script>

</html>
<%
    } catch (Exception e) {
        response.sendError(500, e.getMessage());
    }
%>
