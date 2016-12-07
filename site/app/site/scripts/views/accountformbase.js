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
 
define(["jquery","models/account","views/form","validation"],

    function($,Account,Form){

        /*
            Custom validator for codenvy.com workspace names
        */

        jQuery.validator.addMethod("validDomain", function(value) {
            return Account.isValidDomain(value.toLowerCase());
        });
        jQuery.validator.addMethod("checkEmail", function(value) {
            return Account.isValidEmail(value);
        });
        jQuery.validator.addMethod("isValidPassword", function(value) {
            return Account.isValidPassword(value);
        });
        var AccountFormBase = Form.extend({

            settings : {
                noDomainErrorMessage : "Please specify a workspace name",
                noUsernameErrorMessage : "We have not detected a valid user name",
                noEmailErrorMessage : "Please provide a user name",
                noPasswordErrorMessage : "Please provide your password",
                noConfirmPasswordErrorMessage : "Please type your new password again. Both passwords must match.",
                invalidEmailErrorMessage : "Emails with '+' and '/' are not allowed",
                invalidDomainNameErrorMessage : "Your workspace name should start with a Latin letter or a digit, and must only contain Latin letters, digits, underscores, dots or dashes. You are allowed to use from 3 to 20 characters in a workspace name.",
                notSecuredPassword : "Password should contain between 8-100 characters, both letters and digits",
                invalidUserName : "Please enter no more than 35 characters.",
                noFirstNameErrorMessage : "Please enter your first name",
                noLastNameErrorMessage : "Please enter your last name",
            },

            __validationRules : function(){
                return {
                    email: {
                        required : true,
                        checkEmail : true
                    }
                };
            },

            __validationMessages : function(){
                return {
                    email: {
                        required : this.settings.noUserNameErrorMessage,
                        checkEmail : this.settings.invalidEmailErrorMessage
                    },
                    password: {
                        required: this.settings.noPasswordErrorMessage,
                        isValidPassword:this.settings.notSecuredPassword
                    },
                    password1: {
                        required: this.settings.noConfirmPasswordErrorMessage,
                        equalTo: this.settings.noConfirmPasswordErrorMessage
                    },
                    firstName: {
                        required: this.settings.noFirstNameErrorMessage,
                        maxlength: this.settings.invalidUserName,
                    },
                    lastName: {
                        required: this.settings.noLastNameErrorMessage,
                        maxlength: this.settings.invalidUserName,
                    }
                };
            },

            __showErrors : function(errorMap){
                function refocus(el){
                    el.focus();
                }
                /*this.validator.defaultShowErrors();*/

                if(typeof errorMap.email !== 'undefined'){
                    this.trigger("invalid","email",errorMap.email);
                    refocus(this.$("input[name='email']"));
                    return;
                }

                if(typeof errorMap.domain !== 'undefined'){
                    this.trigger("invalid","domain",errorMap.domain);
                    refocus(this.$("input[name='email']"));
                    return;
                }

                if(typeof errorMap.password !== 'undefined'){
                    this.trigger("invalid","password",errorMap.password);
                    refocus(this.$("input[name='password']"));
                    return;
                }

                if(typeof errorMap.password1 !== 'undefined'){
                    this.trigger("invalid","password1",errorMap.password1);
                    refocus(this.$("input[name='password1']"));
                    return;
                }

                if(typeof errorMap.change_password !== 'undefined'){
                    this.trigger("invalid","change_password",errorMap.change_password);
                    refocus(this.$("input[name='change_password']"));
                    return;
                }

                if(typeof errorMap.confirm_password !== 'undefined'){
                    this.trigger("invalid","confirm_password",errorMap.confirm_password);
                    refocus(this.$("input[name='confirm_password']"));
                    return;
                }

                if(typeof errorMap.firstName !== 'undefined'){
                    this.trigger("invalid","firstName",errorMap.firstName);
                    refocus(this.$("input[name='firstName']"));
                    return;
                }

                if(typeof errorMap.lastName !== 'undefined'){
                    this.trigger("invalid","lastName",errorMap.lastName);
                    refocus(this.$("input[name='lastName']"));
                    return;
                }                

            },

            __restoreForm : function(){
                this.$("input[type='submit']").removeAttr("disabled");
            },

            __showProgress : function(){
                this.$("input[type='submit']").attr("disabled","disabled");
            }
        });

        return AccountFormBase;
    }
);
