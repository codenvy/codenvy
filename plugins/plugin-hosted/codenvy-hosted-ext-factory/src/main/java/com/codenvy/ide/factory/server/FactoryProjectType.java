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
package com.codenvy.ide.factory.server;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;

import javax.inject.Singleton;

import static com.codenvy.ide.factory.shared.Constants.FACTORY_ID_ATTRIBUTE_NAME;
import static com.codenvy.ide.factory.shared.Constants.FACTORY_PROJECT_TYPE_DISPLAY_NAME;
import static com.codenvy.ide.factory.shared.Constants.FACTORY_PROJECT_TYPE_ID;

/**
 * Factory project type mixin.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
public class FactoryProjectType extends ProjectTypeDef {
    public FactoryProjectType() {
        super(FACTORY_PROJECT_TYPE_ID, FACTORY_PROJECT_TYPE_DISPLAY_NAME, false, true);
        addVariableDefinition(FACTORY_ID_ATTRIBUTE_NAME, "Factory flag", false);
    }
}
