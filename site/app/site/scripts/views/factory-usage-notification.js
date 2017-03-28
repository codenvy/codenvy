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
 
define(["jquery", "backbone", "models/account", "views/branding-base","validation"],

    function($, Backbone, Account, BrandingBasePage){
        var FactoryNotification = BrandingBasePage.extend({
            
            initialize : function(){
                var continueURL = decodeURI(Account.getQueryParameterByName('redirect_url'));
                if (continueURL !== 'undefined') {
                    $("#continue")
                    .click(
                        function(){
                            var factoryID = Account.getQueryParameterByName('id', decodeURIComponent(continueURL));
                            document.cookie = factoryID + '=accepted; path=/factory/';
                            window.location = continueURL;} //goto 'redirect_url' from search string
                    );
                } else {
                    $("#continue").click(function(){window.location ='/dashboard';}); //goto dashboard if 'redirect_url' is not exists
                }
                $("#cancel").click(function(){window.location = '/dashboard';});// goto dashboard '/dashboard'
            }
        });

        return {
            get : function(form){
                if(typeof form === 'undefined'){
                    throw new Error("Need a form");
                }

                return new FactoryNotification({ el : form });
            }
        };

    }
);
