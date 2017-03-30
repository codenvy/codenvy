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
 
define(["underscore", "views/accountformbase","models/account"],

    function(_,AccountFormBase,Account){

        var ResetPasswordForm = AccountFormBase.extend({

            initialize: function(attributes){
                AccountFormBase.prototype.initialize.apply(this,attributes);
                Account.getBrandingInfo()
                .done(function(Branding){
                    try{
                        document.title = Branding.title + ' | ' + document.title;
                    }catch(err){
                        window.console.error('Branding error. Missing title in product.json');
                    }
                });
            },

            resolveUserEmail : function(){
                Account.verfySetupPasswordId(
                    _.bind(function(errors){
                        if(errors.length !== 0){
                            this.trigger(
                                "invalid",
                                errors[0].getFieldName(),
                                errors[0].getErrorDescription()
                            );
                        }
                    },this)
                );
            },

            __validationRules : function(){
                return {
                    password: {
                        required : true,
                        isValidPassword : true
                    },
                    password1: {
                        required: true,
                        equalTo: "input[name='password']"
                    }
                };
            },

            __submit : function(){
                this.trigger("submitting");
                this.__showProgress();

                Account.setupPassword(
                    this.$("input[name='password']").val(),
                    _.bind(function(d){
                        this.trigger("success",d);
                    },this),
                    _.bind(function(errors){
                        this.__restoreForm();
                        if(errors.length !== 0){
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
                return new ResetPasswordForm({el:form});
            },

            ResetPasswordForm : ResetPasswordForm
        };
    }
);
