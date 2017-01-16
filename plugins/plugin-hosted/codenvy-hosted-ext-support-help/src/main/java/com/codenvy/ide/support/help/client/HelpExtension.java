/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.ide.support.help.client;

import com.codenvy.ide.support.help.client.action.CreateSupportTicketAction;
import com.codenvy.ide.support.help.client.action.RedirectToEngineerChatChannelAction;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;


/**
 * Extension which adds menu items for help menu, depending on the SubscriptionDescriptor
 *
 * @author Oleksii Orel
 */
@Singleton
@Extension(title = "SupportHelp", version = "1.0.0")
public class HelpExtension {
    private final DtoUnmarshallerFactory              dtoUnmarshallerFactory;
    private final HelpResources                       resources;
    private final ActionManager                       actionManager;
    private final RedirectToEngineerChatChannelAction redirectToEngineerChatChannelAction;
    private final CreateSupportTicketAction           createSupportTicketAction;
    private final HelpLocalizationConstant            localizationConstant;
    private final DefaultActionGroup                  helpGroup;

    /** Create extension. */
    @Inject
    public HelpExtension(ActionManager actionManager,
                         RedirectToEngineerChatChannelAction redirectToEngineerChatChannelAction,
                         CreateSupportTicketAction createSupportTicketAction,
                         HelpLocalizationConstant localizationConstant,
                         DtoUnmarshallerFactory dtoUnmarshallerFactory,
                         HelpResources resources) {
        this.resources = resources;
        this.actionManager = actionManager;
        this.createSupportTicketAction = createSupportTicketAction;
        this.redirectToEngineerChatChannelAction = redirectToEngineerChatChannelAction;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.localizationConstant = localizationConstant;
        helpGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_HELP);

        init();
    }

    private void init() {
        // Compose Help menu
        addDefaultHelpAction();
        checkPremiumSubscription();

    }

    private void checkPremiumSubscription() {
        // TODO: fixme account
//        final String accountId = Config.getCurrentWorkspace().getAccountId();
//        if (accountId != null) {
//            subscriptionServiceClient.getSubscriptions(accountId, new AsyncRequestCallback<List<SubscriptionDescriptor>>(
//                    dtoUnmarshallerFactory.newListUnmarshaller(SubscriptionDescriptor.class)) {
//
//                @Override
//                protected void onSuccess(List<SubscriptionDescriptor> result) {
//                    for (SubscriptionDescriptor subscription : result) {
//                        if (!("Saas".equals(subscription.getServiceId()) &&
//                              "Community".equals(subscription.getProperties().get("Package")))) {
//                            addPremiumSupportHelpAction();
//                            return;
//                        }
//                    }
//                }
//
//                @Override
//                protected void onFailure(Throwable exception) {
//                    //User hasn't permission to account
//                }
//            });
//        }
    }

    private void addDefaultHelpAction() {
        // TODO change link for engineer chat channel
        //actionManager.registerAction(localizationConstant.redirectToEngineerChatChannelAction(), redirectToEngineerChatChannelAction);
        //helpGroup.add(redirectToEngineerChatChannelAction);
    }

    private void addPremiumSupportHelpAction() {
        // userVoice init
        ScriptInjector.fromUrl(resources.userVoice().getSafeUri().asString())
                      .setWindow(ScriptInjector.TOP_WINDOW)
                      .setCallback(new Callback<Void, Exception>() {
                          @Override
                          public void onSuccess(Void aVoid) {
                              // add action
                              actionManager.registerAction(localizationConstant.createSupportTicketAction(), createSupportTicketAction);

                              helpGroup.addSeparator();
                              helpGroup.add(createSupportTicketAction);
                          }

                          @Override
                          public void onFailure(Exception e) {
                              Log.error(getClass(), "Unable to inject UserVoice", e);
                          }
                      }).inject();
    }

}
