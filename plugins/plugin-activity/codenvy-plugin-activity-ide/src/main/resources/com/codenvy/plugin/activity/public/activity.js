/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
var ActivityTracker = new function () {

    var url;
    var timeoutInterval = 30000;
    var maxErrors = 10;
    var errors = 0;
    var active;
    var csrfToken;

    this.init = function (restContext, workspaceId) {
        this.url = restContext + "/activity/" + workspaceId;
        document.addEventListener("mousemove",  ActivityTracker.setActive);
        document.addEventListener("keypress", ActivityTracker.setActive);
        ActivityTracker.requestCsrfToken(restContext,
                function() {
                    setInterval(ActivityTracker.sendRequest, timeoutInterval);
                },
                function() {
                    console.log("Could not fetch X-CSRF-Token, activity tracking won't be started");
                });
    };

    this.setActive = function() {
        if (!active && errors < maxErrors) {
            active = true;
        }
    }


    this.sendRequest = function () {
        if (!active) {
            return;
        }
        active = false;

        var request;
        if (window.XMLHttpRequest) {
            request = new XMLHttpRequest();
        } else {
            request = new ActiveXObject("Microsoft.XMLHTTP");
        }

        request.onreadystatechange = function () {
            if (request.readyState == 4) {
                if (request.status == 204) {
                    errors = 0;
                } else {
                    errors++;
                }

            }
        };
        request.open("PUT", ActivityTracker.url, true);
        if (csrfToken) {
            request.setRequestHeader("X-CSRF-Token", csrfToken);
        }
        request.send();
    };

    this.requestCsrfToken = function(restContext, tokenPresentHandler, tokenMissingHandler) {
        var xhr = new XMLHttpRequest();

        xhr.onreadystatechange = function() {
            if (xhr.readyState == XMLHttpRequest.DONE) {
                csrfToken = xhr.getResponseHeader("X-CSRF-Token");
                if (csrfToken) {
                    tokenPresentHandler(csrfToken);
                } else {
                    tokenMissingHandler();
                }
            }
        };

        xhr.open("GET", restContext + "/profile");
        xhr.setRequestHeader("X-CSRF-Token", "Fetch");
        xhr.send();
    }
};
