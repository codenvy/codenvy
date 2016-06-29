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
package com.codenvy.im.utils;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;

/**
 * @author Anatoliy Bazko
 */
public class Version implements Comparable<Version> {

    private static final String MILESTONE_VERSION_PREFIX = "-M";
    private static final String BETA_VERSION_PREFIX      = "-beta-";
    private static final String RC_VERSION_PREFIX        = "-RC";
    private static final String GA                       = "-GA";
    private static final String SNAPSHOT                 = "-SNAPSHOT";

    private static final Pattern VERSION =
        compile("^(0|[1-9]+[0-9]*)\\.(0|[1-9]+[0-9]*)\\.(0|[1-9]+[0-9]*)(\\.0|\\.[1-9]+[0-9]*|)" +
                "(" + MILESTONE_VERSION_PREFIX + "[1-9]+[0-9]*|)" +
                "(" + BETA_VERSION_PREFIX + "[1-9]+[0-9]*|)" +
                "(" + RC_VERSION_PREFIX + "[1-9]+[0-9]*|)" +
                "(" + GA + "|)" +
                "(" + SNAPSHOT + "|)$");

    private final int     major;
    private final int     minor;
    private final int     bugFix;
    private final int     hotFix;
    private final int     milestone;
    private final int     beta;
    private final int     rc;
    private final boolean isGa;
    private final boolean isSnapshot;

    /**
     * @throws IllegalArgumentException
     */
    public Version(int major,
                   int minor,
                   int bugFix,
                   int hotFix,
                   int milestone,
                   int beta,
                   int rc,
                   boolean isGa,
                   boolean isSnapshot) {
        this.major = major;
        this.minor = minor;
        this.bugFix = bugFix;
        this.hotFix = hotFix;
        this.milestone = milestone;
        this.beta = beta;
        this.rc = rc;
        this.isGa = isGa;
        this.isSnapshot = isSnapshot;
    }

    public static final Version VERSION_3     = Version.valueOf("3.0.0");
    public static final Version VERSION_4_4_0 = Version.valueOf("4.4.0");

    /**
     * Checks if version format is valid.
     */
    public static boolean isValidVersion(String version) {
        return VERSION.matcher(version).matches();
    }

    /**
     * Parse version in string representation.
     *
     * @throws IllegalVersionException
     */
    public static Version valueOf(@NotNull String version) throws IllegalVersionException {
        Matcher matcher = VERSION.matcher(version);
        if (!matcher.find()) {
            throw new IllegalVersionException(version);
        }

        int hotFix = 0;
        int milestone = 0;
        int beta = 0;
        int rc = 0;

        String hotFixGroup = matcher.group(4);
        if (!hotFixGroup.isEmpty()) {
            hotFix = parseInt(hotFixGroup.substring(1));
        }

        String milestoneGroup = matcher.group(5);
        if (!milestoneGroup.isEmpty()) {
            milestone = parseInt(milestoneGroup.substring(MILESTONE_VERSION_PREFIX.length()));
        }

        String betaGroup = matcher.group(6);
        if (!betaGroup.isEmpty()) {
            beta = parseInt(betaGroup.substring(BETA_VERSION_PREFIX.length()));
        }

        String rcGroup = matcher.group(7);
        if (!rcGroup.isEmpty()) {
            rc = parseInt(rcGroup.substring(RC_VERSION_PREFIX.length()));
        }

        boolean isGa = !matcher.group(8).isEmpty();

        boolean isSnapshot = !matcher.group(9).isEmpty();

        return new Version(parseInt(matcher.group(1)),
                           parseInt(matcher.group(2)),
                           parseInt(matcher.group(3)),
                           hotFix,
                           milestone,
                           beta,
                           rc,
                           isGa,
                           isSnapshot);
    }

    /**
     * Checks if version suites for pattern.
     * For example 3.1.0 version is suited for 3.1.* or 3.*.0
     */
    public boolean isSuitedFor(String versionRegex) {
        Pattern pattern = compile(versionRegex);
        return pattern.matcher(toString()).matches();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Version)) {
            return false;
        }

        Version version = (Version)o;

        if (bugFix != version.bugFix) {
            return false;
        }
        if (hotFix != version.hotFix) {
            return false;
        }
        if (major != version.major) {
            return false;
        }
        if (milestone != version.milestone) {
            return false;
        }
        if (beta != version.beta) {
            return false;
        }
        if (minor != version.minor) {
            return false;
        }
        if (rc != version.rc) {
            return false;
        }
        if (isGa != version.isGa) {
            return false;
        }
        if (isSnapshot != version.isSnapshot) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + bugFix;
        result = 31 * result + hotFix;
        result = 31 * result + milestone;
        result = 31 * result + beta;
        result = 31 * result + rc;
        result = 31 * result + (isGa ? 1 : 0);
        result = 31 * result + (isSnapshot ? 1 : 0);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Version o) {
        if (major > o.major
            || (major == o.major && minor > o.minor)
            || (major == o.major && minor == o.minor && bugFix > o.bugFix)
            || (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix > o.hotFix)
            || (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix == o.hotFix
                && (milestone == 0 && o.milestone != 0 || milestone != 0 && o.milestone != 0 && milestone > o.milestone))
            || (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix == o.hotFix && milestone == o.milestone
                && (beta == 0 && o.beta != 0 || beta != 0 && o.beta != 0 && beta > o.beta))
            || (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix == o.hotFix && milestone == o.milestone && beta == 0 && o.beta == 0
                && (rc == 0 && o.rc != 0 || rc != 0 && o.rc != 0 && rc > o.rc))
            || (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix == o.hotFix && milestone == o.milestone
                && beta == o.beta && rc == o.rc && !isSnapshot && o.isSnapshot)
            || (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix == o.hotFix && milestone == o.milestone
                && beta == o.beta && rc == o.rc && !isGa && o.isGa)){
            return 1;
        } else if (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix == o.hotFix
                   && milestone == o.milestone && beta == o.beta && rc == o.rc && isGa == o.isGa && isSnapshot == o.isSnapshot) {
            return 0;
        } else {
            return -1;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return major
               + "." + minor
               + "." + bugFix
               + (hotFix > 0 ? "." + hotFix : "")
               + (milestone > 0 ? MILESTONE_VERSION_PREFIX + milestone : "")
               + (beta > 0 ? BETA_VERSION_PREFIX + beta : "")
               + (rc > 0 ? RC_VERSION_PREFIX + rc : "")
               + (isGa ? GA : "")
               + (isSnapshot ? SNAPSHOT : "");
    }

    public static boolean is4Major(String versionStr) {
        if (versionStr == null) {
            return false;
        }

        try {
            Version version = valueOf(versionStr);
            return version.is4Major();
        } catch(IllegalVersionException e) {
            return false;
        }
    }

    public static boolean is3Major(String versionStr) {
        if (versionStr == null) {
            return false;
        }

        try {
            Version version = valueOf(versionStr);
            return version.is3Major();
        } catch(IllegalVersionException e) {
            return false;
        }
    }

    static public class ReverseOrderComparator implements Comparator<Version> {
        @Override
        public int compare(Version v1, Version v2) {
            return v2.compareTo(v1);
        }
    }

    private int compareToMajor(int majorToCompare) {
        return Integer.compare(this.major, majorToCompare);
    }

    public boolean is3Major() {
        return compareToMajor(3) == 0;
    }

    public boolean is4Major() {
        return compareToMajor(4) == 0;
    }

}
