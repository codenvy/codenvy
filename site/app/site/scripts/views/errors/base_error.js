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
 
 define(['jquery','backbone',"models/account"],
 	function($, Backbone, Account){
 		var BaseErrorPage = Backbone.View.extend({
 			initialize : function(options){
                var self = this;
                Account.getBrandingInfo()
                .done(function(Branding){
                    try{
                        document.title = Branding.errorpage[options.pageName].title;
                    }catch(err){
                        window.console.error('Branding error. Missing title for ' + options.pageName + ' in product.json');
                    }
                    try{
                        $('.header').html(Branding.errorpage[options.pageName].header);
                    }catch(err){
                        window.console.error('Branding error. Missing header for ' + options.pageName + ' in product.json');
                    }
                    try{
                        $('.message').html(Branding.errorpage[options.pageName].message);
                    }catch(err){
                        window.console.error('Branding error. Missing error message for ' + options.pageName +' in product.json');
                    }
                })
                .then(function(){
                    self._showHidden();
                });
 			},

            _showHidden : function(){
                this.$el.removeClass('hidden');
            }
 		});
 		return BaseErrorPage;
 	}
 	);
 