/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.api.account.subscription.service.util;

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Member;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.user.server.dao.User;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.commons.lang.Strings;

import org.codenvy.mail.MailSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.api.account.billing.PaymentState.CREDIT_CARD_MISSING;
import static com.codenvy.api.account.billing.PaymentState.PAID_SUCCESSFULLY;
import static com.codenvy.commons.lang.IoUtil.getResource;
import static com.codenvy.commons.lang.IoUtil.readAndCloseQuietly;

/**
 * Sends emails and retrieve list of emails of account owners
 *
 * @author Alexander Garagatyi
 */
public class SubscriptionMailSender {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionMailSender.class);

    private static final String TEMPLATE_CC_OUTSTANDING = "/email-templates/saas-outstanding-balance.html";

    private static final String TEMPLATE_CC_DELETE = "/email-templates/saas-remove-credit-card.html";

    @Inject
    @Named("subscription.saas.mail.invoice.subject")
    private  String  invoiceSubject;

    @Inject
    @Named("subscription.saas.mail.invoice.no_credit_card.subject")
    private  String  invoiceCreditCardMissingSubject;

    @Inject
    @Named("subscription.saas.mail.billing.failed.subject")
    private  String billingFailedSubject;

    @Inject
    @Named("subscription.saas.mail.address")
    private String billingAddress;

    @Inject
    private AccountDao accountDao;

    @Inject
    private UserDao userDao;

    @Inject
    private MailSenderClient mailClient;

    @Inject
    @Named("api.endpoint")
    private String apiEndpoint;

    public void sendInvoice(String accountId, String state, String text) throws IOException, MessagingException, ServerException {
        String subject;
        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
        if (PAID_SUCCESSFULLY.getState().equals(state)) {
            subject = invoiceSubject;
        } else if (CREDIT_CARD_MISSING.getState().equals(state)) {
            subject = invoiceCreditCardMissingSubject;
        } else {
            subject = billingFailedSubject;
        }
        LOG.debug("Send invoice to {}", accountOwnersEmails);
        sendEmail(text,subject, accountOwnersEmails, MediaType.TEXT_HTML, null);
    }

    public void sendTrialExpiredNotification(String accountId) throws IOException, MessagingException, ServerException {
        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
        LOG.debug("Send email about trial removing to {}", accountOwnersEmails);
        // TODO: replace text with template && check title
        sendEmail("Send email about trial removing", "Subscription notification", accountOwnersEmails, MediaType.TEXT_PLAIN, null);
    }

    public void sendSubscriptionChargedNotification(String accountId) throws IOException, MessagingException, ServerException {
        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
        LOG.debug("Send email about subscription charging to {}", accountOwnersEmails);
        // TODO: replace text with template && check title
        sendEmail("Send email about subscription charging", "Subscription notification", accountOwnersEmails, MediaType.TEXT_PLAIN, null);
    }

    public void sendSubscriptionChargeFailNotification(String accountId) throws IOException, MessagingException, ServerException {
        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
        LOG.debug("Send email about unsuccessful subscription charging to {}", accountOwnersEmails);
        // TODO: replace text with template && check title
        sendEmail("Send email about unsuccessful subscription charging", "Subscription notification", accountOwnersEmails, MediaType.TEXT_PLAIN, null);
    }

    public void sendSubscriptionExpiredNotification(String accountId, Integer days) throws IOException, MessagingException, ServerException {
        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
        LOG.debug("Send email about trial removing in {} days to {}", days, accountOwnersEmails);
        // TODO: replace text with template && check title
        sendEmail("Send email about trial removing in " + days + " days", "Subscription notification", accountOwnersEmails, MediaType.TEXT_PLAIN, null);
    }

    public void sendCCRemovedNotification(String accountId, String ccNumber, String ccType) throws IOException, MessagingException, ServerException {
        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
        Map<String, String> properties = new HashMap<>();
        properties.put("type", ccType);
        properties.put("number", ccNumber);
        LOG.debug("Send credit card removed notifications to {}", accountOwnersEmails);
        sendEmail(readAndCloseQuietly(getResource(TEMPLATE_CC_DELETE)), "Credit Card Removed from Codenvy",
                  accountOwnersEmails, MediaType.TEXT_HTML, properties);
    }

    public void sendAccountLockedNotification(String accountId, String total) throws IOException, MessagingException, ServerException {
        List<String> accountOwnersEmails = getAccountOwnersEmails(accountId);
        Map<String, String> properties = new HashMap<>();
        properties.put("total", total);
        LOG.debug("Send account locked notifications to {}", accountOwnersEmails);
        sendEmail(readAndCloseQuietly(getResource(TEMPLATE_CC_OUTSTANDING)), "Outstanding Balance with Codenvy",
                  accountOwnersEmails, MediaType.TEXT_HTML,  properties);
    }


    private List<String> getAccountOwnersEmails(String accountId) throws ServerException {
        List<String> emails = new ArrayList<>();
        for (Member member : accountDao.getMembers(accountId)) {
            if (member.getRoles().contains("account/owner")) {
                try {
                    User user = userDao.getById(member.getUserId());

                    emails.add(user.getEmail());
                } catch (ServerException | NotFoundException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return emails;
    }

    private void sendEmail(String text, String subject, List<String> emails, String mediaType, Map<String, String> properties)
            throws IOException, MessagingException {
        if (properties != null) {
            properties.put("com.codenvy.masterhost.url", apiEndpoint.substring(0, apiEndpoint.lastIndexOf("/")));
        }
        mailClient.sendMail(billingAddress,
                            Strings.join(", ", emails.toArray(new String[emails.size()])),
                            null,
                            subject,
                            mediaType,
                            text,
                            properties);
    }
}
