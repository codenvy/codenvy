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

define(["jquery", "underscore", "backbone", "models/account", "validation"],

    function($,_,Backbone,Account){

        var Form = Backbone.View.extend({

            initialize : function(){
                Account.isApiAvailable()
                .then(function(apiAvailable){
                    if (!apiAvailable){
                        window.location = "/site/maintenance";
                    }
                });

                // eagerly fetch csrf token
                this.csrfToken = Account.getCsrfToken();

                this.getBranding = Account.getBrandingInfo()
                .done(function(Branding){
                    try{
                        document.title = Branding.title + ' | ' + document.title;
                    }catch(err){
                        window.console.error('Branding error. Missing title in product.json');
                    }
                });
                $(this.el).on('submit', function(e){
                    e.preventDefault();
                });

                this.validator = $(this.el).validate({
                    rules: this.__validationRules(),
                    messages: this.__validationMessages(),
                    onfocusout : false, onkeyup : false,
                    submitHandler: _.bind(this.__submit,this),
                    showErrors : _.bind(function(errorMap, errorList){
                        this.__showErrors(errorMap, errorList);
                    },this)
                });
            },

            __validationRules : function(){
                throw new Error("Not implemented");
            },

            __validationMessages : function(){
                throw new Error("Not implemented");
            },

            __showErrors : function(){
                throw new Error("Not implemented");
            },

            __restoreForm : function(){
                throw new Error("Not implemented");
            },

            __showProgress : function(){
                throw new Error("Not implemented");
            }
        });

        return Form;
    }
);
