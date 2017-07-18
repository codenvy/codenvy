/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.auth.sso.server;

import com.google.inject.name.Named;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.schedule.ScheduleRate;
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
import java.util.concurrent.TimeUnit;

/**
 * Validates email by the blacklist file.
 * File line format can be following:
 *  - Exact email: e.g. john@gmail.com - only this email will be banned;
 *  - Partial email with asterisk: e.g   *hotmail.com,  *john@gmail.com - any email which ends
 *    with this suffix will be banned.
 *
 * @author Alexander Garagatyi
 * @author Sergey Kabashniuk
 */
@Singleton
public class EmailValidator {
    private static final Logger LOG = LoggerFactory.getLogger(EmailValidator.class);

    private static final String EMAIL_BLACKLIST_FILE = "emailvalidator.blacklistfile";

    private final String blacklistPath;

    private Set<String> emailBlackList = Collections.emptySet();

    @Inject
    public EmailValidator(@Nullable @Named(EMAIL_BLACKLIST_FILE) String emailBlacklistFile) {
        this.blacklistPath = emailBlacklistFile;
        try {
            readBlacklistFile();
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
     * @return set with forbidden words
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    @ScheduleRate(period = 2, unit = TimeUnit.MINUTES)
    private void readBlacklistFile() throws IOException {
        if (blacklistPath == null) {
            return;
        }
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
            this.emailBlackList =  blacklist;
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
            if ((current.startsWith("*") && userMail.endsWith(current.substring(1))) || userMail.equals(current)) {
                throw new BadRequestException("User mail " + userMail + " is forbidden.");
            }
        }
    }
}
