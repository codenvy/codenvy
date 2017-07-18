/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.hosted.client.login;


import org.eclipse.che.ide.api.mvp.View;

/**
 * Dialog box that prompts the visitor for login or create account
 *
 * @author Max Shaposhnik
 * @author Sergii Leschenko
 */
public interface PromptToLoginView extends View<PromptToLoginView.ActionDelegate> {

    interface ActionDelegate {

        void onLogin();

        void onCreateAccount();

    }

    void showDialog(String title, String message);

    void close();
}