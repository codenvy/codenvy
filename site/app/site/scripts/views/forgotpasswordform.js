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
 
define(["jquery","underscore","views/accountformbase","models/account"],

    function($,_,AccountFormBase,Account){

        var ForgotPasswordForm = AccountFormBase.extend({
            initialize : function(attributes){
                AccountFormBase.prototype.initialize.apply(this,attributes);
                Account.getUserSettings() //get user props
                .then(function(settings){
                    if (settings["che.auth.user_self_creation"] === "true") {
                            //show create-account link
                        $('#bottom-ul').prepend('<li><a href="/">Create a New Account</a></li>');
                    }
               })
                .fail();
            },

            __submit : function(){
                this.__showProgress();
                this.trigger("submitting");

                Account.recoverPassword(
                    this.$("input[name='email']").val(),
                    _.bind(function(){
                        this.__showResultMessage();
                    },this),
                    _.bind(function(errors){

                        this.__restoreForm();

                        if(errors.length > 0){
                            this.trigger(
                                "invalid",
                                errors[0].getFieldName(),
                                errors[0].getErrorDescription()
                            );
                        }
                    },this)
                );
                return false;
            },

            __showResultMessage : function(){
                $(".notice").addClass("hide");
				$(".forgotpassword-form").addClass("hide");
				$(".forgotpassword-result").removeClass("hide");
            }
        });

        return {
            get : function(form){
                if(typeof form === 'undefined'){
                    throw new Error("Need a form");
                }

                return new ForgotPasswordForm({ el : form });
            },

            ForgotPasswordForm : ForgotPasswordForm
        };

    }
);
