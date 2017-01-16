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
/**
 * IDE page library namespace.
 */
(function CloudIdeMenuAdditions() {
	
    /** ****************** Private members ********** */
    var IDE_MENU_ADDITIONALS_ID = "ide-menu-additions-rows";
    var IDE_MENU_ITEM_ID = "ideMenuAdditionsItem";
    var IDE_MENU_ITEM_NAME = "IDE";

    var IDE_SUBMENU_CONTAINER_ID = "ideMenuAdditionsSubmenuContainer";
    var IDE_SUBMENU_ID = "ideMenuAdditionsSubmenu";

    var LOGOUT_MENU_ITEM_ID = "logoutButton";
    var LOGIN_MENU_ITEM_ID = "loginButton";
    var SHELL_MENU_ITEM_ID = "shellButton";

    var menuAddition;
    
    /*
     * Appends DIV element to placing additional menu items.
     */
    function addMenuPlaceHolder() {
    	var html = "<div id='ide-menu-additions' align='right' class='ideMenuAdditions'>" +
    	"<table cellspacing='0' cellpadding='0' border='0' class='ideMenuAdditionsTable'>" +
    	"<tr id='ide-menu-additions-rows'></tr>" +
    	"</table>" +
    	"</div>";
    	
    	var div = document.createElement('div');
    	div.innerHTML = html;
    	document.body.appendChild(div.childNodes[0]);
    	
    	var head = document.getElementsByTagName('head')[0];
    	var links = head.getElementsByTagName('link');
    	
    	for (i = 0; i < links.length; i++) {
    		if ("menu-additions.css" == links[i].href) {
    			return;
    		}
    	}
    	
    	var link = document.createElement('link');
    	link.rel = "stylesheet";
    	link.type = "text/css";
    	link.href = "/ide-resources/_app/menu-additions.css";
    	head.appendChild(link);
    	
    }
    
    /** ****************** Private methods ********** */
    function addMenuAddition(html, itemId, clickHandler) {
        var td = document.createElement("td");
        td.innerHTML = html;

        if (itemId) {
            td.id = itemId;
        }

        if (clickHandler) {
            td.onclick = clickHandler;
        }

        menuAddition.appendChild(td);
    }
    
 // Add info for premium user
    function addPremiumUserInfo() {
        var Info = "You have a premium account";
        var premiumInfo=document.createElement("td");
        var textnode=document.createTextNode(Info);
        premiumInfo.id = "premium";
        premiumInfo.appendChild(textnode);
        var existingEl = document.getElementById(IDE_MENU_ADDITIONALS_ID).firstChild;
        document.getElementById(IDE_MENU_ADDITIONALS_ID).insertBefore(premiumInfo,existingEl);
    }
    
    // get Workspace ID
    function getWorkspaceID(wsName){
        var request;
        var url =  "/api/workspace";
        if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
            request = new XMLHttpRequest();
        } else {// code for IE6, IE5
            request = new ActiveXObject("Microsoft.XMLHTTP");
        }
        request.onreadystatechange = function () {
            if (request.readyState == 4 && request.status == 200) {
                response = JSON.parse(request.responseText);
                response.forEach(function(workspase){
                        if (workspase.name === ws){
                            getAccountAttr(workspase.owner.id);
                        }
                    }
                );
            }
        }
        request.open("GET", url, true);
        request.send();
    }

    // get Accounts attributes
    function getAccountAttr(id){
        var request;
        var url =  "/api/account/" + id;
        //var url = window.location.protocol + '//' + window.location.host + '/ws/rest/' + ws + '/configuration/init';
        if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
            request = new XMLHttpRequest();
        } else {// code for IE6, IE5
            request = new ActiveXObject("Microsoft.XMLHTTP");
        }
        request.onreadystatechange = function () {
            if (request.readyState == 4 && request.status == 200) {
                response = JSON.parse(request.responseText);
                if ("Personal Premium" == response.attributes.tariff_plan){
                    // Add info for premium account;
                    addPremiumUserInfo();
                }
            }
        }
        request.open("GET", url, true);
        request.send();
    }

    /** Check if user logged in **/
    function ConstructIDEmenu() {
        var xmlhttp;
        var url = window.location.protocol + '//' + window.location.host + '/ws/rest/' + ws + '/configuration/init';
        if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
            xmlhttp = new XMLHttpRequest();
        } else {// code for IE6, IE5
            xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
        }
        xmlhttp.onreadystatechange = function () {
            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                var response = JSON.parse(xmlhttp.responseText);
                if (response.user.temporary !== true) {
                    // -- add User plan info
                    // (getWorkspaceID -> getAccountAttr -> if response.attributes.tariff_scale -> addPremiumUserInfo)
                    getWorkspaceID(ws);

                    // -- add profile item
                    addMenuAddition("<a href='#'>Profile</a>", null, function (event) {
                        event.preventDefault();
                        window.open("/site/private/profile", "_blank");
                    });
                    
                    // -- add workspace item
                    addMenuAddition("<a href='#'>Workspace</a>", null, function (event) {
                        event.preventDefault();
                        window.location = "/site/private/select-tenant?forced";
                    });

                    // -- add logout item
                    addMenuAddition("<a href='#'>Logout</a>", LOGOUT_MENU_ITEM_ID, function (event) {
                        event.preventDefault();
                        window.location = '/api/auth/logout';
                    });

                } else {
                    // -- add login item
                    addMenuAddition("<a href='#'>Sign In</a>", LOGIN_MENU_ITEM_ID, function (event) {
                        event.preventDefault();
                        if (window.location.search !== "") {
                            window.location.search += "&login";
                        } else {
                            window.location.search += "?login";
                        }

                    });

                }
            }
        }

        xmlhttp.open("GET", url, true);
        xmlhttp.send();
    }

    addMenuPlaceHolder();
    
    menuAddition = document.getElementById(IDE_MENU_ADDITIONALS_ID);
    var htmlShell = "<a id='shell-link' href='/ws/" + ws + "/_app/shell' target='_blank'>Shell</a>";
    addMenuAddition(htmlShell, SHELL_MENU_ITEM_ID);
    ConstructIDEmenu();
    
})();
