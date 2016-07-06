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

import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.tck.ProfileDaoTest;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

/**
 * {@link LdapProfileDao} implements {@link ProfileDao} contract not fully,
 * this annotation transformer helps to skip {@link ProfileDaoTest} test methods
 * which test not-implemented contract part.
 *
 * @author Yevhenii Voevodin
 */
public class ProfileDaoTestsSkipper implements IAnnotationTransformer {

    private final Set<String> skipTestMethodNames = new HashSet<>();

    {
        skipTestMethodNames.add("shouldThrowConflictExceptionWhenCreatingProfileThatAlreadyExistsForUserWithGivenId");
        skipTestMethodNames.add("shouldRemoveProfile");
        skipTestMethodNames.add("shouldThrowNpeWhenRemovingNull");
        skipTestMethodNames.add("shouldCreateProfile");
        skipTestMethodNames.add("shouldThrowNpeWhenCreatingNull");
        for (String skipMethodName : skipTestMethodNames) {
            try {
                ProfileDaoTest.class.getMethod(skipMethodName);
            } catch (NoSuchMethodException x) {
                throw new RuntimeException(format("'%s' class doesn't contain method '%s' while it is declared to be skipped",
                                                  ProfileDaoTest.class.getName(),
                                                  skipMethodName));
            }
        }
    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        if (testMethod != null
            && testMethod.getDeclaringClass() == ProfileDaoTest.class
            && skipTestMethodNames.contains(testMethod.getName())) {
            annotation.setEnabled(false);
        }
    }
}
