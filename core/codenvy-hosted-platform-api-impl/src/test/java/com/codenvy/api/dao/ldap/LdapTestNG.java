/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.dao.ldap;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class LdapTestNG implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        result.getTestContext().setAttribute("ldap_server_url", "user123");
        System.out.println("START");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("SUCCESS TEST");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("TEST FAILURE");
    }

    @Override
    public void onTestSkipped(ITestResult result) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("->>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("->>>>>>>>>>>>>>>>>>>>");
    }
}
