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

        var AcceptLicencePage = AccountFormBase.extend({

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
                    email: {
                    	required: true,
                    	email: true,
                    	maxlength: 254,
                    	validEmail: true
                    }
                };
            },

            __submit : function(){
                this.trigger("submitting");
                this.__showProgress();

            }
        });


        return {
            get : function(form){
                if(typeof form === 'undefined'){
                    throw new Error("Need a form");
                }
                return new AcceptLicencePage({el:form});
            },

            AcceptLicencePage : AcceptLicencePage
        };
    }
);
