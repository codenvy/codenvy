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
package com.codenvy.organization.api.listener;

import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.mail.EmailBean;
import com.codenvy.mail.MailSender;
import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.api.event.MemberAddedEvent;
import com.codenvy.organization.api.event.MemberRemovedEvent;
import com.codenvy.organization.api.event.OrganizationRemovedEvent;
import com.codenvy.organization.api.event.OrganizationRenamedEvent;
import com.codenvy.organization.api.listener.templates.MemberAddedTemplate;
import com.codenvy.organization.api.listener.templates.MemberRemovedTemplate;
import com.codenvy.organization.api.listener.templates.OrganizationRemovedTemplate;
import com.codenvy.organization.api.listener.templates.OrganizationRenamedTemplate;
import com.codenvy.organization.shared.event.OrganizationEvent;
import com.codenvy.organization.shared.model.Member;
import com.codenvy.template.processor.html.HTMLTemplateProcessor;
import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.user.server.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

import static javax.ws.rs.core.MediaType.TEXT_HTML;

/**
 * Notify users about organization changes.
 *
 * @author Anton Korneta
 */
@Singleton
public class OrganizationNotificationEmailSender implements EventSubscriber<OrganizationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationNotificationEmailSender.class);

    private final String                                   apiEndpoint;
    private final String                                   memberAddedSubject;
    private final String                                   memberRemovedSubject;
    private final String                                   orgRenamedSubject;
    private final String                                   orgRemovedSubject;
    private final String                                   mailFrom;
    private final HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf;
    private final MailSender                               mailSender;
    private final OrganizationManager                      organizationManager;
    private final UserManager                              userManager;
    private final DefaultEmailResourceResolver resourceResolver;

    @Inject
    public OrganizationNotificationEmailSender(@Named("mailsender.application.from.email.address") String mailFrom,
                                               @Named("che.api") String apiEndpoint,
                                               @Named("organization.email.member.added.subject") String memberAddedSubject,
                                               @Named("organization.email.member.removed.subject") String memberRemovedSubject,
                                               @Named("organization.email.renamed.subject") String orgRenamedSubject,
                                               @Named("organization.email.removed.subject") String orgRemovedSubject,
                                               DefaultEmailResourceResolver resourceResolver,
                                               HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf,
                                               MailSender mailSender,
                                               OrganizationManager organizationManager,
                                               UserManager userManager) {
        this.mailFrom = mailFrom;
        this.apiEndpoint = apiEndpoint;
        this.memberAddedSubject = memberAddedSubject;
        this.memberRemovedSubject = memberRemovedSubject;
        this.orgRenamedSubject = orgRenamedSubject;
        this.orgRemovedSubject = orgRemovedSubject;
        this.resourceResolver = resourceResolver;
        this.mailSender = mailSender;
        this.thymeleaf = thymeleaf;
        this.organizationManager = organizationManager;
        this.userManager = userManager;
    }

    @Inject
    private void subscribe(EventService eventService) {
        eventService.subscribe(this);
    }

    @Override
    public void onEvent(OrganizationEvent event) {
        try {
            if (event.getInitiator() != null) {
                if (event.getOrganization().getParent() == null) {
                    try {
                        userManager.getByName(event.getOrganization().getName());
                        return;
                    } catch (NotFoundException ex) {
                        //it is not personal organization
                    }
                }
                switch (event.getType()) {
                    case MEMBER_ADDED:
                        send((MemberAddedEvent)event);
                        break;
                    case MEMBER_REMOVED:
                        send((MemberRemovedEvent)event);
                        break;
                    case ORGANIZATION_REMOVED:
                        send((OrganizationRemovedEvent)event);
                        break;
                    case ORGANIZATION_RENAMED:
                        send((OrganizationRenamedEvent)event);
                }
            }
        } catch (Exception ex) {
            LOG.error("Failed to send email notification '{}' cause : '{}'", ex.getLocalizedMessage());
        }
    }

    private void send(MemberAddedEvent event) throws Exception {
        final String orgName = event.getOrganization().getName();
        final String emailTo = event.getMember().getEmail();
        final String initiator = event.getInitiator();
        final String dashboardEndpoint = apiEndpoint.replace("api", "dashboard");
        final String orgQualifiedName = event.getOrganization().getQualifiedName();
        final String processed = thymeleaf.process(new MemberAddedTemplate(orgName,
                                                                           dashboardEndpoint,
                                                                           orgQualifiedName,
                                                                           initiator));
        send(new EmailBean().withBody(processed).withSubject(memberAddedSubject), emailTo);
    }

    private void send(MemberRemovedEvent event) throws Exception {
        final String organizationName = event.getOrganization().getName();
        final String initiator = event.getInitiator();
        final String emailTo = event.getMember().getEmail();
        final String processed = thymeleaf.process(new MemberRemovedTemplate(organizationName, initiator));
        send(new EmailBean().withBody(processed).withSubject(memberRemovedSubject), emailTo);
    }

    private void send(OrganizationRemovedEvent event) throws Exception {
        final String processed = thymeleaf.process(new OrganizationRemovedTemplate(event.getOrganization()
                                                                                        .getName()));
        for (Member member : event.getMembers()) {
            final String emailTo = userManager.getById(member.getUserId()).getEmail();
            try {
                send(new EmailBean().withBody(processed).withSubject(orgRemovedSubject), emailTo);
            } catch (Exception ignore) {
            }
        }
    }

    private void send(OrganizationRenamedEvent event) throws Exception {
        final String processed = thymeleaf.process(new OrganizationRenamedTemplate(event.getOldName(),
                                                                                   event.getNewName()));
        Page<? extends Member> members;
        long next = 0;
        do {
            members = organizationManager.getMembers(event.getOrganization().getId(), 100, next);
            for (Member member : members.getItems()) {
                final String emailTo = userManager.getById(member.getUserId()).getEmail();
                try {
                    send(new EmailBean().withBody(processed).withSubject(orgRenamedSubject), emailTo);
                } catch (Exception ignore) {
                }
            }
            next += 100;
        } while (members.hasNextPage());
    }

    private void send(EmailBean emailBean, String emailTo) throws IOException, ServerException {
        mailSender.sendAsync(resourceResolver.resolve(emailBean.withFrom(mailFrom)
                                                               .withReplyTo(mailFrom)
                                                               .withTo(emailTo)
                                                               .withMimeType(TEXT_HTML)));
    }
}
