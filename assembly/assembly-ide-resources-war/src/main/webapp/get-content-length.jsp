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
<%
try {
    String _resource = request.getParameter("resource");
    
    if (!_resource.startsWith("/ide-resources")) {
        response.sendError(404, "Resource is not IDE Resource");
    } else {
        String res = application.getRealPath("") + _resource.substring("/ide-resources".length());
        java.io.File f = new java.io.File(res);
        if (!f.exists()) {
            response.sendError(404, "Resource " + _resource + " does not exist");
        } else {
            out.print("" + f.length());
        }
    }
} catch (Exception e) {
    response.sendError(500, e.getMessage());
}
%>
