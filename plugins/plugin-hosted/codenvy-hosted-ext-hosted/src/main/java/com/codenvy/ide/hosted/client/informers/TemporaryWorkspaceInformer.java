/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.ide.hosted.client.informers;

import com.codenvy.ide.hosted.client.HostedLocalizationConstant;
import com.codenvy.ide.hosted.client.HostedResources;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.vectomatic.dom.svg.ui.SVGImage;

import static com.google.gwt.dom.client.Style.Display.BLOCK;
import static com.google.gwt.dom.client.Style.Display.NONE;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * @author Vitaliy Guliy
 * @author Oleksii Orel
 */
public class TemporaryWorkspaceInformer {
    private final HostedResources             resources;
    private final HostedLocalizationConstant  locale;
    private final ActionManager               actionManager;
    private final TemporaryWorkspaceIndicator temporaryWorkspaceIndicator;
    private final AppContext                  appContext;

    @Inject
    public TemporaryWorkspaceInformer(ActionManager actionManager,
                                      HostedResources resources,
                                      HostedLocalizationConstant locale,
                                      AppContext appContext) {
        this.actionManager = actionManager;
        this.resources = resources;
        this.locale = locale;
        temporaryWorkspaceIndicator = new TemporaryWorkspaceIndicator();
        this.appContext = appContext;
    }

    public void process() {
        Workspace workspace = appContext.getWorkspace();
        if (workspace == null || !workspace.isTemporary()) {
            return;
        }

        actionManager.registerAction("temporaryWorkspaceIndicator", temporaryWorkspaceIndicator);

        DefaultActionGroup mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_LEFT_STATUS_PANEL);
        mainToolbarGroup.add(temporaryWorkspaceIndicator, Constraints.FIRST);
        mainToolbarGroup.addSeparator();
    }

    public class TemporaryWorkspaceIndicator extends Action implements CustomComponentAction {

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public Widget createCustomComponent(Presentation presentation) {
            final FlowPanel wrapper = new FlowPanel();
            final Label button = new Label();
            final Element tooltipElement = DOM.createDiv();
            final Element tooltipHeader = DOM.createDiv();
            final Element tooltipHeaderMessageElement = DOM.createSpan();
            final Element tooltipBody = DOM.createDiv();
            final Element tooltipBodyMessageElement = DOM.createSpan();
            final Element tooltipArrow = DOM.createDiv();

            wrapper.addStyleName(resources.hostedCSS().temporary());
            wrapper.add(new SVGImage(resources.temporaryButton()));

            button.addStyleName(resources.hostedCSS().temporaryLabel());
            button.setText(locale.temporaryToolbarLabelText());
            button.ensureDebugId("temporary-workspace-used-toolbar-button");

            // add handlers
            wrapper.addDomHandler(new MouseOverHandler() {
                public void onMouseOver(MouseOverEvent event) {
                    tooltipHeaderMessageElement.setInnerHTML(locale.temporaryToolbarLabelTitle());
                    tooltipBodyMessageElement.setInnerHTML(locale.temporaryToolbarLabelTitleMessage());
                    tooltipElement.getStyle().setRight(0, PX);
                    tooltipElement.getStyle().setBottom(0, PX);
                    tooltipElement.getStyle().setDisplay(BLOCK);

                    final Element parent = wrapper.getElement();
                    final int screenWidth = Document.get().getClientWidth();
                    final int screenHeight = Document.get().getClientHeight();
                    final int parentRight = screenWidth - parent.getAbsoluteRight();
                    final double parentMiddleRight = parentRight + (parent.getClientWidth() / 2.0);

                    double right = parentRight - (tooltipElement.getOffsetWidth() / 2.0);
                    right += parent.getClientWidth() / 2.0;
                    if (right < 0) {
                        right = 0;
                    }
                    if (screenWidth < (right + tooltipElement.getOffsetWidth())) {
                        right = screenWidth - tooltipElement.getOffsetWidth();
                    }
                    tooltipElement.getStyle().setRight(right, PX);
                    tooltipArrow.getStyle().setRight(parentMiddleRight - (tooltipArrow.getOffsetWidth() / 2.0), PX);
                    tooltipElement.getStyle()
                                  .setBottom(screenHeight - parent.getAbsoluteTop() + (tooltipArrow.getOffsetHeight() / 2.0), PX);
                    tooltipArrow.getStyle().setBottom(screenHeight - parent.getAbsoluteTop() - (tooltipArrow.getOffsetHeight() / 2.0), PX);
                }
            }, MouseOverEvent.getType());

            wrapper.addDomHandler(new MouseOutHandler() {
                public void onMouseOut(MouseOutEvent event) {
                    tooltipElement.getStyle().setDisplay(NONE);
                }
            }, MouseOutEvent.getType());

            wrapper.add(button);

            tooltipHeader.addClassName(resources.hostedCSS().bottomMenuTooltipHeader());
            tooltipHeader.appendChild(new SVGImage(resources.temporaryButton()).getElement());
            tooltipHeader.appendChild(tooltipHeaderMessageElement);

            tooltipBody.addClassName(resources.hostedCSS().bottomMenuTooltipBody());
            tooltipBody.appendChild(tooltipBodyMessageElement);

            tooltipElement.addClassName(resources.hostedCSS().bottomMenuTooltip());
            tooltipElement.appendChild(tooltipHeader);
            tooltipElement.appendChild(tooltipBody);
            tooltipElement.appendChild(tooltipArrow);

            wrapper.getElement().appendChild(tooltipElement);

            return wrapper;
        }
    }

}
