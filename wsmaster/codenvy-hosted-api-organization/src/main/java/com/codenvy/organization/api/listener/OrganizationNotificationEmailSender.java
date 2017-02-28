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

import com.codenvy.mail.Attachment;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.google.common.io.Files.toByteArray;
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
    private final String                                   teamRenamedSubject;
    private final String                                   teamRemovedSubject;
    private final Map<String, String>                      logos;
    private final String                                   mailFrom;
    private final HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf;
    private final MailSender                               mailSender;
    private final OrganizationManager                      organizationManager;
    private final UserManager                              userManager;

    @Inject
    public OrganizationNotificationEmailSender(@Named("mailsender.application.from.email.address") String mailFrom,
                                               @Named("che.api") String apiEndpoint,
                                               @Named("team.email.member.added.subject") String memberAddedSubject,
                                               @Named("team.email.member.removed.subject") String memberRemovedSubject,
                                               @Named("team.email.renamed.subject") String teamRenamedSubject,
                                               @Named("team.email.removed.subject") String teamRemovedSubject,
                                               @Named("codenvy.email.logos") Map<String, String> logos,
                                               HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf,
                                               MailSender mailSender,
                                               OrganizationManager organizationManager,
                                               UserManager userManager) {
        this.mailFrom = mailFrom;
        this.apiEndpoint = apiEndpoint;
        this.memberAddedSubject = memberAddedSubject;
        this.memberRemovedSubject = memberRemovedSubject;
        this.teamRenamedSubject = teamRenamedSubject;
        this.teamRemovedSubject = teamRemovedSubject;
        this.logos = logos;
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
            if (event.getOrganization() != null) {
                try {
                    // checks whether personal organization or not
                    userManager.getByName(event.getOrganization().getName());
                } catch (NotFoundException ignore) {
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
            }
        } catch (Exception ex) {
            LOG.error("Failed to send email notification '{}' cause : '{}'", ex.getLocalizedMessage());
        }
    }

    private void send(MemberAddedEvent event) throws Exception {
        final String teamName = event.getOrganization().getName();
        final String emailTo = event.getMember().getEmail();
        final String initiator = event.getInitiator();
        final String teamLink = apiEndpoint.replace("api", "dashboard/#/team/" + initiator + '/' + teamName);
        final String processed = thymeleaf.process(new MemberAddedTemplate(teamName, teamLink, initiator));
        send(new EmailBean().withBody(processed).withSubject(memberAddedSubject), emailTo);
    }

    private void send(MemberRemovedEvent event) throws Exception {
        final String teamName = event.getOrganization().getName();
        final String initiator = event.getInitiator();
        final String emailTo = event.getMember().getEmail();
        final String processed = thymeleaf.process(new MemberRemovedTemplate(teamName, initiator));
        send(new EmailBean().withBody(processed).withSubject(memberRemovedSubject), emailTo);
    }

    private void send(OrganizationRemovedEvent event) throws Exception {
        final String processed = thymeleaf.process(new OrganizationRemovedTemplate(event.getOrganization()
                                                                                        .getName()));
        for (Member member : event.getMembers()) {
            final String emailTo = userManager.getById(member.getUserId()).getEmail();
            try {
                send(new EmailBean().withBody(processed).withSubject(teamRemovedSubject), emailTo);
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
                    send(new EmailBean().withBody(processed).withSubject(teamRenamedSubject), emailTo);
                } catch (Exception ignore) {
                }
            }
            next += 100;
        } while (members.hasNextPage());
    }

    private void send(EmailBean emailBean, String mailTo) throws IOException, ServerException {
        final List<Attachment> attachments = new ArrayList<>(logos.size());
        for (Map.Entry<String, String> entry : logos.entrySet()) {
            final File logo = new File(this.getClass().getResource(entry.getValue()).getPath());
            final String encoded = Base64.getEncoder().encodeToString(toByteArray(logo));
            attachments.add(new Attachment().withContent(encoded)
                                            .withContentId(entry.getKey())
                                            .withFileName(entry.getKey()));
        }
        mailSender.sendMail(emailBean.withFrom(mailFrom)
                                     .withReplyTo(mailFrom)
                                     .withTo(mailTo)
                                     .withMimeType(TEXT_HTML)
                                     .withAttachments(attachments));
    }
}
