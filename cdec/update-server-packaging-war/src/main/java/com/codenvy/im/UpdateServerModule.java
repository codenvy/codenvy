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
package com.codenvy.im;

import com.codenvy.im.update.ReportService;
import com.codenvy.im.update.RepositoryService;
import com.codenvy.im.update.UtilService;
import com.google.inject.AbstractModule;

import org.eclipse.che.inject.DynaModule;

/**
 * @author Anatoliy Bazko
 */
@DynaModule
public class UpdateServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RepositoryService.class);
        bind(ReportService.class);
        bind(UtilService.class);
    }
}
