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
 
(function(window){
    define(["jquery", "underscore", "views/accountformbase", "models/account", "handlebars", "text!templates/oauthbtn.html", "validation"],

        function($,_,AccountFormBase,Account, Handlebars, oauthTemplate){
            var oauthProviderButtons = ["google", "github"]; // the list of oauth provider buttons
            var SignInForm = AccountFormBase.extend({

                oauthTemplate : Handlebars.compile(oauthTemplate),

                initialize : function(attributes){
                    Account.getBrandingInfo()
                    .done(function(Branding){
                        try{
                            document.title = Branding.title + ' | ' + document.title;
                        }catch(err){
                            window.console.error('Branding error. Missing title in product.json');
                        }
                    });
                    var self = this;
                    AccountFormBase.prototype.initialize.apply(this,attributes);
                    Account.getOAuthproviders(_.bind(self.constructOAuthElements,self));
                },

                __validationRules : function(){
                    var rule = {};
                    rule.username = {required: true};
                    rule.password = {required: true};
                    return rule;
                },

                constructOAuthElements : function(deffer){
                    var self = this;
                    deffer
                    .then(function(providers){
                        _.each(providers,function(provider){
                            if (oauthProviderButtons.indexOf(provider.name) >= 0){
                                self.$(".oauth-list").append(
                                    self.oauthTemplate(provider)
                                );
                                // bind action to oauth button
                                $(".oauth-button." + provider.name).click(function(){
                                    Account.loginWithOauthProvider(provider, "Login page", function(url){
                                        window.location = url;
                                    });
                                });
                            }
                        },this);
                    });
                },

                __submit : function(){
                    Account.processLogin(
                            $(this.el).find("input[name='username']").val().toLowerCase(),
                            $(this.el).find("input[name='password']").val(),
                            Account.getQueryParameterByName('redirect_url'),
                            _.bind(function(errors){
                                if(errors.length !== 0){
                                    $(this.el).find("input[name='password']").val("");
                                    $(this.el).find("input[name='password']").focus();
                                    this.trigger(
                                        "invalid",
                                        errors[0].getFieldName(),
                                        errors[0].getErrorDescription()
                                    );
                                }
                            },this)
                        );
                }

            });

            return {
                get : function(form){
                    if(typeof form === 'undefined'){
                        throw new Error("Need a form");
                    }

                    return new SignInForm({
                        el : form
                    });
                },

                SignInForm : SignInForm
            };
        }
    );
}(window));
