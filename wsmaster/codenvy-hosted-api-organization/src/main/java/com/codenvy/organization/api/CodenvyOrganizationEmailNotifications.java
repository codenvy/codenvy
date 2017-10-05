/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.organization.api;

import com.codenvy.mail.DefaultEmailResourceResolver;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.mail.EmailBean;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.eclipse.che.multiuser.organization.api.notification.OrganizationEmailNotifications;

/** @author Sergii Leshchenko */
public class CodenvyOrganizationEmailNotifications extends OrganizationEmailNotifications {

  private final DefaultEmailResourceResolver resourceResolver;

  @Inject
  public CodenvyOrganizationEmailNotifications(
      @Named("che.mail.from_email_address") String mailFrom,
      @Named("che.organization.email.member_added_subject") String memberAddedSubject,
      @Named("che.organization.email.member_added_template") String memberAddedTemplate,
      @Named("che.organization.email.member_removed_subject") String memberRemovedSubject,
      @Named("che.organization.email.member_removed_template") String memberRemovedTemplate,
      @Named("che.organization.email.org_removed_subject") String orgRemovedSubject,
      @Named("che.organization.email.org_removed_template") String orgRemovedTemplate,
      @Named("che.organization.email.org_renamed_subject") String orgRenamedSubject,
      @Named("che.organization.email.org_renamed_template") String orgRenamedTemplate,
      TemplateProcessor templateProcessor,
      DefaultEmailResourceResolver resourceResolver) {
    super(
        mailFrom,
        memberAddedSubject,
        memberAddedTemplate,
        memberRemovedSubject,
        memberRemovedTemplate,
        orgRemovedSubject,
        orgRemovedTemplate,
        orgRenamedSubject,
        orgRenamedTemplate,
        templateProcessor);
    this.resourceResolver = resourceResolver;
  }

  @Override
  protected EmailBean doBuildEmail(
      String subject, String templatePath, Map<String, Object> attributes) throws ServerException {
    return resourceResolver.resolve(super.doBuildEmail(subject, templatePath, attributes));
  }
}
