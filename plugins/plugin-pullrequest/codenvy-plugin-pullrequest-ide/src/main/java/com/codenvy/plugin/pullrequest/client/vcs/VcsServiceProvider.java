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
package com.codenvy.plugin.pullrequest.client.vcs;

import org.eclipse.che.api.core.model.project.ProjectConfig;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.ext.git.client.GitUtil.isUnderGit;

/**
 * Provider for the {@link VcsService}.
 *
 * @author Kevin Pollet
 */
public class VcsServiceProvider {
    private final GitVcsService gitVcsService;

    @Inject
    public VcsServiceProvider(@NotNull final GitVcsService gitVcsService) {
        this.gitVcsService = gitVcsService;
    }

    /**
     * Returns the {@link VcsService} implementation corresponding to the current project VCS.
     *
     * @return the {@link VcsService} implementation or {@code null} if not supported or not
     * initialized.
     */
    public VcsService getVcsService(final ProjectConfig project) {
        if (project != null) {
            if (isUnderGit(project)) {
                return gitVcsService;
            }
        }
        return null;
    }
}
