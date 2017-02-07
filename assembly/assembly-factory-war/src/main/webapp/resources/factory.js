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
if (!window["codenvy-factories"]) {

    window["codenvy-factories"] = new Array();

    window.addEventListener("message", function(event) {
        try {
            // It must be the request for resize Factory button.
            if (event.data.indexOf("resize-factory-button:") != 0) {
                return;
            }

            // Parse message and resize Factory Button.
            var parts = event.data.split(':');

            // resize all, which ID is equal to required
            for (var i = 0; i < window["codenvy-factories"].length; i++) {
                var iframe = window["codenvy-factories"][i];
                if (iframe["factory"] && iframe["factory"] === parts[1] &&
                    iframe["uid"] && iframe["uid"] === parts[2]) {
                    iframe.style.width = "" + parts[3] + "px";
                    iframe.style.height = "" + parts[4] + "px";
                }
            }
        } catch (e) {
            console.log(e.message);
        }
    }, false);


    var ButtonInjector = new function() {

        /*
         * Find all scripts and check everyone for factory button parameters.
         */
        this.go = function() {
            var scripts = document.getElementsByTagName('script');
            for (var i = 0; i < scripts.length; i++) {
                var script = scripts.item(i);
                try {
                    var src = script.getAttribute("src");
                    if ((src && src.indexOf("resources/factory.js") >= 0) || (src && src.indexOf("site/factory.js") >= 0)) {
                        if (script.src.indexOf("/factory.js?") >= 0) {
                            ButtonInjector.injectEncoded(script);
                        } else {
                            ButtonInjector.injectNonencoded(script);
                        }
                    }
                } catch (e) {
                    console.log(e.message);
                }
            }
        };

        /*
         * Inject a button for encoded factory.
         */
        this.injectEncoded = function(script) {
            var factory = script.src.substring(script.src.indexOf('?') + 1);
            var uid = "" + Math.random();

            var frameParams = "factory=" + factory + "&uid=" + uid;

            var style = script.getAttribute("style");
            if (style) {
                frameParams += "&style=" + style.toLowerCase();
            }

            var counter = script.getAttribute("counter");
            if (counter) {
                frameParams += "&counter=" + counter.toLowerCase();
            }

            var logo = script.getAttribute("logo");
            if (logo) {
                frameParams += "&logo=" + encodeURIComponent(logo);
            }

            var frame = ButtonInjector.injectFrame(frameParams, script);
            frame.factory = factory;
            frame.uid = uid;
            window["codenvy-factories"].push(frame);
        };

        /*
         * Inject a button for nonencoded factory.
         */
        this.injectNonencoded = function(script) {
            var style = script.getAttribute("style");
            if (!style) {
                return;
            }
            style = style.toLowerCase();

            var frameParams = "style=" + style;

            var url = script.getAttribute("url");
            if (url) {
                frameParams += "&url=" + encodeURIComponent(url);
            }

            var logo = script.getAttribute("logo");
            if (logo) {
                frameParams += "&logo=" + encodeURIComponent(logo);
            }

            var frame = ButtonInjector.injectFrame(frameParams, script);

            if (style.indexOf("advanced") >= 0) {
                frame.style.width = "112px";
                frame.style.height = "113px";
            } else if (style.indexOf("horizontal") >= 0) {
                frame.style.width = "118px";
                frame.style.height = "21px";
            } else if (style.indexOf("vertical") >= 0) {
                frame.style.width = "77px";
                frame.style.height = "61px";
            } else if (style.indexOf("white") >= 0 || style.indexOf("dark") >= 0 || style.indexOf("gray") >= 0) {
                frame.style.width = "77px";
                frame.style.height = "21px";
            }
        };

        /*
         * Inject frame in which a factory button will be displatyed.
         */
        this.injectFrame = function(frameParams, script) {
            var frameURL = script.src.substring(0, script.src.lastIndexOf("/")) + "/factory.html?" + frameParams;

            var frame = document.createElement("iframe");
            frame.src = frameURL;
            frame.style.width = "0px";
            frame.style.height = "0px";

            // Style attributes
            frame.style.background = "transparent";
            frame.style.border = "0px none transparent";
            frame.style.padding = "0px";
            frame.style.overflow = "hidden";

            // Properties
            frame.scrolling = "no";
            frame.frameborder = "0";
            frame.allowtransparency = "true";

            setTimeout(function() {
                script.parentNode.replaceChild(frame, script);
            }, 10);

            return frame;
        };

    };

    setTimeout(ButtonInjector.go, 100);

}
