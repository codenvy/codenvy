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
package com.codenvy.auth.sso.server;

import com.google.inject.name.Named;

import org.eclipse.che.api.core.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Validates email and workspace name.
 *
 * @author Alexander Garagatyi
 * @author Sergey Kabashniuk
 */
@Singleton
public class InputDataValidator {

    public static final  String      EMAIL_BLACKLIST_FILE        = "emailvalidator.blacklistfile";
    private static final Logger      LOG                         = LoggerFactory.getLogger(InputDataValidator.class);
    private              Set<String> emailBlackList              = Collections.emptySet();

    @Inject
    public InputDataValidator(@Named(EMAIL_BLACKLIST_FILE) String emailBlacklistFile) {
        try {
            this.emailBlackList = readBlacklistFile(emailBlacklistFile);
        } catch (FileNotFoundException e) {
            LOG.warn("Email blacklist is not found or is a directory", emailBlacklistFile);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Reads set of forbidden words from file. One word by line. If file not
     * found file reading failed, then throws exception.
     *
     * @param blacklistPath
     *         path to email black list file
     * @return set with forbidden words
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    private static Set<String> readBlacklistFile(String blacklistPath) throws IOException {
        InputStream blacklistStream;
        File blacklistFile = new File(blacklistPath);
        if (blacklistFile.exists() && blacklistFile.isFile()) {
            blacklistStream = new FileInputStream(blacklistFile);
        } else {
            blacklistStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(blacklistPath);
            if (blacklistStream == null) {
                throw new FileNotFoundException("Blacklist file " + blacklistPath + " not found!");
            }
        }

        try (InputStream is = blacklistStream) {
            Set<String> blacklist = new HashSet<>();
            try (Scanner in = new Scanner(is)) {
                while (in.hasNextLine()) {
                    blacklist.add(in.nextLine().trim());
                }
            }
            return blacklist;
        }
    }

    public void validateUserMail(String userMail) throws BadRequestException {
        if (userMail == null || userMail.isEmpty()) {
            throw new BadRequestException("User mail can't be null or ''");
        }

        try {
            InternetAddress address = new InternetAddress(userMail);
            address.validate();
        } catch (AddressException e) {
            throw new BadRequestException(
                    "E-Mail validation failed. Please check the format of your e-mail address.");
        }

        // Check blacklist
        for (String current : emailBlackList) {
            if (userMail.endsWith(current)) {
                throw new BadRequestException("User mail " + userMail + " is forbidden.");
            }
        }
    }
}
