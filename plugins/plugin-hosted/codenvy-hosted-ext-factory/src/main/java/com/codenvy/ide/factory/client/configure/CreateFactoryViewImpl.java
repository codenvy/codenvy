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
package com.codenvy.ide.factory.client.configure;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.FactoryResources;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilder;

import javax.validation.constraints.NotNull;

import static com.codenvy.ide.factory.client.FactoryResources.Style;
import static com.google.gwt.dom.client.Style.Unit;

/**
 * @author Anton Korneta
 */
@Singleton
public class CreateFactoryViewImpl extends Window implements CreateFactoryView {
    private static final RegExp FACTORY_NAME_PATTERN = RegExp.compile("[^A-Za-z0-9_-]");


    interface FactoryViewImplUiBinder extends UiBinder<Widget, CreateFactoryViewImpl> {
    }

    private final FactoryResources factoryResources;

    private ActionDelegate delegate;

    @UiField
    Style     style;
    @UiField
    TextBox   factoryName;
    @UiField
    TextBox   factoryLink;
    @UiField
    Label     factoryNameLabel;
    @UiField
    Label     factoryLinkLabel;
    @UiField
    Label     factoryNameErrorLabel;
    @UiField
    Button    createFactoryButton;
    @UiField
    FlowPanel upperPanel;
    @UiField
    FlowPanel lowerPanel;
    @UiField
    FlowPanel createFactoryPanel;
    @UiField
    Anchor    launch;
    @UiField
    Anchor    configure;

    private Tooltip labelsErrorTooltip;

    @Inject
    protected CreateFactoryViewImpl(FactoryViewImplUiBinder uiBinder,
                                    FactoryLocalizationConstant locale,
                                    FactoryResources factoryResources,
                                    ClipboardButtonBuilder buttonBuilder) {
        this.factoryResources = factoryResources;
        setTitle(locale.createFactoryTitle());
        setWidget(uiBinder.createAndBindUi(this));
        factoryNameLabel.setText(locale.createFactoryName());
        factoryLinkLabel.setText(locale.createFactoryLink());
        configure.getElement().insertFirst(factoryResources.configure().getSvg().getElement());
        launch.getElement().insertFirst(factoryResources.execute().getSvg().getElement());
        launch.addStyleName(style.launchIcon());
        configure.addStyleName(style.configureIcon());
        createFactoryButton.setEnabled(false);
        Button cancelButton = createButton(locale.createFactoryButtonClose(), "git-remotes-pull-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        createFactoryButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                delegate.onCreateClicked();
            }
        });
        cancelButton.ensureDebugId("projectReadOnlyGitUrl-btnClose");
        addButtonToFooter(cancelButton);
        getWidget().getElement().getStyle().setPadding(0, Unit.PX);
        buttonBuilder.withResourceWidget(factoryLink).build();
        factoryLink.setReadOnly(true);
        final Tooltip launchFactoryTooltip = Tooltip.create((elemental.dom.Element)launch.getElement(),
                                                            PositionController.VerticalAlign.TOP,
                                                            PositionController.HorizontalAlign.MIDDLE,
                                                            locale.createFactoryLaunchTooltip());
        launchFactoryTooltip.setShowDelayDisabled(false);

        final Tooltip configureFactoryTooltip = Tooltip.create((elemental.dom.Element)configure.getElement(),
                                                               PositionController.VerticalAlign.TOP,
                                                               PositionController.HorizontalAlign.MIDDLE,
                                                               locale.createFactoryConfigureTooltip());
        configureFactoryTooltip.setShowDelayDisabled(false);
        factoryName.getElement().setAttribute("placeholder", "new-factory-name");
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void showDialog() {
        clear();
        this.show();
    }

    @Override
    public String getFactoryName() {
        return factoryName.getText();
    }

    @Override
    public void setAcceptFactoryLink(String acceptLink) {
        factoryLink.setText(acceptLink);
        launch.getElement().setAttribute("target", "_blank");
        launch.setHref(acceptLink);
    }

    @Override
    public void setConfigureFactoryLink(String configureLink) {
        configure.getElement().setAttribute("target", "_blank");
        configure.setHref(configureLink);
    }

    @Override
    public void enableCreateFactoryButton(boolean enabled) {
        createFactoryButton.setEnabled(enabled);
    }

    @Override
    public void showFactoryNameError(@NotNull String labelMessage, @Nullable String tooltipMessage) {
        factoryName.addStyleName(factoryResources.factoryCSS().inputError());
        factoryNameErrorLabel.setText(labelMessage);
        if (labelsErrorTooltip != null) {
            labelsErrorTooltip.destroy();
        }

        if (!Strings.isNullOrEmpty(tooltipMessage)) {
            labelsErrorTooltip = Tooltip.create((elemental.dom.Element)factoryNameErrorLabel.getElement(),
                                                PositionController.VerticalAlign.TOP,
                                                PositionController.HorizontalAlign.MIDDLE,
                                                tooltipMessage);
            labelsErrorTooltip.setShowDelayDisabled(false);
        }
    }

    @Override
    public void hideFactoryNameError() {
        factoryName.removeStyleName(factoryResources.factoryCSS().inputError());
        factoryNameErrorLabel.setText("");
    }

    @Override
    public void close() {
        this.hide();
    }

    @UiHandler({"factoryName"})
    public void onProjectNameChanged(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER
            && createFactoryButton.isEnabled()) {
            delegate.onCreateClicked();
        } else {
            String name = factoryName.getValue();
            if (!Strings.isNullOrEmpty(name) && FACTORY_NAME_PATTERN.test(name)) {
                name = name.replaceAll("[^A-Za-z0-9_]", "-");
                factoryName.setValue(name);
            }
            delegate.onFactoryNameChanged(name);
        }
    }

    private void clear() {
        launch.getElement().removeAttribute("href");
        configure.getElement().removeAttribute("href");
        createFactoryButton.setEnabled(false);
        factoryName.removeStyleName(factoryResources.factoryCSS().inputError());
        factoryNameErrorLabel.setText("");
        factoryName.setText("");
        factoryLink.setText("");
    }
}