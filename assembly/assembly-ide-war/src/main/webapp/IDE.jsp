<%--

    Copyright (c) [2012] - [2017] Red Hat, Inc.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
      Red Hat, Inc. - initial API and implementation

--%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Codenvy</title>
    <link rel="shortcut icon" href="/site/images/ico/favicon.ico" />
    <meta initiator="/_app/Application.jsp" />
    <meta property="og:title" content="Codenvy | Provision, Share and Scale Developer Workspaces"/>
    <meta property="og:description" content="Industrial-strength Saas Developer Environments for developers and corporate teams with advanced collaboration, security, and enterprise options."/>

    <script type="text/javascript" language="javascript">

        /**This parameter is needed to define sdk mode.*/
        window.sdk = 1;

        /**
         * Base IDE object
         */

        window.IDE = {};

        /**
         * Initial configuration
         */

        window.IDE.config = {
            "restContext": "/api",
            "websocketContext": "/api/websocket"
        };

        /**
         * Event handlers
         */

        window.IDE.eventHandlers = {};

        window.IDE.eventHandlers.initializationFailed = function (message) {
            if (message) {
                var err = new Error(message);
                window.alert(err.stack);
            } else {
                window.alert("Unable to initialize IDE");
            }
        };

    </script>

    <script type="text/javascript" language="javascript" src="/_app/browserNotSupported.js"></script>
    <script type="text/javascript" language="javascript" async="true" src="/_app/_app.nocache.js"></script>
</head>
<body style="background-color: #373737;">
<!-- Google Tag Manager -->
<noscript><iframe src="//www.googletagmanager.com/ns.html?id=GTM-M69DMB"
                  height="0" width="0" style="display:none;visibility:hidden"></iframe></noscript>
<script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push(
        {'gtm.start': new Date().getTime(),event:'gtm.js'}
);var f=d.getElementsByTagName(s)[0],
        j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
        '//www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
})(window,document,'script','dataLayer','GTM-M69DMB');</script>
<!-- End Google Tag Manager -->

</body>
</html>
