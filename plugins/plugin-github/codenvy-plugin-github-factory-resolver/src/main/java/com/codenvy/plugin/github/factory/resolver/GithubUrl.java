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
package com.codenvy.plugin.github.factory.resolver;

import com.google.common.base.Strings;

/**
 * Representation of a github URL, allowing to get details from it.
 * <p> like
 * https://github.com/<username>/<repository>
 * https://github.com/<username>/<repository>/tree/<branch>
 *
 * @author Florent Benoit
 */
public class GithubUrl {

    /**
     * Master branch is the default.
     */
    private static final String DEFAULT_BRANCH_NAME = "master";

    /**
     * Username part of github URL
     */
    private String username;

    /**
     * Repository part of the URL.
     */
    private String repository;

    /**
     * Branch name (by default if it is omitted it is "master" branch)
     */
    private String branch = DEFAULT_BRANCH_NAME;

    /**
     * subfolder if any
     */
    private String subfolder;

    /**
     * Creation of this instance is made by the parser so user may not need to create a new instance directly
     */
    protected GithubUrl() {

    }

    /**
     * Gets username of this github url
     *
     * @return the username part
     */
    public String username() {
        return this.username;
    }

    public GithubUrl username(String userName) {
        this.username = userName;
        return this;
    }

    /**
     * Gets repository of this github url
     *
     * @return the repository part
     */
    public String repository() {
        return this.repository;
    }

    protected GithubUrl repository(String repository) {
        this.repository = repository;
        return this;
    }

    /**
     * Gets branch of this github url
     *
     * @return the branch part
     */
    public String branch() {
        return this.branch;
    }

    protected GithubUrl branch(String branch) {
        if (!Strings.isNullOrEmpty(branch)) {
            this.branch = branch;
        }
        return this;
    }

    /**
     * Gets subfolder of this github url
     *
     * @return the subfolder part
     */
    public String subfolder() {
        return this.subfolder;
    }

    /**
     * Sets the subfolder represented by the URL.
     *
     * @param subfolder
     *         path inside the repository
     * @return current github instance
     */
    protected GithubUrl subfolder(String subfolder) {
        this.subfolder = subfolder;
        return this;
    }

    /**
     * Provides the location to codenvy dockerfile
     *
     * @return location of dockerfile in a repository
     */
    protected String codenvyDockerFileLocation() {
        return "https://raw.githubusercontent.com/" + this.username + "/" + this.repository + "/" + this.branch() + "/.codenvy.dockerfile";
    }

    /**
     * Provides the location to codenvy factory json file
     *
     * @return location of codenvy factory json file in a repository
     */
    protected String codenvyFactoryJsonFileLocation() {
        return "https://raw.githubusercontent.com/" + this.username + "/" + this.repository + "/" + this.branch() + "/.codenvy.json";
    }

    /**
     * Provides location to the repository part of the full github URL.
     *
     * @return location of the repository.
     */
    protected String repositoryLocation() {
        return "https://github.com/" + this.username + "/" + this.repository;
    }


}
