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
package com.codenvy.plugin.urlfactory;

import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

import javax.inject.Singleton;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Merge or add inside a factory the source storage dto
 *
 * @author Florent Benoit
 */
@Singleton
public class ProjectConfigDtoMerger {

    /**
     * Apply the merging of project config dto including source storage dto into the existing factory
     * <p>
     * here are the following rules
     * <ul>
     * <li>no projects --> add whole project</li>
     * <li>if projects
     * <ul>
     * <li>: if there is only one project: add source if missing</li>
     * <li> if many projects: do nothing</li>
     * </ul></li>
     * </ul>
     *
     * @param factory
     * @param computedProjectConfig
     * @return
     */
    public FactoryDto merge(FactoryDto factory, ProjectConfigDto computedProjectConfig) {

        final List<ProjectConfigDto> projects = factory.getWorkspace().getProjects();
        if (projects == null || projects.isEmpty()) {
            factory.getWorkspace().setProjects(singletonList(computedProjectConfig));
            return factory;
        }

        // if we're here, they are projects
        if (projects.size() == 1) {
            ProjectConfigDto projectConfig = projects.get(0);
            if (projectConfig.getSource() == null)
                projectConfig.setSource(computedProjectConfig.getSource());
        }

        return factory;
    }
}
