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
package com.codenvy.plugin.pullrequest.projecttype.server;


import com.codenvy.plugin.pullrequest.shared.ContributionProjectTypeConstants;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codenvy.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_DISPLAY_NAME;
import static com.codenvy.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_ID;

/**
 * The contribution project type definition.
 *
 * @author Kevin Pollet
 */
@Singleton
public class ContributionProjectType extends ProjectTypeDef {
    @Inject
    public ContributionProjectType() {
        super(CONTRIBUTION_PROJECT_TYPE_ID, CONTRIBUTION_PROJECT_TYPE_DISPLAY_NAME, false, true);

        addVariableDefinition(ContributionProjectTypeConstants.CONTRIBUTE_LOCAL_BRANCH_NAME, "Name of local branch", false);
        addVariableDefinition(ContributionProjectTypeConstants.CONTRIBUTE_TO_BRANCH_VARIABLE_NAME, "Branch where the contribution has to be pushed", true);
    }
}
