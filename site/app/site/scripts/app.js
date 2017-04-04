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
 
define(["jquery","config",
        "views/forgotpasswordform",
        "views/resetpasswordform",
        "views/errorreport",
        "views/create_ws_add_member",
        "views/onpremises-login",
        "views/factory-usage-notification",
        "views/login",
        "views/accept-fair-source-license",
        "views/errors/branding-pages"
        ],

    function($,Config,
        ForgotPasswordForm,
        ResetPasswordForm,
        ErrorReport,
        CreateWsAdd_Member,
        OnPremisesLogin,
        FactoryUsageNotification,
        MainPage,
        AcceptLicensePage,
        BrandingPages){

        function modernize(){
            Modernizr.load({
                // HTML5 placeholder for input elements
                test : Modernizr.input.placeholder,
                nope : Config.placeholderPolyfillUrl,
                complete : function(){
                    if(typeof $.fn.placeholder !== 'undefined'){
                        $('input, textarea').placeholder();
                    }
                }
            });
        }

        return {
            run : function(){
                $(document).ready(function(){

                    modernize();
                    var brandingPages = {// product.json file contains branding data for these pages. pageName is used as the key.
                        1:{class:".400",pageName:"error400"},
                        2:{class:".401",pageName:"error401"},
                        3:{class:".403",pageName:"error403"},
                        4:{class:".404",pageName:"error404"},
                        5:{class:".405",pageName:"error405"},
                        6:{class:".500",pageName:"error500"},
                        7:{class:".503",pageName:"error503"},
                        8:{class:".504",pageName:"error504"},
                        9:{class:".browser-not-supported",pageName:"BrowserNotSupported"},
                        10:{class:".error-cookies-disabled",pageName:"YourCookiesAreDisabled"},
                        11:{class:".error-factory-creation",pageName:"FactoryWorkspaceCreationFailed"},
                        12:{class:".error-tenant-name",pageName:"WorkspaceDoesNotExist"},
                        13:{class:".maintenance",pageName:"Maintenance"},
                        14:{class:".fair-source-license-is-not-accepted-error",pageName:"AccessRequiresLicenseAcceptance"},
                        15:{class:".no-account-found", pageName:"NoAccountFound"},
                        16:{class:".websocket-connection-error", pageName:"WebsocketConnectionError"}};

                    var forgotPasswordForm = $(".forgotpassword-form"),
                        resetPasswordForm = $(".resetpassword-form"),
                        errorContainer = $(".error-container"),
                        creatWsAddMember = $(".create-ws-add-memeber"),
                        onpremloginForm = $(".onpremloginForm"),
                        factoryUsageNotification =  $(".factory-notification"),
                        mainpage = $(".main-page"),
                        acceptLicensePage = $(".accept-license-form");

                    $.each(brandingPages, function(i,val){
                        var page = $(val.class);
                        if (page.length !== 0) {
                            (function(){
                                BrandingPages.get(page, val.pageName);
                            }());
                        }
                    });

                    if(acceptLicensePage.length !== 0){
                        (function(){
                            var form = AcceptLicensePage.get(acceptLicensePage),
                            errorReport = ErrorReport.get(errorContainer);
                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });
                            form.on("submitting", function(){
                                errorReport.hide();
                            });
                        }());

                    }

                    if(factoryUsageNotification.length !== 0){
                        (function(){
                            FactoryUsageNotification.get(factoryUsageNotification);
                        }());
                        
                    }

                    if(onpremloginForm.length !== 0){
                        (function(){
                            var form = OnPremisesLogin.get(onpremloginForm),
                            errorReport = ErrorReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
                            });

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });
                            
                        }());
                    }

                    if(creatWsAddMember.length !== 0){
                        (function(){
                            var form = CreateWsAdd_Member.get(creatWsAddMember),
                            errorReport = ErrorReport.get(errorContainer);

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });
                            
                        }());
                    }

                    if(forgotPasswordForm.length !== 0){
                        (function(){
                            var form = ForgotPasswordForm.get(forgotPasswordForm),
                            errorReport = ErrorReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
                            });

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });


                        }());
                    }

                    if(resetPasswordForm.length !== 0){
                        (function(){

                            var form = ResetPasswordForm.get(resetPasswordForm),
                                errorReport = ErrorReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
                            });

                            form.on("success", function(d){
                                window.location.href = d.url;
                            });

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });

                            form.resolveUserEmail();

                        }());
                    }


                    if(mainpage.length !== 0){
                        (function(){
                            var form = MainPage.get(mainpage);
                            form.on("invalid", function(field,message,errorContainer){
                                var errorReport = ErrorReport.get(errorContainer);
                                errorReport.show(message);
                            });
                        }());
                        
                    }
                    
                });
            }
        };

    }
);
