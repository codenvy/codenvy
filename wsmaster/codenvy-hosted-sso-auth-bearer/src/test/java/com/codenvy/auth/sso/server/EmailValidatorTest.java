package com.codenvy.auth.sso.server;

import org.eclipse.che.api.core.BadRequestException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmailValidatorTest {
  private EmailValidator emailValidator;

  @BeforeMethod
  public void setUp() {
    emailValidator = new EmailValidator(
        getClass().getClassLoader().getResource("email-blacklist").getPath());
  }

  @Test(dataProvider = "validEmails")
  public void shouldValidateEmail(String emails) throws Exception {
    emailValidator.validateUserMail(emails);
  }

  @Test(dataProvider = "blackListedEmails", expectedExceptions = BadRequestException.class)
  public void shouldInvalidateEmail(String emails) throws Exception {
    emailValidator.validateUserMail(emails);
  }

  @DataProvider(name = "validEmails")
  public Object[][] validEmails() {
    return new Object[][] {
        {"..hell..@gmail.com"},
    };
  }

  @DataProvider(name = "blackListedEmails")
  public Object[][] blackListedEmails() {
    return new Object[][] {
        {"hello@gmail.com"},
        {"hello@googlemail.com"},
        {"heLLo@gmail.com"},
        {"hel.lo@gmail.com"},
        {"hel..lo@gmail.com"},
        {"hello@Gmail.com"},
        {"hello@gOOglemail.com"},
        {"h.el.lo.@gOOglemail.com"},
    };
  }
}
