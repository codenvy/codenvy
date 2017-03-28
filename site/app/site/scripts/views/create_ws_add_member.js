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
 
define(["jquery","underscore", "backbone", "models/account", "views/branding-base"],

    function($, _, Backbone, Account, BrandingBasePage){
        var CreateWsAddMember = BrandingBasePage.extend({
            
            initialize : function(){
                var bearertoken = Account.getQueryParameterByName("bearertoken"),
                 redirect_url = Account.getQueryParameterByName("redirect_url");
                if (bearertoken) {
                    Account.processCreate(
                        bearertoken,
                        redirect_url,
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
                } else {
                    Account.redirectToUrl("/");
                }

            }
        });

        return {
            get : function(form){
                if(typeof form === 'undefined'){
                    throw new Error("Need a form");
                }

                return new CreateWsAddMember({ el : form });
            }
        };

    }
);
