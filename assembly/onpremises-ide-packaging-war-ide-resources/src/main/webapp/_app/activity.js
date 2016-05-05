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
var ActivityTracker = new function () {

    var url;
    var timeoutInterval = 30000;
    var maxErrors = 10;
    var errors = 0;
    var active;

    this.init = function (restContext, workspaceId) {
        this.url = restContext + "/activity/" + workspaceId;
        document.addEventListener("mousemove",  ActivityTracker.setActive);
        document.addEventListener("keypress", ActivityTracker.setActive);
        setInterval(ActivityTracker.sendRequest, timeoutInterval);
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
        request.send();
    };

};
