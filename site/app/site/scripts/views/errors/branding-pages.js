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
 
define(["jquery", "models/account", "views/errors/base_error"],

    function($, Account, BaseErrorPage){
        var Error400 = BaseErrorPage.extend({

        });


        return {
            get : function(form, pageName){
                if(typeof form === 'undefined'){
                    throw new Error("Need a form");
                }
                return new Error400({el:form, pageName:pageName});
            },

            Error400 : Error400
        };
    }
);
