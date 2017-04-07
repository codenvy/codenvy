/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
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
(function(window) {
    var _gaq = _gaq || [];
    define(["jquery", "underscore", "json", "cookies"], function($, _, JSON) {
        /*
            AccountError is used to report errors through error callback function
            (see details below ). Example usage:

            new AccountError("password","Your password is too short")

        */
        var isWebsocketEnabled = function() {
            var USER_AGENT = navigator.userAgent.toLowerCase();
            if (USER_AGENT.indexOf("chrome") === -1 && USER_AGENT.indexOf("firefox") === -1 && USER_AGENT.indexOf("safari") === -1) {
                window.location = "/site/error/browser-not-supported#show";
                return false;
            }
            var IS_WS_SUPPORTED = ("WebSocket" in window);
            if (!IS_WS_SUPPORTED) {
                window.location = "/site/error/websocket-connection-error";
                return false;
            }
            return true;
        };

        var AccountError = function(fieldName, errorDescription) {
            return {
                getFieldName: function() {
                    return fieldName;
                },
                getErrorDescription: function() {
                    return errorDescription;
                }
            };
        };

        var getQueryParameterByName = function(name, queryString) {
            if (typeof queryString === 'undefined') {
                queryString = window.location.search;
            }
            name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
            var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
            var results = regex.exec(queryString);
            if (results) {
                return decodeURIComponent(results[1].replace(/\+/g, " "));
            }
        };

        var loginWithOauthProvider = function(provider, page, callback) {
            if (isWebsocketEnabled()) {
                // build oauth url
                var oauthUrl, scope;
                _.each(provider.links, function(link){
                    if (link.rel==="Authenticate URL"){
                        oauthUrl = link.href + "?";
                        _.each(link.parameters, function(param){
                            oauthUrl = oauthUrl+ param.name + "=" + param.defaultValue + "&";
                        });
                    }
                });
                var pageUrl = "&page_url=" + window.location.pathname;
                var redirectAfterLogin = "&redirect_after_login=" + encodeURIComponent(window.location.origin + "/api/oauth?" + window.location.search.substring(1) + encodeURIComponent(window.location.hash) + (window.location.search ? '&' : '') + 'oauth_provider=' + provider.name + pageUrl + window.location.hash);
                switch (provider.name) {
                    case "google":
                        _gaq.push(['_trackEvent', 'Regisration', 'Google registration', page]);
                        scope = "scope=https://www.googleapis.com/auth/userinfo.profile&scope=https://www.googleapis.com/auth/userinfo.email";
                        break;
                    case "github":
                        _gaq.push(['_trackEvent', 'Regisration', 'GitHub registration', page]);
                        scope = "scope=user,repo,write:public_key";
                        break;
                    case "microsoft":
                        scope = "scope=vso.code_manage vso.code_status";
                        break;
                    case "redhat":
                        scope = "scope=user";
                        break;
                    case "linkedin":
                        scope = "scope=r_basicprofile r_emailaddress";
                        break;
                }
                oauthUrl = oauthUrl + scope + redirectAfterLogin;
                if (typeof callback !== 'undefined') {
                    callback(oauthUrl);
                }
            }
        };

        /*
            Every method accepts 0 or more data values and two callbacks (success and error)

            Success callback spec:

                function success(data){
                    // data includes any additonal data you want to pass back
                }

            Error callback spec:

                function error(errors){
                    // errors is a list of AccountError instances
                }

        */
        var removeCookie = function(cookie) {
            if ($.cookie(cookie)) {
                $.removeCookie("cookie", {
                    path: "/"
                });
            }
        };
        var authenticate = function(bearertoken) {
            var deferredResult = $.Deferred();
            var data = {
                token: bearertoken
            };
            var authenticateUrl = "/api/internal/token/authenticate";
            $.ajax({
                url: authenticateUrl,
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(data)
            })
            .success(function(response){
                deferredResult.resolve(response);
            })
            .error(function(error){
                deferredResult.reject(error);
            });
            return deferredResult;
        };

        var getOwnAccount = function(accounts){
            var ownAccount = {accountReference:{id:false}};
            if (accounts.length > 0) {
                accounts.forEach(function(account) {
                    if (account.roles.indexOf('account/owner') >= 0){
                        ownAccount = account;
                    }
                });
            }
            return ownAccount;
        };
        // Verify api response. Returns false if response does not contain "implementationVendor" field
        var isApiAvailable = function(){
            var deferredResult = $.Deferred();
            var url = "/api/";
            $.ajax({
                url: url,
                type: "OPTIONS",
                success: function(response){
                    if (response.implementationVendor) {//response.implementationVendor == "Codenvy, S.A."
                        deferredResult.resolve(true);
                    }else{
                        deferredResult.resolve(false);
                    }
                },
                error: function(){
                    deferredResult.resolve(false);
                }
            });
            return deferredResult;
        };

        var getProfilePrefs = function(){
            var deferredResult = $.Deferred();
            var url = "/api/profile/prefs";
            $.ajax({
                url: url,
                type: "GET",
                success: function(response){
                    deferredResult.resolve(response);
                },
                error: function(response){
                    deferredResult.reject(response);
                }
            });
            return deferredResult;
        };

        var createAccount = function(accountName) {
            var deferredResult = $.Deferred();
            var url = "/api/account";
            var data = {
                name: accountName
            };
            $.ajax({
                url: url,
                type: "POST",
                data: JSON.stringify(data),
                contentType: "application/json"
            })
            .success(function(response){
                deferredResult.resolve(response);
            })
            .error(function(error){
                deferredResult.reject(error);
            });
            return deferredResult;
        };

        var ensureExistenceAccount = function(accountName) {
            var deferredResult = $.Deferred();
            var url = "/api/account";
            $.ajax({
                url: url,
                type: "GET",
                success: function(membership) {
                    var account = getOwnAccount(membership);
                    if (account.accountReference.id){
                        deferredResult.resolve(account.accountReference, false); //returns Account
                    }
                    else {
                        // user hasn't memberships
                        createAccount(accountName)
                        .fail(function(error){deferredResult.reject(error);})
                        .then(function(account){
                        deferredResult.resolve(account, true);
                        });

                    }
                },
                error: function(error){
                    deferredResult.reject(error);
                }
            });
            return deferredResult;
        };

        var logout = function() {
            if (isWebsocketEnabled()) {
                var loginUrl = "/api/auth/logout";
                return $.ajax({
                    url: loginUrl,
                    type: "POST"
                });
            }
        };

        var login = function(email, password) {
            if (isWebsocketEnabled()) {
                var loginUrl = "/api/auth/login?" + window.location.search.substring(1);
                var data = {
                    username: email,
                    password: password
                };
                return $.ajax({
                    url: loginUrl,
                    type: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                });
            }
        };

        var redirectToUrl = function(url) {
            window.location = url;
        };

        var navigateToLocation = function(){
            var redirect_url = "/dashboard/";
            redirectToUrl(redirect_url);
        };
        //TODO api returns list of oAuth providers
        var getOAuthproviders = function(success) {
            var deferredResult = $.Deferred();
            var url = "/api/oauth/";
            $.ajax({
                url: url,
                type: "GET"
            })
            .success(function(response){
                success(deferredResult.resolve(response));
            })
            .error(function(error){
                deferredResult.reject(error);
            });
            return deferredResult;
        };

        var getResponseMessage = function(response){
            var responseErr;
            try{
                responseErr = JSON.parse(response.responseText).message;
            }catch(e){
                console.log(e);
                responseErr = "Something went wrong. Please try again or contact support.";
            }
            return responseErr;
        };

        var acceptLicense = function(){
            var deferredResult = $.Deferred();
            var acceptLicenseUrl = "/api/license/system/fair-source-license";
            $.ajax({
                url: acceptLicenseUrl,
                type: "POST"
            })
            .success(function(response){
                deferredResult.resolve(response);
            })
            .error(function(error){
                deferredResult.reject(getResponseMessage(error));
            });
            return deferredResult;
        };

        var appendQuery = function(url) {
            return url  +  window.location.search + window.location.hash;
        };

        var getUserSettings = function() {
            var deferredResult = $.Deferred();
            var url = "/api/user/settings";
            $.ajax({
                url: url,
                type: "GET"
            })
            .success(function(response){
                deferredResult.resolve(response);
            })
            .error(function(){
                deferredResult.resolve({});
            });
            return deferredResult;
        };

        return {
            removeCookie: removeCookie,
            isWebsocketEnabled: isWebsocketEnabled,
            getQueryParameterByName: getQueryParameterByName,
            appendQuery: appendQuery,
            AccountError: AccountError,
            authenticate: authenticate,
            ensureExistenceAccount: ensureExistenceAccount,
            getOwnAccount: getOwnAccount,
            isApiAvailable: isApiAvailable,
            getOAuthproviders: getOAuthproviders,
            loginWithOauthProvider: loginWithOauthProvider,
            getUserSettings: getUserSettings,
            acceptLicense: acceptLicense,
            logout: logout,
            isValidDomain: function(domain) {
                return (/^[a-z0-9][a-z0-9_.-]{2,19}$/).exec(domain) !== null;
            },
            isValidEmail: function(email) {
                return (/^[^\+\/]+$/).test(email);
            },
            //Password must contain at least one letter, at least one number, and be longer than 8 charaters, and shorter than 100.
            isValidPassword: function(value) {
                return (/^(?=.*[0-9]+.*)(?=.*[a-zA-Z]+.*).{8,100}$/).test(value);
            },

            getBrandingInfo: function() {
                return $.getJSON("/site/branding/product.json")
                .fail(function(error){
                    switch (error.status) {
                        case 404:
                            window.console.error('Branding: product.json not found.');
                            break;
                        case 200:
                            window.console.error('JSON parse error. Valid product.json needed.');
                            break;
                        default:
                            window.console.error('Branding error. Something went wrong');
                    }
                });
            },

            isUserAuthenticated: function() {
                return getProfilePrefs()
                    .then(function(prefs){
                            if (prefs.temporary){
                                try {
                                    var temporary = JSON.parse(prefs.temporary);
                                    if (temporary===true){
                                        return false; // anonymous user
                                    }else{
                                        return true;
                                    }
                                }catch(err) {
                                    return false;
                                }
                            } else{
                                return true;
                            }
                    })
                    .fail(function(){
                        return false;
                    });
            },

            navigateToLocation: navigateToLocation,

            // Login with email and password
            processLogin: function(email, password, redirect_url, error){
                login(email.toLowerCase(), password)
                .then(function(){
                    if (!redirect_url){
                        redirectToUrl("/dashboard/");
                    } else {
                        redirectToUrl(redirect_url);
                    }
                })
                .fail(function(response) {
                        if (response){
                            error([
                                new AccountError(null, getResponseMessage(response))
                            ]);
                        }
                    }
                );
            },

            // signup, oAuth login,
            processCreate: function(bearertoken, redirect_url,  error) {
                authenticate(bearertoken)
                .then(function(){
                    if (!redirect_url){
                        redirectToUrl("/dashboard/");
                    } else {
                        redirectToUrl(redirect_url);
                    }
                })
                .fail(function(response) {
                        if (response){
                            error([
                                new AccountError(null, getResponseMessage(response))
                            ]);
                        }
                    }
                );
            },

            createTenant: function(email, username, error) {
                var data = {
                    email: email.toLowerCase(),
                    username: username.toLowerCase()
                };
                var emailValidateUrl = "/api/internal/token/validate?" + window.location.search.substring(1);
                $.ajax({
                    url: emailValidateUrl,
                    type: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                    success: function() {
                        redirectToUrl('../site/thank-you');
                    },
                    error: function(response /*, status , err*/ ) {
                        error([
                            new AccountError(null, getResponseMessage(response))
                        ]);
                    }
                });
            },

            //--------------------------------- Recover password module
            // Send recover password request
            recoverPassword: function(email, success, error) {
                var passwordRecoveryUrl = "/api/password/recover/" + email;
                $.ajax({
                    url: passwordRecoveryUrl,
                    type: "POST",
                    data: {},
                    success: function() {
                        success();
                    },
                    error: function(response) {
                        error([
                            new AccountError(null, getResponseMessage(response))
                        ]);
                    }
                });
            },
            // Verify reset password id
            verfySetupPasswordId: function(error) {
                var verifySetupPasswordIdUrl = "/api/password/verify",
                    id = getQueryParameterByName("id");
                if (typeof id === 'undefined') {
                    error([
                        new AccountError(null, "Invalid password reset url")
                    ]);
                    return;
                }
                $.ajax({
                    url: verifySetupPasswordIdUrl + "/" + id,
                    type: "GET",
                    success: function() {
                     },
                    error: function(response) {
                        setTimeout(function() {
                            window.location = "/site/recover-password";
                        }, 10000);
                        error([
                            new AccountError(null, getResponseMessage(response) + ".<br>You will be redirected in 10 sec")
                        ]);
                    }
                });
            },
            // Setup new password
            setupPassword: function(password, success, error) {
                // implementation based on this:
                // https://github.com/codenvy/cloud-ide/blob/master/cloud-ide-war/src/main/webapp/js/setup-password.js
                // We assume that uid is part of the url :
                //  https://codenvy.com/pages/setup-password?id=df3c62fe-1459-48af-a4a0-d0c1cc17614a
                var setupPasswordUrl = "/api/password/setup",
                    id = getQueryParameterByName("id");
                $.ajax({
                    url: setupPasswordUrl,
                    type: "POST",
                    data: {
                        uuid: id,
                        password: password
                    },
                    success: function() {
                        redirectToUrl("/site/login");
                    },
                    error: function(response) {
                        error([
                            new AccountError(null, getResponseMessage(response))
                        ]);
                    }
                });
            },
            //--------------------------------- The end of Recover password module
            /**
             * Encode all special characters including ~!*()'. Replace " " on "+"
             * @see http://xkr.us/articles/javascript/encode-compare/
             * @param {Object} string
             */
            encodeSpecialSymbolsForPost: function(string) {
                if (string) {
                    string = encodeURIComponent(string);
                    string = string.replace(/~/g, escape("~"));
                    string = string.replace(/!/g, escape("!"));
                    string = string.replace(/\*/g, escape("*"));
                    string = string.replace(/[(]/g, escape("("));
                    string = string.replace(/[)]/g, escape(")"));
                    string = string.replace(/'/g, escape("'"));
                    string = string.replace(/%20/g, escape("+"));
                }
                return string;
            },
            /**
             * Escape special symbols from user input
             * @param string
             * @returns
             */
            escapeSpecialSymbols: function(string) {
                if (string) {
                    string = string.replace(/\n/g, "\\n");
                    string = string.replace(/\r/g, "\\r");
                    string = string.replace(/\t/g, "\\t");
                    string = string.replace(/[\b]/g, "\\b");
                    string = string.replace(/\f/g, "\\f");
                    string = string.replace(/\\/g, "\\\\");
                    string = string.replace(/\"/g, "\\\"");
                }
                return string;
            }
        };
    });
}(window));
