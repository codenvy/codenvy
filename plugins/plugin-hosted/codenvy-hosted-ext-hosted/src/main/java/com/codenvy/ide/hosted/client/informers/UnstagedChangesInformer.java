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
package com.codenvy.ide.hosted.client.informers;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.AppCloseActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.rest.RestContext;

/**
 * Checks for unsaved and uncommitted files and inform the user when he tries to leave the page.
 *
 * @author Vitaliy Guliy
 */
public class UnstagedChangesInformer extends Action {

    private final AppContext appContext;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})//used in native method
    private final String     restContext;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})//used in native method
    private final String     workspaceId;

    @Inject
    public UnstagedChangesInformer(AppContext appContext,
                                   @RestContext String restContext) {
        this.appContext = appContext;
        this.restContext = restContext;
        this.workspaceId = appContext.getWorkspace().getId();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e instanceof AppCloseActionEvent) {
            ((AppCloseActionEvent)e).setCancelMessage(checkUnstagedChanges());
        }
    }

    /**
     * Performs registered action
     *
     * @return null if user hasn't unstaged changes else string with description of changes.
     */
    private native String checkUnstagedChanges() /*-{
        var instance = this;

        var projectPath = instance.@com.codenvy.ide.hosted.client.informers.UnstagedChangesInformer::getCurrentProjectPath()();
        if (projectPath == null) {
            return;
        }

        var gitStatusUrl = instance.@com.codenvy.ide.hosted.client.informers.UnstagedChangesInformer::restContext + "/git/"
            + instance.@com.codenvy.ide.hosted.client.informers.UnstagedChangesInformer::workspaceId +
            "/status?projectPath=" + projectPath + "&short=false";

        try {
            var gitStatusRequest = new XMLHttpRequest();
            gitStatusRequest.open("POST", gitStatusUrl, false);
            gitStatusRequest.setRequestHeader("Accept", "text/plain");
            gitStatusRequest.setRequestHeader("Content-type", "application/json");
            gitStatusRequest.send();

            var prompt = null;

            if (gitStatusRequest.status == 200) {
                var text = gitStatusRequest.responseText;

                var textParts = text.split("\n");

                for (var i = 0; i < textParts.length; i++) {
                    if (textParts[i].indexOf("Changes to be committed") >= 0) {
                        prompt = "You have uncommitted changes.";
                        break;
                    }

                    if (textParts[i].indexOf("Changes not staged for commit") >= 0) {
                        prompt = "You have not staged changes for commit.";
                        break;
                    }

                    if (textParts[i].indexOf("Your branch is ahead") >= 0) {
                        prompt = "You have not pushed changes.";
                        break;
                    }
                }

                if (prompt) {
                    try {
                        var url = instance.@com.codenvy.ide.hosted.client.informers.UnstagedChangesInformer::restContext +
                            "/ui-messages/unstaged.changes.warning";

                        var request = new XMLHttpRequest();
                        request.open("GET", url, false);
                        request.send();

                        if (request.status == 200 && request.readyState == 4) {
                            prompt = request.responseText;
                        }
                    } catch (exc) {
                        console.log(exc.message);
                    }

                    return prompt;
                }
            }
        } catch (exc) {
            console.log(exc.message);
        }

        return null;
    }-*/;

    /**
     * Returns path of current opened project.
     */
    private String getCurrentProjectPath() {
        Project project = appContext.getRootProject();
        if (project == null) {
            return null;
        }

        return project.getLocation().toString();
    }
}
