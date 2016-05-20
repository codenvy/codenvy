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
/*
 This script has been written for magically loading IDE.
 */

// Google Analytics in C3
(function (i, s, o, g, r, a, m) {
    i['GoogleAnalyticsObject'] = r;
    i[r] = i[r] || function () {
        (i[r].q = i[r].q || []).push(arguments)
    }, i[r].l = 1 * new Date();
    a = s.createElement(o),
        m = s.getElementsByTagName(o)[0];
    a.async = 1;
    a.src = g;
    m.parentNode.insertBefore(a, m)
})(window, document, 'script', '//www.google-analytics.com/analytics.js', 'ga');

ga('create', 'UA-37306001-1', 'auto');
ga('send', 'pageview');

var Script = new function () {

    /*
     * Sets a new function to creation an element from a HTML string.
     */
    Document.prototype.elementFromHTML = function (html) {
        var div = this.createElement('div');
        div.innerHTML = html;
        return div.childNodes[0];
    };

    /*
     * Sets a new function to removing the element.
     */
    Element.prototype.remove = function () {
        this.parentNode.removeChild(this);
    };

    /*
     * Determines whether string starts with specified prefix.
     */
    if (!String.prototype.startsWith) {
        String.prototype.startsWith = function (prefix) {
            return !this.indexOf(prefix);
        }
    }

    /*
     * Determines whether string ends with specified suffix.
     */
    if (!String.prototype.endsWith) {
        String.prototype.endsWith = function (suffix) {
            return this.indexOf(suffix, this.length - suffix.length) !== -1;
        }
    }

    if (!Array.prototype.contains) {
        Array.prototype.contains = function (k) {
            for (var p in this) {
                if (this[p] === k) {
                    return true;
                }
            }
            return false;
        }
    }


    // Remember this JavaScript element
    this.scripts = document.getElementsByTagName('script');

    for (var i = 0; i < this.scripts.length; i++) {
        var s = this.scripts[i];
        if (s.src.indexOf("ide-resources/initializer/init.js?") > 0) {
            this.script = s;
        }
    }

    // Remember parent of this JavaScript element
    this.parent = this.script.parentNode;

    // Fetch URL to Loader stylesheet CSS
    this.css = this.script.src.substring(0, this.script.src.lastIndexOf(".")) + ".css";
    this.css += "?" + new Date().getTime();

    // Remove this script element from Document
    this.parent.removeChild(this.script);

};


var Debug = new function () {

    this.dump_properties = function (obj) {
        if (obj) {
            var total = 0;
            console.log("properties -------------------------------------------------------------------");
            for (var key in obj) {
                var val = obj[key];

                if (typeof val === 'number') {
                    console.log("    " + key + " : " + val);
                } else if (typeof val === 'boolean') {
                    console.log("    " + key + " : " + val);

                } else if (typeof val === 'string') {

                    var vv = val;
                    if (vv.length > 64) {
                        vv = vv.substring(0, 64) + "...";
                    }

                    console.log("    " + key + " : \"" + vv + "\"");
                    //console.log("    " + key + " : " + (typeof val) + " : " + vv);

                } else if (obj[key]) {
                    console.log("    " + key + " : " + (typeof val));
                } else {
                    console.log("    " + key + " : null");
                }

                total++;
            }
            console.log("------------------------------------------------------------------------------");
            console.log("total: " + total);
            console.log("------------------------------------------------------------------------------");
        }
    }

};


var Page = new function () {

    /*
     * Injects link element pointed to external CSS resources.
     */
    this.injectCss = function (href) {
        var head = document.getElementsByTagName('head')[0];
        var links = head.getElementsByTagName('link');

        for (var i = 0; i < links.length; i++) {
            if (href == links[i].href) {
                return;
            }
        }

        var link = document.createElement('link');
        link.rel = "stylesheet";
        link.type = "text/css";
        link.href = href;
        head.appendChild(link);
    };


    /*
     * Injects JavaScript element.
     */
    this.injectScript = function (src) {
        var po = document.createElement('script');
        po.type = 'text/javascript';
        po.async = true;
        po.src = src;
        document.getElementsByTagName('head')[0].appendChild(po);
    };


    /*
     * Returns browser identifier.
     *
     * This code was copied from
     * https://code.google.com/p/google-web-toolkit/source/browse/trunk/user/src/com/google/gwt/user/rebind/UserAgentPropertyGenerator.java?r=9701
     */
    this.getBrowserIdentifier = function () {
        var userAgent = navigator.userAgent.toLowerCase();

        var makeVersion = function (result) {
            return (parseInt(result[1]) * 1000) + parseInt(result[2]);
        };

        if (userAgent.indexOf('opera') != -1) {
            return 'opera';
        } else if (userAgent.indexOf('webkit') != -1) {
            return 'safari';
        } else if (userAgent.indexOf('msie') != -1) {
            if ($doc.documentMode >= 8) {
                return 'ie8';
            } else {
                var result = /msie ([0-9]+)\\.([0-9]+)/.exec(userAgent);
                if (result && result.length == 3) {
                    var v = makeVersion(result);
                    if (v >= 6000) {
                        return 'ie6';
                    }
                }
            }
        } else if (userAgent.indexOf('gecko') != -1) {
            return 'gecko1_8';
        }

        return 'unknown';
    };


    /*
     * Returns value of named query parameter.
     */
    this.getQueryParam = function (name, query) {
        if (!query) {
            query = window.location.search;
        }

        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
        var results = regex.exec(query);
        if (results) {
            return decodeURIComponent(results[1].replace(/\+/g, " "));
        }
    };

    /*
     *
     */
    this.getInitiator = function () {
        var metas = document.getElementsByTagName("meta");
        for (var i = 0; i < metas.length; i++) {
            var meta = metas[i];
            if (meta.hasAttribute("initiator")) {
                return meta.getAttribute("initiator");
            }
        }

        return null;
    };


    /*
     * Relocates page URL using history API.
     */
    this.setURL = function (title, url) {
        try {
            if (!window["_history_relocation_id"]) {
                window["_history_relocation_id"] = 0;
            }

            document.title = title;
            history.pushState(window["_history_relocation_id"], title, url);
            window["_history_relocation_id"]++;
        } catch (e) {
            console.log(e.message);
        }
    };


    /*
     * Redirects to URL.
     */
    this.redirect = function (url) {
        //As an HTTP redirect (back button will not work )
        //window.location.replace(url);

        //like if you click on a link (it will be saved in the session history, so back button will work as expected)
        window.location.href = url;
    }

};


var Initializer = new function () {

    // Pause between steps
    this.STEP_DELAY = 500;

    /***************************************************************************
     *
     * Variables
     *
     **************************************************************************/

        // Loader UI element
    this.element = null;

    // Tenant name
    this.tenant = null;

    // Is need to wait for tenant
    this.waitForTenant = false;

    // URL to main IDE JavaScript
    this.ideJavaScriptURL = null;

    // Size of main IDE JavaScript
    this.ideJavaScriptSize = 0;

    // Action to be performed just after loading IDE
    this.postActionType = null;

    /*
     * IDE configuration.
     */

    /**
     * Base IDE object
     */
    window.IDE = {};

    /**
     * Initial configuration
     */
    window.IDE.config = {
        "restContext": "/api",
        "context": "/ws",
        "workspaceName": null,
        "workspaceId": null,
        "projectName": null,
        "startupParams": null,
        "hiddenFiles": ".*",
        "facebookLikeURL": "/ide-resources/_app/facebook-like.html",
        "googleLikeURL": "/ide-resources/_app/google-like.html",
        "cheExtensionPath": "/wsagent/ext"
    };

    /**
     * Event handlers.
     * Are used to interact with IDE.
     */
    window.IDE.eventHandlers = {};

    /**
     * Expands or collapses the editor.
     */
    window.IDE.eventHandlers.expandEditor = null;

    /**
     * Is called by IDE when the IDE cannot be loaded.
     */
    window.IDE.eventHandlers.initializationFailed = function (message) {
        SiteApp.onError(message);
    };


    /*
     * Creates and shows IDE Loader.
     */
    this.show = function () {
        Initializer.element = document.elementFromHTML("" +
        "<div class='initializer' id='initializer' style=''>" +
        "<div>" +
        "<div title></div>" +
        "<div logo><i></i><i></i></div>" +
        "<div action></div>" +
        "<div progress></div>" +
        "</div>" +
        "</div>");
        document.body.appendChild(Initializer.element);

        Initializer.element.classList.add("bounceInDown");
    };

    /*
     * Hides loader with fade out animation.
     */
    this.hide = function () {
        //remove document.body.style style="background-color: #373737;"
        document.body.style["background-color"] = null;

        if (Initializer.element == null) {
            return;
        }

        Initializer.element.classList.add("bounceOutUp");
        setTimeout(function () {
            Initializer.element.parentNode.removeChild(Initializer.element);
        }, 2000);
    };

    /*
     * Sets Loader title.
     */
    this.setTitle = function (title) {
        Initializer.element.firstChild.firstChild.innerHTML = title;
    };

    /*
     * Sets Loader message.
     */
    this.setMessage = function (message) {
        Initializer.element.firstChild.childNodes[2].innerHTML = message;
    };

    /*
     * Displays error message.
     */
    this.setError = function (message) {
        Initializer.element.firstChild.childNodes[2].style.color = "red";
        Initializer.element.firstChild.childNodes[2].innerHTML = message;
    };

    /*
     * Switches current step.
     */
    this.setStep = function (step, title, message) {
        Initializer.element.setAttribute("step", step);
        Initializer.setTitle(title);
        Initializer.setMessage(message);
    };

    /*
     * If action=clone-projects then determine destination workspace as own workspace
     * and redirect to destination workspace
     */
    this.processCloneProjectAction = function (success) {
        // If action is absent then do nothing
        if (window.location.search.indexOf("action=clone-projects") < 0) {
            success();
            return;
        }

        var destinationWorkspace = Page.getQueryParam("dest-workspace-name");

        // If destination workspace is absent try determine it
        if (!destinationWorkspace) {
            try {
                var request = new XMLHttpRequest();

                request.onerror = request.abort = function () {
                    Initializer.setError("Cannot get memberships");
                };

                request.onload = function () {
                    if (request.status == 200) {
                        var memberships = JSON.parse(request.responseText);

                        if (typeof memberships !== 'undefined' && memberships.length > 0) {
                            var persistedWorkspaces = [];

                            memberships.forEach(function (membership) {
                                if (membership.workspaceReference.temporary != true) {
                                    persistedWorkspaces.push(membership.workspaceReference);
                                }
                            });

                            // If user has one named workspace then redirect to it
                            if (persistedWorkspaces.length == 1) {
                                Page.setURL("Codenvy Developer Environment", window.IDE.config.context + "/" + persistedWorkspaces[0].name +
                                window.location.search);
                            } else {
                                // If workspace destination isn't determined then remove action clone project
                                Page.setURL("Codenvy Developer Environment", window.location.origin + window.location.pathname +
                                window.location.search.replace("action=clone-projects", ""));
                            }
                        }

                        success();
                    } else {
                        Initializer.setError("Cannot get memberships");
                    }
                };

                var url = "/api/workspace/all";
                request.open("GET", url, true);
                request.send();
            } catch (e) {
                console.log(e.message);
                Initializer.setError("Cannot get memberships");
            }
        } else {
            // If current workspace not equals destination workspace
            // then use destination workspace as current
            if (destinationWorkspace != window.location.pathname.split('/')[2]) {
                Page.setURL("Codenvy Developer Environment", window.IDE.config.context + "/" + destinationWorkspace +
                window.location.search);
            }
            success();
        }
    };

    /***************************************************************************
     *
     * Directly entering tenant
     *
     **************************************************************************/
    this.processTenant = function (tenant, project, path, startupParams) {
        Initializer.tenant = tenant;
        Initializer.waitForTenant = false;

        window.IDE.config.workspaceName = tenant;
        window.IDE.config.projectName = project;
        window.IDE.config.startupParams = startupParams;

        Initializer.try_access_tenant();
    };


    this.setParams =  function (startupParams) {
        window.IDE.config.startupParams = startupParams;
    };


    /***************************************************************************
     *
     * Entering tenant with processing a factory
     *
     * ( /site/wait-for-tenant )
     *
     **************************************************************************/
    this.processWaitForTenant = function (tenant, postActionType, startupParams) {
        Initializer.tenant = tenant;
        Initializer.waitForTenant = true;
        Initializer.postActionType = postActionType;

        window.IDE.config.workspaceName = tenant;
        window.IDE.config.startupParams = startupParams;

        Initializer.try_access_tenant();
    };


    /***************************************************************************
     *
     * Start loading IDE.
     *
     **************************************************************************/

    this.start_loading = function () {
        Initializer.show();

        Initializer.setStep(1, "Preparing Your Workspace", "Caching the Codenvy client...");

        Page.setURL("Codenvy Developer Environment", window.IDE.config.context + "/" + window.IDE.config.workspaceName +
        (window.IDE.config.projectName ? "/" + window.IDE.config.projectName : ""));

        // Load compilation-mapping.txt file to determine which IDE JavaScript file will be loaded.
        try {
            var request = new XMLHttpRequest();

            request.onerror = request.abort = function () {
                Initializer.setError("Cannot load compilation mappings");
            };

            request.onload = function () {
                if (request.status == 200) {
                    Initializer.determine_ide_javascript_filename(request.responseText);
                } else {
                    Initializer.setError("Cannot load compilation mappings");
                }
            };

            var url = "/ide-resources/_app/compilation-mappings.txt?" + new Date().getTime();
            request.open("GET", url, true);
            request.send();
        } catch (e) {
            console.log(e.message);
            Initializer.setError("Cannot load compilation mappings");
        }
    };


    /***************************************************************************
     *
     * Determine name of IDE JavaScript
     *
     **************************************************************************/

    this.ideJavaScriptURL = null;

    this.determine_ide_javascript_filename = function (compilation_mappings_txt) {
        try {
            var browser_identifier = Page.getBrowserIdentifier();

            var mappings = compilation_mappings_txt.split("\n");
            var index = 0;

            var js_name = null;
            var mobile_user_agent = null;
            var user_agent = null;
            var webide_client_os = null;

            while (true) {
                if (index == mappings.length) {
                    break;
                }

                if (mappings[index] === "") {
                    if (js_name && user_agent && browser_identifier === user_agent.split(" ")[1]) {
                        Initializer.ideJavaScriptURL = "/ide-resources/_app/" + js_name;
                        break;
                    }

                    js_name = null;
                    mobile_user_agent = null;
                    user_agent = null;
                    webide_client_os = null;
                }

                if (mappings[index].endsWith(".cache.js")) {
                    js_name = mappings[index];
                }

                if (mappings[index].startsWith("mobile.user.agent ")) {
                    mobile_user_agent = mappings[index];
                }

                if (mappings[index].startsWith("user.agent ")) {
                    user_agent = mappings[index];
                }

                if (mappings[index].startsWith("webide.clientOs ")) {
                    webide_client_os = mappings[index];
                }

                index++;
            }

            if (Initializer.ideJavaScriptURL) {
                Initializer.determine_ide_javascript_contentlength();
            } else {
                Initializer.setError("Cannot determine IDE JavaScript for current browser");
            }
        } catch (e) {
            console.log(e.message);
            Initializer.setError("Cannot determine IDE JavaScript for current browser");
        }
    };


    /***************************************************************************
     *
     * Determine size of IDE JavaScript.
     *
     **************************************************************************/

    this.determine_ide_javascript_contentlength = function () {
        try {
            var request = new XMLHttpRequest();

            request.onerror = request.abort = function () {
                Initializer.setError("Cannot determine size of IDE JavaScript");
            };

            request.onload = function () {
                if (request.status == 200) {
                    Initializer.ideJavaScriptSize = parseInt(request.responseText);
                    Initializer.load_ide_javascript();
                } else {
                    Initializer.setError("Cannot determine size of IDE JavaScript");
                }
            };

            var url = "/ide-resources/get-content-length.jsp?resource=" + Initializer.ideJavaScriptURL;
            request.open("GET", url, true);
            request.send();
        } catch (e) {
            console.log(e.message);
            Initializer.setError("Cannot determine size of IDE JavaScript");
        }
    };


    /***************************************************************************
     *
     * Load content of IDE JavaScript.
     *
     **************************************************************************/

    this.ideJavaScriptBytesLoaded = 0;
    this.ideJavaScriptSuccessAttempts = 0;

    this.load_ide_javascript = function () {
        Initializer.ideJavaScriptBytesLoaded = 0;

        try {
            var request = new XMLHttpRequest();

            request.onprogress = function (event) {
                Initializer.ideJavaScriptBytesLoaded = event.loaded;
                var percentComplete = Initializer.ideJavaScriptBytesLoaded / Initializer.ideJavaScriptSize * 100;
                var rounded = Math.round(percentComplete);
                Initializer.setMessage("Caching the Codenvy client... ( " + rounded + "% )");
            };

            request.onload = function () {
                if (request.status == 200) {
                    Initializer.ideJavaScriptSuccessAttempts++;
                    if (Initializer.ideJavaScriptSuccessAttempts == 20) {
                        setTimeout(Initializer.setting_up_builders, Initializer.STEP_DELAY);
                        return;
                    }

                    if (Initializer.ideJavaScriptSize == Initializer.ideJavaScriptBytesLoaded) {
                        setTimeout(Initializer.setting_up_builders, Initializer.STEP_DELAY);
                    } else {
                        // restart
                        setTimeout(Initializer.load_ide_javascript, 100);
                    }
                } else {
                    Initializer.setError("Unable to load IDE JavaScript. Connection error.");
                }
            };

            request.onloadend = function () {
            };

            request.ontimeout = function () {
                setTimeout(Initializer.load_ide_javascript, 100);
            };

            request.onerror = request.abort = function () {
                Initializer.setError("Unable to load IDE JavaScript. Connection error.");
            };

            request.open("GET", Initializer.ideJavaScriptURL, true);
            request.send();
        } catch (e) {
            console.log(e.message);
            Initializer.setError("Unable to load IDE JavaScript. Connection error.");
        }
    };


    /***************************************************************************
     *
     * Try to access tenant
     *
     **************************************************************************/

    this.tryAccessTenantAttempts = 0;

    this.try_access_tenant = function () {
        Initializer.tryAccessTenantAttempts++;
        if (Initializer.tryAccessTenantAttempts == 300) {
            Initializer.setError("Unable to initialize your workspace. Connection timeout.");
            return;
        }

        try {
            var request = new XMLHttpRequest();

            request.onload = function () {
                if (request.status == 200) {
                    Initializer.fetch_workspace_params_from_response(request.responseText);
                } else {
                    setTimeout(Initializer.try_access_tenant, 100);
                }
            };

            request.onerror = request.abort = function () {
                Initializer.setError("Unable to initialize your workspace. Connection error.");
            };

            var url = "/api/workspace?name=" + Initializer.tenant;
            request.open("GET", url, true);
            request.send();
        } catch (e) {
            console.log(e.message);
            Initializer.setError("Unable to initialize your workspace. Connection error.");
        }
    };

    this.fetch_workspace_params_from_response = function (responseText) {
        try {
            var workspace = JSON.parse(responseText);

            //Initializer.config.wsName = workspace.name;
            window.IDE.config.workspaceName = workspace.name;

            //Initializer.config.wsId = workspace.id;
            window.IDE.config.workspaceId = workspace.id;

            //Debug.dump_properties(Initializer.config);

            Initializer.check_browser_support_websocket();
        } catch (e) {
            console.log("Can't fetch workspace ID. " + e.message);
            Initializer.setError("Unable to initialize your workspace.");
        }
    };

    /***************************************************************************
     *
     * Checking support browser and websocket
     *
     **************************************************************************/
    this.check_browser_support_websocket = function () {

        // Checking support browser
        var user_agent = navigator.userAgent.toLowerCase();
        if ((user_agent.indexOf("chrome") === -1 && user_agent.indexOf("firefox") === -1 && user_agent.indexOf("safari") === -1)) {
            Page.redirect("/site/error/browser-not-supported");
        }

        // Checking support websocket
        var websocket_error_page_url = "/site/error/websocket-connection-error";
        try {
            if (("WebSocket" in window)) {

                var wsProtocol = window.location.protocol == "https:" ? "wss:" : "ws:";
                // Try to open websocket connection
                var test_socket = new WebSocket(wsProtocol + "//" + window.location.host + "/api/ws/" + window.IDE.config.workspaceId);

                test_socket.onopen = function () {
                    test_socket.close();
                    Initializer.check_project_access();
                };

                test_socket.onerror = function () {
                    Page.redirect(websocket_error_page_url);
                }
            } else {
                Page.redirect(websocket_error_page_url);
            }
        } catch (e) {
            // Object WebSocket is incorrect or error connecting to host
            console.log(e.message);
            Page.redirect(websocket_error_page_url);
        }
    };

    /***************************************************************************
     *
     * Try to access project
     *
     **************************************************************************/
    this.check_project_access = function () {
        try {
            if (window.IDE.config.projectName) {
                var request = new XMLHttpRequest();

                // if we have access to the project, starting loader.
                // otherwise, should redirect to login or information page.
                request.onload = function () {
                    if (request.status == 200) {
                        Initializer.start_loading();
                    } else if (request.status == 404) {
                        Page.redirect("/site/error/unavailable-resource");
                    } else {
                        Initializer.check_user_loggedin();
                    }
                };

                request.onerror = request.abort = function () {
                    Initializer.setError("Unable to access your project. Connection error.");
                };

                var url = "/api/project/" + window.IDE.config.workspaceId + "/" + window.IDE.config.projectName;
                request.open("GET", url, true);
                request.send();
            }
            else {
                Initializer.start_loading();
            }
        }
        catch
            (e) {
            console.log(e.message);
            Initializer.setError("Unable to access your project. Connection error.");
        }
    };


    /***************************************************************************
     *
     * Check if user is logged in
     *
     **************************************************************************/
    this.check_user_loggedin = function () {
        try {
            var request = new XMLHttpRequest();
            var redirect_url = window.location.protocol + "//"
                + window.location.host + window.IDE.config.context + "/"
                + window.IDE.config.workspaceName + "/" + window.IDE.config.projectName;
            request.onload = function () {
                if (request.status == 200) {
                    Initializer.check_user_temporary();
                } else {
                    Page.redirect("/site/login?redirect_url=" + encodeURIComponent(redirect_url));
                }
            };

            request.onerror = request.abort = function () {
                Initializer.setError("Unable to access user info. Connection error.");
            };

            var url = "/api/user/";
            request.open("GET", url, true);
            request.send();
        } catch (e) {
            console.log(e.message);
            Initializer.setError("Unable to access user info. Connection error.");
        }
    };

    /***************************************************************************
     *
     * Check if user is persistent or temporary
     *
     **************************************************************************/
    this.check_user_temporary = function () {
        try {
            var request = new XMLHttpRequest();
            var redirect_url = window.location.protocol + "//"
                + window.location.host + window.IDE.config.context + "/"
                + window.IDE.config.workspaceName + "/" + window.IDE.config.projectName;

            request.onload = function () {
                if (request.status == 200) {
                    var prefs = JSON.parse(request.responseText);

                    var temporary = prefs.temporary == "true";

                    if (temporary) {
                        Page.redirect("/site/login?redirect_url=" + encodeURIComponent(redirect_url));
                    } else {
                        Page.redirect("/site/error/unavailable-resource");
                    }
                } else {
                    Page.redirect("/site/login?redirect_url=" + encodeURIComponent(redirect_url));
                }
            };

            request.onerror = request.abort = function () {
                Initializer.setError("Unable to access user info. Connection error.");
            };

            var url = "/api/profile/prefs";
            request.open("GET", url, true);
            request.send();
        } catch (e) {
            console.log(e.message);
            Initializer.setError("Unable to access user info. Connection error.");
        }
    };

    /***************************************************************************
     *
     * Setting up builders.
     *
     **************************************************************************/
    this.setting_up_builders = function () {
        Page.injectCss("/ide-resources/_app/css/ide01.css");
        Page.injectScript("/ide-resources/_app/browserNotSupported.js");
        Page.injectScript("/ide-resources/_app/_app.nocache.js");
        setTimeout(Initializer.wait_for_ide_has_been_loaded, Initializer.STEP_DELAY);
    };


    /***************************************************************************
     *
     * Preconfiguring runners
     *
     **************************************************************************/
    this.preconfigure_runners = function () {
        Initializer.setStep(3, "Pre-Configuring Runners", "");
        setTimeout(Initializer.load_your_configuration, Initializer.STEP_DELAY);
    };


    /***************************************************************************
     *
     * Loading Your Configuration
     *
     **************************************************************************/

    this.load_your_configuration = function () {
        Initializer.setStep(4, "Loading Your Configuration", "");
        setTimeout(Initializer.open_your_project, Initializer.STEP_DELAY);
    };


    /***************************************************************************
     *
     * Opening Your Project
     *
     **************************************************************************/
    this.open_your_project = function () {
        if (Initializer.postActionType && Initializer.postActionType == "factory") {
            Initializer.setStep(5, "Opening Your Project",
                "Setup time depends upon the size of your project.");
        } else {
            Initializer.setStep(5, "Opening Your Workspace",
                "Setup time depends upon the size of your project.");
        }

        setTimeout(Initializer.wait_for_ide_has_been_loaded, Initializer.STEP_DELAY);
    };


    /***************************************************************************
     *
     * Wait for IDE has been successfully loaded
     *
     **************************************************************************/

    this.waitForIdeAttempts = 0;

    this.wait_for_ide_has_been_loaded = function () {
        Initializer.waitForIdeAttempts++;
        if (Initializer.waitForIdeAttempts == 300) {
            Initializer.hide();
            return;
        }

        if (document.getElementById("codenvyIdeWorkspaceViewImpl")) {

            Initializer.hide();

//            if (Initializer.postActionType && Initializer.postActionType == "factory") {
//                Initializer.setMessage("Cloning project. Time needed depends on repository size.");
//                setTimeout(Initializer.wait_for_cloning_complete, 500);
//            } else {
//                Initializer.hide();
//            }

        } else {
            setTimeout(Initializer.wait_for_ide_has_been_loaded, 100);
        }
    };

    /***************************************************************************
     *
     * Waiting for cloning has been finished.
     *
     **************************************************************************/

//    this.wait_for_cloning_complete = function() {
//        var debugNavigationSelectedFile = document.getElementById("debug-navigation-selected-file");
//        if (debugNavigationSelectedFile && debugNavigationSelectedFile.innerHTML) {
//            Initializer.hide();
//        } else if (document.getElementById("operation") && document.getElementById("ideOutputContent")) {
//            Initializer.hide();
//        } else if (document.getElementById("codenvyAskForValueModalView")
//                || document.getElementById("ideAskModalView")
//                || document.getElementById("ideWarningModalView")
//                || document.getElementById("ideInformationModalView")
//                || document.getElementById("codenvyAskForValueModalView-window")
//                || document.getElementById("ideAskModalView-window")
//                || document.getElementById("ideWarningModalView-window")
//                || document.getElementById("ideInformationModalView-window")) {
//            Initializer.hide();
//        } else {
//            setTimeout(Initializer.wait_for_cloning_complete, 1000);
//        }
//    }

};


/*********************************************************************************************
 *
 * Utilites from site/script/models
 *
 *********************************************************************************************/

var SiteApp = new function () {


    this.onError = function (message, exception) {
        Initializer.hide();
        document.body.style["background-color"] = "#373737";

        var html = "" +
            "<div style='position: absolute; left:0px; right:0px; top:0px; bottom:0px; overflow:hidden;'>" +
            "<div id='error-panel' class='select-ws-error'>" +
            "<div class='select-ws-logo'></div>" +
            "<hr class='select-ws-line'>" +
            "<div id='select-ws-error-message' class='select-ws-error-message'>" + message + "</div>" +
            "</div>" +
            "</div>";

        SiteApp.errorElement = document.elementFromHTML(html);
        document.body.appendChild(SiteApp.errorElement);

        document.body.addEventListener('click', function () {
            SiteApp.errorElement.classList.remove("bounceInDown");
            SiteApp.errorElement.classList.add("bounceOutUp");

            setTimeout(function () {
                Page.redirect("/");
            }, 600);

            //Page.redirect("/");
        }, false);

        SiteApp.errorElement.classList.add("bounceInDown");

        if (exception) {
            console.log(exception.message);
        }
    };


    this.authenticate = function (username, bearertoken, success, error) {
        try {
            var request = new XMLHttpRequest();

            request.onreadystatechange = function () {
                if (this.readyState == this.DONE) {
                    if (this.status == 200) {
                        success();
                    } else {
                        error("Unable to authenticate current user.");
                    }
                }
            };

            var data = {username: username.toLowerCase(), token: bearertoken};
            var authenticateUrl = "/api/internal/token/authenticate";

            request.open("POST", authenticateUrl, true);
            request.setRequestHeader("Content-type", "application/json");
            request.send(JSON.stringify(data));
        } catch (e) {
            error("Unable to authenticate current user.", e);
        }
    };


    this.createWorkspace = function (workspace, account, success, error) {
        try {
            var request = new XMLHttpRequest();

            request.onreadystatechange = function () {
                if (this.readyState == this.DONE) {
                    if (this.status == 201) {
                        success();
                    } else {
                        error("Unable to create workspace " + workspace + ".");
                    }
                }
            };

            var url = "/api/workspace";
            var data = {name: workspace, accountId: account};
            request.open("POST", url, true);
            request.setRequestHeader("Content-type", "application/json");
            request.send(JSON.stringify(data));
        } catch (e) {
            error("Unable to create workspace " + workspace + ".", e);
        }
    };

    // try find existing account if fail then create new
    this.ensureExistenceAccount = function (accountName, success, error) {
        try {
            var request = new XMLHttpRequest();
            request.onreadystatechange = function () {
                if (this.readyState == this.DONE) {
                    if (this.status == 200) {
                        try {
                            var memberships = JSON.parse(request.responseText);
                            if (memberships.length > 0) {
                                memberships.forEach(function (membership) {
                                    var isOwner = (membership.roles.indexOf('account/owner') >= 0);
                                    if (isOwner) {
                                        success(membership.accountReference.id);
                                    }
                                });
                            } else {
                                // user hasn't memberships
                                SiteApp.createAccount(accountName, success, error);
                            }
                        } catch (e) {
                            // error parsing of response
                            SiteApp.createAccount(accountName, success, error);
                        }
                    } else {
                        SiteApp.createAccount(accountName, success, error);
                    }
                }
            };

            var url = "/api/account";
            request.open("GET", url, true);
            request.send();
        } catch (e) {
            // error sending request
            SiteApp.createAccount(accountName, success, error);
        }
    };

    this.createAccount = function (account, success, error) {
        try {
            var request = new XMLHttpRequest();
            request.onreadystatechange = function () {
                if (this.readyState == this.DONE) {
                    if (this.status == 201) {
                        try {
                            success(JSON.parse(request.responseText).id);
                        } catch (e) {
                            error("Unable to create account " + account + ".");
                        }
                    } else {
                        error("Unable to create account " + account + ".");
                    }
                }
            };

            var url = "/api/account";
            var data = {name: account};
            request.open("POST", url, true);
            request.setRequestHeader("Content-type", "application/json");
            request.send(JSON.stringify(data));
        } catch (e) {
            error("Unable to create account " + account + ".", e);
        }
    }
};


/*********************************************************************************************
 *
 * /site/auth/create
 *
 *********************************************************************************************/
var AuthCreate = new function () {

    this.processCreate = function (username, bearertoken, src_workspace, workspace, redirect_url, queryParams) {
        Page.setURL("Codenvy Developer Environment", "/");

        SiteApp.authenticate(username, bearertoken, function () {
            if (workspace) {
                SiteApp.ensureExistenceAccount(workspace,
                    function (account) {
                        SiteApp.createWorkspace(workspace, account, function () {
                            if (redirect_url) {
                                Page.redirect(redirect_url);
                            } else if (src_workspace) {
                                Initializer.processWaitForTenant(workspace, "", "?" + queryParams);
                            } else {
                                //Initializer.processWaitForTenant(workspace, "create", "?" + queryParams);
                                Page.redirect("/dashboard/");
                            }
                        }, SiteApp.onError)
                    },
                    SiteApp.onError);

            } else if (src_workspace) {
                SelectWorkspace.getTenants(
                    function (workspaces) {
                        for (var i = 0; i < workspaces.length; i++) {
                            if (!workspaces[i].workspaceReference.temporary) {
                                Initializer.processWaitForTenant(workspaces[i].workspaceReference.name, "", "?" + queryParams);
                            }
                        }
                    }, SiteApp.onError);
            } else if (redirect_url) {
                var base = window.location.protocol + "//" + window.location.host + window.IDE.config.context + "/";

                if (window.location.href.startsWith(base)) {
                    var tenant = redirect_url.substring(base.length);

                    if (tenant.indexOf("?") < 0) {
                        Initializer.processTenant(tenant, null, null, null);
                    } else {
                        var params = tenant.substring(tenant.indexOf("?"));
                        tenant = tenant.substring(0, tenant.indexOf("?"));
                        Initializer.processTenant(tenant, null, null, params);
                    }
                } else {
                    Page.redirect(redirect_url);
                }
            } else {
                Page.redirect("/dashboard/");
            }
        }, SiteApp.onError);
    }
};


var SelectWorkspace = new function () {

    this.getTenants = function (success, error) {
        var tenantsURL = "/api/workspace/all";
        try {
            var request = new XMLHttpRequest();

            request.onreadystatechange = function () {
                if (this.readyState == this.DONE) {
                    if (this.status == 200) {
                        try {
                            success(JSON.parse(request.responseText));
                        } catch (e) {
                            error("Unable to receive list of workspaces");
                        }
                    } else {
                        error("Unable to receive list of workspaces");
                    }
                }
            };

            request.open("GET", tenantsURL, true);
            request.send();
        } catch (e) {
            error("Unable to receive list of workspaces", e);
        }
    };


    /*********************************************************************************************
     *
     * Displays Select tenant page
     *
     *********************************************************************************************/
    this.showTenants = function (tenants) {
        var managed = new Array();
        var shared = new Array();

        for (var i = 0; i < tenants.length; i++) {
            var tenant = tenants[i];

            if (tenant.roles.contains("workspace/admin")) {
                managed.push(tenant);
            } else if (tenant.roles.contains("workspace/developer")) {
                shared.push(tenant);
            }
        }

        var html = "<div id='select-ws' class='select-ws'>" +
            "<div class='select-ws-logo'></div>" +
            "<h2 class='select-ws-title'>Select your destination workspace</h2>" +
            "<hr class='select-ws-line'>";

        if (managed.length > 0) {
            html += "<span class='select-ws-descr'>Managed by you:</span>";
            html += "<ul id='managed-workspace-list' class='select-ws-list'>";
            for (var i = 0; i < managed.length; i++) {
                html += "<li" +
                " onclick='SelectWorkspace.onTenantSelected(\"" + managed[i].workspaceReference.name + "\")'>" + managed[i].workspaceReference.name + "</li>";
            }
            html += "</ul>";
        }

        if (shared.length > 0) {
            html += "<span class='select-ws-descr'>Shared with you:</span>";
            html += "<ul id='shared-workspace-list' class='select-ws-list'>";
            for (var i = 0; i < shared.length; i++) {
                html += "<li" +
                " onclick='SelectWorkspace.onTenantSelected(\"" + shared[i].workspaceReference.name + "\")'>" + shared[i].workspaceReference.name + "</li>";
            }
            html += "</ul>";
        }

        html += "</div>";

        this.element = document.elementFromHTML(html);
        document.body.appendChild(this.element);
        this.element.classList.add("bounceInDown");
    };


    /*********************************************************************************************
     *
     * Perform actions when tenant selected
     *
     *********************************************************************************************/

    this.onTenantSelected = function (tenantName) {
        SelectWorkspace.element.classList.remove("bounceInDown");
        SelectWorkspace.element.classList.add("bounceOutUp");

        setTimeout(function () {
            SelectWorkspace.element.parentNode.removeChild(SelectWorkspace.element);
        }, 2000);

        setTimeout(function () {
            Initializer.processTenant(tenantName, null, null, SelectWorkspace.startupParams);
        }, 500);
    };


    this.startupParams = null;

    /*********************************************************************************************
     *
     * Displays Select tenant page
     *
     *********************************************************************************************/

    this.process = function (queryParams) {
        if (queryParams) {
            SelectWorkspace.startupParams = queryParams;
        }

        Page.setURL("Codenvy Developer Environment", "/");

        SelectWorkspace.getTenants(function (tenants) {
            SelectWorkspace.tenants = tenants;

            var persistentTenants = new Array();
            for (var i = 0; i < tenants.length; i++) {
                if (!tenants[i].workspaceReference.temporary) {
                    persistentTenants.push(tenants[i]);
                }
            }

            if (persistentTenants.length == 0) {
                window.location.href = "/site/create-account";

            } else if (persistentTenants.length == 1) {
                if (SelectWorkspace.startupParams && SelectWorkspace.startupParams == "?forced") {
                    SelectWorkspace.showTenants(persistentTenants);
                } else {
                    Initializer.processTenant(persistentTenants[0].workspaceReference.name, null, null, SelectWorkspace.startupParams);
                }
            } else {
                SelectWorkspace.showTenants(persistentTenants);
            }
        }, SiteApp.onError);

    }

};


Page.injectCss(Script.css);

//Initializer.setting_up_builders();

switch (Page.getInitiator()) {
//
//    case window.IDE.config.context + "/_app/Application.jsp":
//        Initializer.processCloneProjectAction(function () {
//            var workspaceName = window.location.pathname.split('/')[2];
//            if (!workspaceName) {
//                Page.redirect("/dashboard/");
//            } else {
//                var project = window.location.pathname.split('/')[3];
//                if (!project) {
//                    if (window.location.search) {
//                        Initializer.processTenant(workspaceName, null, null, window.location.search);
//                    } else {
//                        Page.redirect("/dashboard/");
//                    }
//                } else {
//                    Initializer.processTenant(workspaceName, project, null, window.location.search);
//                }
//            }
//        });
//
//        break;
//
//    case "/site/wait-for-tenant.html":
//        var tenantName = Page.getQueryParam("tenantName");
//        var type = Page.getQueryParam("type");
//        var redirect_url = Page.getQueryParam("redirect_url");
//
//        if (tenantName && type) {
//            Initializer.processWaitForTenant(tenantName, type, redirect_url.substring(redirect_url.indexOf("?") + 1));
//        } else {
//            Page.redirect("/");
//        }
//
//        break;
//
    case "/site/auth/create.html":
        var username = Page.getQueryParam("username");
        var bearertoken = Page.getQueryParam("bearertoken");
        var workspace = Page.getQueryParam("workspace");
        var redirect_url = Page.getQueryParam("redirect_url");
        var src_workspace = Page.getQueryParam("src-workspace-id");
        var queryParams = window.location.search.substring(1);

        if (username && bearertoken) {
            SiteApp.authenticate(username, bearertoken);
//            AuthCreate.processCreate(username, bearertoken, src_workspace, workspace, redirect_url, queryParams);
//        } else {
//            Page.redirect("/");
        }

        break;
//
//    case "/site/private/select-tenant.html":
//        if (window.location.search) {
//            SelectWorkspace.process(window.location.search);
//        } else {
//            Page.redirect("/dashboard/");
//        }
//        break;
//
//    default:
//        Page.redirect("/");
//
}

Initializer.setParams(location.search ? location.search.substring(1) : null);
Initializer.setting_up_builders();