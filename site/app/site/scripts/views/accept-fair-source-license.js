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
 
define(["jquery","underscore", "views/accountformbase","models/account"],

    function($,_,AccountFormBase,Account){
        var AcceptLicensePage = AccountFormBase.extend({

        	initialize : function(attributes){
        		AccountFormBase.prototype.initialize.apply(this,attributes);
        	},

            __validationRules : function(){
                return {
                    firstName: {
                        required : true,
                        maxlength: 35
                    },
                    lastName: {
                        required: true,
                        maxlength: 35
                    },
                    adminEmail: {
                    	required: true,
                    	email: true,
                        maxlength: 254
                    }
                };
            },

            __submit : function(){
                var self = this;
                this.trigger("submitting");
                this.__showProgress();
                Account.acceptLicense()
                .then(function(success){
                    window.console.log(success);
                    
                    Account.navigateToLocation();
                })
                .fail(function(error){
                    self.trigger("invalid",null, error);
                    window.console.log(error);
                    self.__restoreForm();
                })
                ;

            }
        });


        return {
            get : function(form){
                if(typeof form === 'undefined'){
                    throw new Error("Need a form");
                }
                return new AcceptLicensePage({el:form});
            },

            AcceptLicensePage : AcceptLicensePage
        };
    }
);
