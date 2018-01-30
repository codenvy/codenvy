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

import org.eclipse.che.api.core.BadRequestException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmailValidatorTest {
  private EmailValidator emailValidator;

  @BeforeMethod
  public void setUp() {
    emailValidator =
        new EmailValidator(getClass().getClassLoader().getResource("email-blacklist").getPath());
  }

  @Test(dataProvider = "validEmails")
  public void shouldValidateEmail(String emails) throws Exception {
    emailValidator.validateUserMail(emails);
  }

  @Test(
    dataProvider = "invalidEmails",
    expectedExceptions = BadRequestException.class,
    expectedExceptionsMessageRegExp =
        "User mail must not contain characters like '\\+','\\/'or consecutive periods"
  )
  public void shouldInvalidateEmailWithIllegalCharacters(String emails) throws Exception {
    emailValidator.validateUserMail(emails);
  }

  @Test(
      dataProvider = "blackListedEmails",
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp = "User mail .* is forbidden."
  )
  public void shouldInvalidateBlackListedEmail(String emails) throws Exception {
    emailValidator.validateUserMail(emails);
  }

  @DataProvider(name = "validEmails")
  public Object[][] validEmails() {
    return new Object[][] {
        {"test@gmail.com"},
        {"test.test@gmail.com"},
        {"test@googlemail.com"},
        {"test190@gmail.com"},
        {"test@mail.net"},
        {"user-test@mail.net"},
    };
  }

  @DataProvider(name = "invalidEmails")
  public Object[][] invalidEmails() {
    return new Object[][] {
      {"test..@googlemail.com"},
      {"..test@gmail.com"},
      {"te..st@gmail.com"},
      {"test+@gmail.com"},
      {"test+test@gmail.com"},
      {"test/test@gmail.com"},
      {"test/@gmail.com"},
      {"/test@gmail.com"},
    };
  }

  @DataProvider(name = "blackListedEmails")
  public Object[][] blackListedEmails() {
    return new Object[][] {
      {"banned@gmail.com"},
      {"banned@googlemail.com"},
      {"ban.ned@gmail.com"},
      {"baNnEd@gmail.com"},
      {"banned@Gmail.com"},
      {"ban.ned@googleMaiL.com"},
      {"banned.@gOOglemail.com"},
      {"ba.n.n.ed@gmail.com"},
      {"blacklisted@mail.net"},
      {"new-blacklisted@mail.net"},
      {"NEW-blacklisted@mail.net"},
      {"user-1212@mail.com"},
      {"user-2121@mail.org"},
    };
  }
}
