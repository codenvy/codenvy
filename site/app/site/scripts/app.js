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
        "views/accept-fair-source-licence"
        ],

    function($,Config,
        ForgotPasswordForm,
        ResetPasswordForm,
        ErrorReport,
        CreateWsAdd_Member,
        OnPremisesLogin,
        FactoryUsageNotification,
        MainPage,
        AcceptLicencePage){

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
                    var uvOptions = {}; //UserVoice object
                    
                    if (uvOptions){}
                    var forgotPasswordForm = $(".forgotpassword-form"),
                        resetPasswordForm = $(".resetpassword-form"),
                        errorContainer = $(".error-container"),
                        creatWsAddMember = $(".create-ws-add-memeber"),
                        onpremloginForm = $(".onpremloginForm"),
                        factoryUsageNotification =  $(".factory-notification"),
                        mainpage = $(".main-page"),
                        acceptLicencePage = $(".accept-licence-form");

                    if(acceptLicencePage.length !== 0){
                        (function(){
                            var form = AcceptLicencePage.get(acceptLicencePage);
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
