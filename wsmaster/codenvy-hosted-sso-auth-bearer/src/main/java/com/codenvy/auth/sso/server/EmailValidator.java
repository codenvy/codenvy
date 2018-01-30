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
package com.codenvy.auth.sso.server;

import com.google.inject.name.Named;
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
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates email by the blacklist file. File line format can be following: - Exact email: e.g.
 * john@gmail.com - only this email will be banned; - Partial email with asterisk: e.g *hotmail.com,
 * *john@gmail.com - any email which ends with this suffix will be banned.
 *
 * @author Alexander Garagatyi
 * @author Sergey Kabashniuk
 */
@Singleton
public class EmailValidator {
  private static final Logger LOG = LoggerFactory.getLogger(EmailValidator.class);

  private static final String EMAIL_BLACKLIST_FILE = "emailvalidator.blacklistfile";
  private static final Pattern EMAIL_ILLEGAL_CHARACTERS_PATTERN =
      Pattern.compile("(?:\\+|/|\\.\\.)");

  private final String blacklistPath;

  private Set<String> blacklist = Collections.emptySet();
  private Set<String> blacklistGmail = Collections.emptySet();
  private Set<String> blacklistPartial = Collections.emptySet();
  private Set<Pattern> blacklistRegexp = Collections.emptySet();

  private long emailBlackListFileDate;

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
   * Reads set of forbidden words from file. One word by line. If file not found file reading
   * failed, then logs error.
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
    File blacklistFile = new File(blacklistPath);
    if (blacklistFile.exists()) {
      if (blacklistFile.lastModified() != emailBlackListFileDate) {
        try (InputStream is = new FileInputStream(blacklistFile)) {
          Set<String> blackList = new HashSet<>();
          Set<String> partialBlackList = new HashSet<>();
          Set<String> gmailBlackList = new HashSet<>();
          Set<Pattern> regexpList = new HashSet<>();

          try (Scanner in = new Scanner(is)) {
            while (in.hasNextLine()) {
              String line = in.nextLine().trim();
              if (line.startsWith("regexp:")) {
                regexpList.add(Pattern.compile(line.split("^regexp:", 2)[1]));
              } else if (line.startsWith("*")) {
                partialBlackList.add(line.substring(1).toLowerCase());
              } else if (isGmailAddress(line.toLowerCase())) {
                gmailBlackList.add(getGmailNormalizedLocalPart(line.toLowerCase()));
              } else {
                blackList.add(line.toLowerCase());
              }
            }
          }
          this.blacklist = blackList;
          this.blacklistPartial = partialBlackList;
          this.blacklistGmail = gmailBlackList;
          this.blacklistRegexp = regexpList;

          this.emailBlackListFileDate = blacklistFile.lastModified();
        }
      }
    } else {
      LOG.error("Couldn't read from blacklist: File {} does not exist", blacklistPath);
    }
  }

  public void validateUserMail(String userMail) throws BadRequestException {
    if (userMail == null || userMail.isEmpty()) {
      throw new BadRequestException("User mail can't be null or ''");
    }

    userMail = userMail.toLowerCase();

    if (EMAIL_ILLEGAL_CHARACTERS_PATTERN.matcher(userMail).find()) {
      throw new BadRequestException(
          "User mail must not contain characters like '+','/'or consecutive periods");
    }

    try {
      InternetAddress address = new InternetAddress(userMail);
      address.validate();
    } catch (AddressException e) {
      throw new BadRequestException(
          "E-Mail validation failed. Please check the format of your e-mail address.");
    }

    if (blacklist.contains(userMail)) {
      throw new BadRequestException(String.format("User mail %s is forbidden", userMail));
    }
    if (isGmailAddress(userMail)
        && blacklistGmail.contains(getGmailNormalizedLocalPart(userMail))) {
      throw new BadRequestException(String.format("User mail %s is forbidden", userMail));
    }
    for (String blacklistedPartialEmail : blacklistPartial) {
      if (userMail.endsWith(blacklistedPartialEmail)) {
        throw new BadRequestException(String.format("User mail %s is forbidden", userMail));
      }
    }
    for (Pattern blackListRegexp : blacklistRegexp) {
      if (blackListRegexp.matcher(userMail).find()) {
        throw new BadRequestException(String.format("User mail %s is forbidden", userMail));
      }
    }
  }

  private boolean isGmailAddress(String mail) {
    return mail.endsWith("@gmail.com") || mail.endsWith("@googlemail.com");
  }

  private String getGmailNormalizedLocalPart(String email) {
    return email.split("@")[0].replace(".", "");
  }
}
