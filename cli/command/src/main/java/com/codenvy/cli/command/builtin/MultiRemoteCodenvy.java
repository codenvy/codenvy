/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.command.builtin;

import com.codenvy.cli.command.builtin.helper.BeforeAfterAction;
import com.codenvy.cli.command.builtin.model.DefaultUserBuilderStatus;
import com.codenvy.cli.command.builtin.model.DefaultUserProject;
import com.codenvy.cli.command.builtin.model.DefaultUserProjectReference;
import com.codenvy.cli.command.builtin.model.DefaultUserRunnerStatus;
import com.codenvy.cli.command.builtin.model.DefaultUserWorkspace;
import com.codenvy.cli.command.builtin.model.UserBuilderStatus;
import com.codenvy.cli.command.builtin.model.UserProject;
import com.codenvy.cli.command.builtin.model.UserProjectReference;
import com.codenvy.cli.command.builtin.model.UserRunnerStatus;
import com.codenvy.cli.command.builtin.model.UserWorkspace;
import com.codenvy.cli.command.builtin.util.ascii.AsciiArray;
import com.codenvy.cli.command.builtin.util.ascii.AsciiForm;
import com.codenvy.cli.command.builtin.util.ascii.DefaultAsciiArray;
import com.codenvy.cli.command.builtin.util.ascii.DefaultAsciiForm;
import com.codenvy.cli.command.builtin.util.ascii.FormatterMode;
import com.codenvy.cli.command.builtin.util.metadata.CodenvyMetadata;
import com.codenvy.cli.command.builtin.util.zip.ZipUtils;
import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.security.PreferencesDataStore;
import com.codenvy.cli.security.RemoteCredentials;
import com.codenvy.cli.security.TokenRetrieverDatastore;
import com.codenvy.client.Codenvy;
import com.codenvy.client.CodenvyClient;
import com.codenvy.client.CodenvyErrorException;
import com.codenvy.client.CodenvyException;
import com.codenvy.client.Request;
import com.codenvy.client.WorkspaceClient;
import com.codenvy.client.auth.CodenvyAuthenticationException;
import com.codenvy.client.auth.Credentials;
import com.codenvy.client.auth.Token;
import com.codenvy.client.model.BuilderStatus;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.RunnerStatus;
import com.codenvy.client.model.Workspace;
import com.codenvy.client.model.project.ImportResponse;
import com.codenvy.client.model.runner.RunOptionsBuilder;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.CommandSessionHolder;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.codenvy.cli.command.builtin.util.ascii.FormatterMode.MODERN;
import static java.lang.String.format;
import static org.apache.karaf.shell.console.SessionProperties.PRINT_STACK_TRACES;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * @author Florent Benoit
 */
public class MultiRemoteCodenvy {

    /**
     * JSON file extension.
     */
    protected static final String JSON_FILE_EXTENSION = ".json";

    public static enum Importer {
        GIT, ZIP;
    }

    private CodenvyClient codenvyClient;

    private CommandSession session;

    private ConcurrentMap<String, Codenvy> readyRemotes;
    private ConcurrentMap<String, Remote>  availableRemotes;

    private Preferences globalPreferences;

    public MultiRemoteCodenvy(CodenvyClient codenvyClient, Preferences globalPreferences, CommandSession session) {
        this.codenvyClient = codenvyClient;
        this.globalPreferences = globalPreferences;
        this.session = session;
        this.readyRemotes = new ConcurrentHashMap<>();
        this.availableRemotes = new ConcurrentHashMap<>();
        init();
    }

    protected void init() {
        readyRemotes.clear();
        availableRemotes.clear();
        // now read remotes and add a new datastore for each env
        Map preferencesRemotes = globalPreferences.get("remotes", Map.class);
        if (preferencesRemotes != null) {
            Iterator<String> remoteIterator = preferencesRemotes.keySet().iterator();
            Preferences remotesPreferences = globalPreferences.path("remotes");
            while (remoteIterator.hasNext()) {
                String remote = remoteIterator.next();
                // create store
                PreferencesDataStore preferencesDataStore = new PreferencesDataStore(remotesPreferences, remote, codenvyClient);

                // read remote
                Remote remoteData = remotesPreferences.get(remote, Remote.class);
                RemoteCredentials remoteCredentials = remotesPreferences.get(remote, RemoteCredentials.class);

                // If token is available, add it
                if (!remoteCredentials.getToken().isEmpty()) {
                    // add remote env
                    // Manage credentials
                    Codenvy codenvy = codenvyClient.newCodenvyBuilder(remoteData.getUrl(), remoteCredentials.getUsername())
                                                   .withCredentialsProvider(preferencesDataStore)
                                                   .withCredentialsStoreFactory(preferencesDataStore)
                                                   .build();
                    readyRemotes.put(remote, codenvy);
                }

                availableRemotes.put(remote, remoteData);

            }
        }
    }

    protected List<UserProjectReference> getProjects(String remoteName, boolean onlyPublic) {

        Codenvy codenvy = readyRemotes.get(remoteName);
        if (codenvy == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(String.format("The remote %s doesn't exists", remoteName));
            buffer.reset();
            System.out.println(buffer.toString());
            return Collections.emptyList();
        }

        return getProjects(remoteName, codenvy, onlyPublic);
    }


    protected List<UserProjectReference> getProjects() {
        return getProjects(false);
    }


    protected List<UserProjectReference> getProjects(boolean onlyPublic) {
        List<UserProjectReference> projects = new ArrayList<>();

        Set<Map.Entry<String, Codenvy>> entries = readyRemotes.entrySet();
        Iterator<Map.Entry<String, Codenvy>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Codenvy> entry = iterator.next();
            try {
                List<UserProjectReference> foundProjects = getProjects(entry.getKey(), entry.getValue(), onlyPublic);
                if (!foundProjects.isEmpty()) {
                    projects.addAll(foundProjects);
                }
            } catch (CodenvyAuthenticationException e) {
                if (isStackTraceEnabled()) {
                    throw e;
                }
                System.err.println("Authentication token has expired on remote '" + entry.getKey() + "'. Please login again or logout on this remote to refresh the token.");
            }
        }
        return projects;
    }

    /**
     * Find the workspace with the specified name
     *
     * @param name
     *         the workspace name
     * @param remote
     *         the remote name
     * @param codenvy
     *         the codenvy object used to retrieve the data
     * @return the list of workspaces
     */
    protected UserWorkspace getWorkspaceWithName(String name, String remote, Codenvy codenvy) {
        WorkspaceClient workspaceClient = codenvy.workspace();
        Request<Workspace> request = workspaceClient.withName(name);
        Workspace workspace = request.execute();
        if (workspace == null) {
            return null;
        }
        return new DefaultUserWorkspace(remote, this, codenvy, workspace);
    }

    /**
     * Gets list of all workspaces for the current user
     *
     * @param remote
     *         the remote name
     * @param codenvy
     *         the codenvy object used to retrieve the data
     * @return the list of workspaces
     */
    protected List<UserWorkspace> getWorkspaces(String remote, Codenvy codenvy) {
        List<UserWorkspace> workspaces = new ArrayList<>();

        // get all workspaces

        WorkspaceClient workspaceClient = codenvy.workspace();
        Request<List<Workspace>> request = workspaceClient.all();
        List<Workspace> readWorkspaces = request.execute();

        for (Workspace workspace : readWorkspaces) {
            workspaces.add(new DefaultUserWorkspace(remote, this, codenvy, workspace));

        }
        return workspaces;
    }

    /**
     * Gets list of all projects for the current user
     *
     * @param codenvy
     *         the codenvy object used to retrieve the data
     * @return the list of projects
     */
    protected List<UserProjectReference> getProjects(String remote, Codenvy codenvy, boolean onlyPublic) {
        List<UserProjectReference> projects = new ArrayList<>();

        // For each workspace, search the project and compute

        WorkspaceClient workspaceClient = codenvy.workspace();
        Request<List<Workspace>> request = workspaceClient.all();
        List<Workspace> readWorkspaces;
        try {
             readWorkspaces = request.execute();
        } catch (CodenvyAuthenticationException e) {
            if (isStackTraceEnabled()) {
                throw e;
            }
            System.err.println("Authentication token has expired on remote '" + remote + "'. Please login again or logout on this remote to refresh the token.");
            return projects;
        } catch (CodenvyErrorException | CodenvyException e) {
            if (isStackTraceEnabled()) {
                throw e;
            }
            System.err.println("Error while retrieving projects on remote '" + remote + "'");
            return projects;
        }

        for (Workspace workspace : readWorkspaces) {

            DefaultUserWorkspace defaultUserWorkspace = new DefaultUserWorkspace(remote, this, codenvy, workspace);

            List<ProjectReference> readProjects = codenvy.project().getWorkspaceProjects(workspace.id()).execute();
            for (ProjectReference readProject : readProjects) {
                // skip private projects
                if (onlyPublic && "private".equals(readProject.visibility())) {
                    continue;
                }
                DefaultUserProjectReference project = new DefaultUserProjectReference(codenvy, readProject, defaultUserWorkspace);
                projects.add(project);
            }
        }
        return projects;
    }


    /**
     * Allows to search a project
     */
    protected UserProjectReference getProjectReference(String shortId) {
        if (shortId == null || shortId.length() < 2) {
            throw new IllegalArgumentException("The identifier should at least contain two digits");
        }


        // get all projects
        List<UserProjectReference> projects = getProjects();

        // no projects
        if (projects.isEmpty()) {
            return null;
        }

        // now search in the given projects
        List<UserProjectReference> matchingProjects = new ArrayList<>();
        for (UserProjectReference project : projects) {
            // match
            if (project.shortId().startsWith(shortId)) {
                matchingProjects.add(project);
            }
        }

        // No matching project
        if (matchingProjects.isEmpty()) {
            return null;
        } else if (matchingProjects.size() == 1) {
            // one matching project
            return matchingProjects.get(0);
        } else {
            throw new IllegalArgumentException("Too many matching projects. Try with a longer identifier");
        }


    }


    /**
     * Allows to search a project
     */
    protected UserProject getProject(String shortId) {
       UserProjectReference projectReference = getProjectReference(shortId);
        if (projectReference == null) {
            return null;
        }


        Project project = projectReference.getCodenvy().project().getProject(projectReference.getWorkspace().id(), projectReference.getInnerReference()).execute();
        if (project == null) {
            return null;
        }

        return new DefaultUserProject(projectReference.getCodenvy(), projectReference, project, projectReference.getWorkspace());
    }


    public boolean hasAvailableRemotes() {
        return !availableRemotes.isEmpty();
    }

    public boolean hasReadyRemotes() {
        return !readyRemotes.isEmpty();
    }

    public Collection<String> getRemoteNames() {
        return availableRemotes.keySet();
    }
    public Map<String, Remote> getAvailableRemotes() {
        return availableRemotes;
    }
    public Map<String, Codenvy> getReadyRemotes() {
        return readyRemotes;
    }

    public String listRemotes() {
        Ansi buffer = Ansi.ansi();
        buffer.a(INTENSITY_BOLD).a("REMOTES\n").a(INTENSITY_BOLD_OFF);
        buffer.reset();

        Map<String, Remote> envs = getAvailableRemotes();

        AsciiForm asciiForm = buildAsciiForm();

        Iterator<Map.Entry<String, Remote>> it = envs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Remote> entry = it.next();
            String isDefault = "";
            if (entry.getValue().isDefaultRemote()) {
                isDefault = "*";
            }
            asciiForm.withEntry("  ".concat(entry.getKey().concat(isDefault)), "[".concat(entry.getValue().getUrl().concat("]")));
        }

        buffer.a(asciiForm.toAscii());

        return buffer.toString();
    }


    public boolean addRemote(String name, String url) {
        // check env doesn't exists
        if (getRemoteNames().contains(name)) {
            System.out.println("The remote with name '" + name + "' already exists");
            return false;
        }

        Preferences preferencesRemotes = globalPreferences.path("remotes");


        // add the new remote
        Remote remote = new Remote();
        remote.setUrl(url);
        preferencesRemotes.put(name, remote);

        // refresh current links
        refresh();

        return true;

    }

    public Remote getRemote(String remoteName) {
        // it exists, get it
        Preferences preferencesRemotes = globalPreferences.path("remotes");


        if (!getRemoteNames().contains(remoteName)) {
            return null;
        }

        return preferencesRemotes.get(remoteName, Remote.class);
    }


    public Remote getDefaultRemote() {
        Map preferencesRemotes = globalPreferences.get("remotes", Map.class);
        if (preferencesRemotes != null) {
            Iterator<String> remoteIterator = preferencesRemotes.keySet().iterator();
            Preferences remotesPreferences = globalPreferences.path("remotes");
            while (remoteIterator.hasNext()) {
                String remote = remoteIterator.next();

                Remote tmpEnv = remotesPreferences.get(remote, Remote.class);
                if (tmpEnv.isDefaultRemote()) {
                    return tmpEnv;
                }
            }
        }

        // No remote is an error case
        throw new IllegalStateException("No default remote found. Needs to use --remote to specify a remote");
    }

    public String getDefaultRemoteName() {
        Map preferencesRemotes = globalPreferences.get("remotes", Map.class);
        if (preferencesRemotes != null) {
            Iterator<String> remoteIterator = preferencesRemotes.keySet().iterator();
            Preferences remotesPreferences = globalPreferences.path("remotes");
            while (remoteIterator.hasNext()) {
                String remote = remoteIterator.next();

                Remote tmpRemote = remotesPreferences.get(remote, Remote.class);
                if (tmpRemote.isDefaultRemote()) {
                    return remote;
                }
            }
        }
        // No remote is an error case
        throw new IllegalStateException("No default remote found. Needs to use --remote to specify a remote");
    }

    /**
     * Login by using a token instead of a password
     * @param remoteName the remote on which we want to log in
     * @param username the username (email)
     * @param token the provided token
     * @return true if login is successful
     */
    public boolean login(String remoteName, String username, Token token) {

        // get URL of the remote
        Remote remote;

        if (remoteName == null) {
            remote = getDefaultRemote();
            remoteName = getDefaultRemoteName();
        } else {
            remote = getRemote(remoteName);
        }

        if (remote == null) {
            System.out.println("Unable to find the given remote '" + remoteName + "'");
            return false;
        }


        String url = remote.getUrl();


        // check that this is valid
        Credentials codenvyCredentials = codenvyClient.newCredentialsBuilder()
                                                      .withUsername(username)
                                                      .withToken(token)
                                                      .build();

        TokenRetrieverDatastore tokenRetrieverDatastore = new TokenRetrieverDatastore();
        tokenRetrieverDatastore.put(username, codenvyCredentials);


        Codenvy codenvy = codenvyClient.newCodenvyBuilder(url, username)
                                       .withCredentials(codenvyCredentials)
                                       .withCredentialsStoreFactory(tokenRetrieverDatastore)
                                       .build();

        // try to connect to the remote side
        try {
            codenvy.user().current().execute();
        } catch (CodenvyException e) {
            if (isStackTraceEnabled()) {
                throw e;
            }
            System.out.println("Unable to authenticate for the given credentials on URL '" + url + "'. Check the provided token.");
            // invalid login so we don't add env
            return false;
        }

        // get token
        Token tokenRetrieval = tokenRetrieverDatastore.getToken();
        if (tokenRetrieval == null) {
            System.out.println("Unable to validate token on URL '" + url + "'");
            // invalid login so we don't add env
            return false;
        }


        // Save credentials
        Preferences preferencesRemotes = globalPreferences.path("remotes");
        RemoteCredentials remoteCredentials = new RemoteCredentials();
        remoteCredentials.setToken(token.value());
        remoteCredentials.setUsername(username);
        // by merging
        preferencesRemotes.merge(remoteName, remoteCredentials);

        // refresh current links
        refresh();

        return true;
    }


    public boolean login(String remoteName, String username, String password) {

        // get URL of the remote
        Remote remote;

        if (remoteName == null) {
            remote = getDefaultRemote();
            remoteName = getDefaultRemoteName();
        } else {
            remote = getRemote(remoteName);
        }

        if (remote == null) {
            System.out.println("Unable to find the given remote '" + remoteName + "'");
            return false;
        }


        String url = remote.getUrl();

        TokenRetrieverDatastore tokenRetrieverDatastore = new TokenRetrieverDatastore();

        // check that this is valid
        Credentials codenvyCredentials = codenvyClient.newCredentialsBuilder()
                                                      .withUsername(username)
                                                      .withPassword(password)
                                                      .build();
        Codenvy codenvy = codenvyClient.newCodenvyBuilder(url, username)
                                       .withCredentials(codenvyCredentials)
                                       .withCredentialsStoreFactory(tokenRetrieverDatastore)
                                       .build();

        // try to connect to the remote side
        try {
            codenvy.user().current().execute();
        } catch (CodenvyException e) {
            if (isStackTraceEnabled()) {
                throw e;
            }
            System.out.println("Unable to authenticate for the given credentials on URL '" + url + "'. Check the username and password.");
            // invalid login so we don't add env
            return false;
        }

        // get token
        Token token = tokenRetrieverDatastore.getToken();
        if (token == null) {
            System.out.println("Unable to get token for the given credentials on URL '" + url + "'");
            // invalid login so we don't add env
            return false;
        }


        // Save credentials
        Preferences preferencesRemotes = globalPreferences.path("remotes");
        RemoteCredentials remoteCredentials = new RemoteCredentials();
        remoteCredentials.setToken(token.value());
        remoteCredentials.setUsername(username);
        // by merging
        preferencesRemotes.merge(remoteName, remoteCredentials);

        // refresh current links
        refresh();

        return true;
    }


    /**
     * Logout to the given remote cloud
     * @param remoteName the remote name
     * @return true if logout successfully
     */
    public boolean logout(String remoteName) {

        // get URL of the remote
        Remote remote;

        if (remoteName == null) {
            remote = getDefaultRemote();
            remoteName = getDefaultRemoteName();
        } else {
            remote = getRemote(remoteName);
        }

        if (remote == null) {
            System.out.println("Unable to find the given remote '" + remoteName + "'");
            return false;
        }

        // Get current credentials
        Preferences preferencesRemotes = globalPreferences.path("remotes");
        RemoteCredentials remoteCredentials = preferencesRemotes.get(remoteName, RemoteCredentials.class);

        // disable token
        remoteCredentials.setToken("");

        // merge it
        preferencesRemotes.merge(remoteName, remoteCredentials);

        // refresh current links
        refresh();

        return true;
    }


    protected void refresh() {
        init();
    }


    protected boolean removeRemote(String name) {
        // check env does exists
        if (!getRemoteNames().contains(name)) {
            System.out.println("The remote with name '" + name + "' does not exists");
            return false;
        }

        // it exists, remove it
        Preferences preferencesRemotes = globalPreferences.path("remotes");

        Remote remote = getRemote(name);
        if (remote.isDefaultRemote()) {
            System.out.println("The default remote cannot be removed.");
            return false;
        }

        // delete
        preferencesRemotes.delete(name);

        // refresh current links
        refresh();

        // OK
        return true;
    }

    protected boolean setDefaultRemote(String name) {
        // check env does exists
        if (!getRemoteNames().contains(name)) {
            System.out.println("The remote with name '" + name + "' does not exists");
            return false;
        }

        // Iterate on each remote, remove default except for the expected one
        Map preferencesRemotes = globalPreferences.get("remotes", Map.class);
        if (preferencesRemotes != null) {

            //  Iterate
            Iterator<String> remoteIterator = preferencesRemotes.keySet().iterator();
            Preferences remotesPreferences = globalPreferences.path("remotes");
            while (remoteIterator.hasNext()) {
                String remote = remoteIterator.next();
                Remote tmpEnv = remotesPreferences.get(remote, Remote.class);
                tmpEnv.setDefaultRemote(name.equals(remote));
                remotesPreferences.merge(remote, tmpEnv);
            }
        }

        // refresh current links
        refresh();

        // OK
        return true;
    }

    public List<UserBuilderStatus> findBuilders(String builderID) {
        // first collect all processes
        List<UserProjectReference> projects = getProjects();

        List<UserBuilderStatus> matchingStatuses = new ArrayList<>();

        // then for each project, gets the builds IDs
        for (UserProjectReference userProjectReference : projects) {
            final List<BuilderStatus> builderStatuses = userProjectReference.getCodenvy().builder().builds(userProjectReference.getInnerReference()).execute();
            for (BuilderStatus builderStatus : builderStatuses) {

                UserBuilderStatus tmpStatus = new DefaultUserBuilderStatus(builderStatus, userProjectReference);
                if (tmpStatus.shortId().startsWith(builderID)) {
                    matchingStatuses.add(tmpStatus);
                }
            }
        }
        return matchingStatuses;
    }


    public List<UserRunnerStatus> findRunners(String runnerID) {
        List<UserProjectReference> projects = getProjects();

        List<UserRunnerStatus> matchingStatuses = new ArrayList<>();

        // then for each project, gets the process IDs
        for (UserProjectReference userProjectReference : projects) {
            final List<RunnerStatus> runnerStatuses =
                    userProjectReference.getCodenvy().runner().processes(userProjectReference.getInnerReference()).execute();
            for (RunnerStatus runnerStatus : runnerStatuses) {

                UserRunnerStatus tmpStatus = new DefaultUserRunnerStatus(runnerStatus, userProjectReference);
                if (tmpStatus.shortId().startsWith(runnerID)) {
                    matchingStatuses.add(tmpStatus);
                }
            }
        }

        return matchingStatuses;
    }

    /**
     * Gets project permissions
     * @param userProjectReference
     * @return
     */
    public List<String> getProjectPermissions(UserProjectReference userProjectReference) {
        Project project = userProjectReference.getCodenvy().project().getProject(userProjectReference.getWorkspace().id(), userProjectReference.getInnerReference()).execute();
        if (project == null) {
            return Collections.emptyList();
        }
        return project.permissions();
    }


    public List<UserRunnerStatus> getRunners(UserProjectReference userProjectReference) {
        List<UserRunnerStatus> statuses = new ArrayList<>();
        final List<RunnerStatus> runnerStatuses = userProjectReference.getCodenvy().runner().processes(userProjectReference.getInnerReference()).execute();
        for (RunnerStatus runnerStatus : runnerStatuses) {

            UserRunnerStatus tmpStatus = new DefaultUserRunnerStatus(runnerStatus, userProjectReference);
                statuses.add(tmpStatus);
        }
        return statuses;
    }

    public List<UserBuilderStatus> getBuilders(UserProjectReference userProjectReference) {
        List<UserBuilderStatus> statuses = new ArrayList<>();
        final List<BuilderStatus> builderStatuses = userProjectReference.getCodenvy().builder().builds(userProjectReference.getInnerReference()).execute();
        for (BuilderStatus builderStatus : builderStatuses) {

            UserBuilderStatus tmpStatus = new DefaultUserBuilderStatus(builderStatus, userProjectReference);
            statuses.add(tmpStatus);
        }
        return statuses;
    }


    protected static <T> T checkOnlyOne(List<T> list, String id, String textNoIdentifier, String textTooManyIDs) {
        if (list.isEmpty()) {
            errorNoIdentifier(id, textNoIdentifier);
            return null;
        } else if (list.size() > 1) {
            errorTooManyIdentifiers(id, textTooManyIDs);
            return null;
        }

        return list.get(0);
    }


    /**
     * Display error if there are too many identifiers that have been found
     * @param text a description of the identifier
     */
    protected static void errorTooManyIdentifiers(String id, String text) {
        Ansi buffer = Ansi.ansi();
        buffer.fg(RED);
        buffer.a("Too many ").a(text).a(" have been found with identifier '").a(id).a("'. Please add extra data to the identifier");
        buffer.reset();
        System.out.println(buffer.toString());
    }

    /**
     * Display error if no identifier has been found
     * @param text a description of the identifier
     */
    protected static void errorNoIdentifier(String id, String text) {
        Ansi buffer = Ansi.ansi();
        buffer.fg(RED);
        buffer.a("No ").a(text).a(" found with identifier '").a(id).a("'.");
        buffer.reset();
        System.out.println(buffer.toString());
    }


    public UserWorkspace getUserWorkspace(String remote, String workspaceName) {

        // Remote ?
        if (remote == null) {
            remote = getDefaultRemoteName();
        }

        // try to connect to the remote
        if (getRemote(remote) == null) {
            System.out.println(format("The remote named %s doesn't exists", remote));
            return null;
        }

        // check if remote is ready
        Codenvy remoteCodenvy = getCodenvy(remote);
        if (remoteCodenvy == null) {
            System.out.println(format("The remote named %s is not yet available. Need to login first", remote));
            return null;
        }


        UserWorkspace userWorkspace;
        List<UserWorkspace> workspaces = getWorkspaces(remote, remoteCodenvy);

        if (workspaceName == null) {
            if (workspaces.size() > 1) {
                System.out.println(format("Too many workspaces in the remote %s. Please specify the name of the workspace", remote));
                return null;
            } else if (workspaces.isEmpty()) {
                System.out.println("No workspace found in the remote %s. Please specify another remote or create a workspace first");
                return null;
            }
            userWorkspace = workspaces.get(0);
        } else {
            // needs to find the workspace
            userWorkspace = getWorkspaceWithName(workspaceName, remote, remoteCodenvy);
            if (userWorkspace == null) {
                System.out.println(format("The workspace with name %s has not been found in the remote %s", workspaceName, remote));
                return null;
            }
        }
        return userWorkspace;
    }


    /**
     *
     * @param workspace
     * @param name
     * @param param
     * @param importerType
     * @return
     */
    protected UserProjectReference importProject(UserWorkspace workspace, String name, String param, String importerType, Path configurationPath) {

        //
        String projectName = name;

        // needs to analyze param

        // 1]
        // param is available on the current filesystem

        File file = new File(param);
        InputStream streamToSend;
        boolean isDirectory = false;
        if (file.exists()) {

            if (projectName == null) {
                projectName = file.getName();
            }

            // this is on the filesystem
            if (file.isFile()) {


                // 2a] json file ?
                if (file.getName().endsWith(JSON_FILE_EXTENSION)) {

                    // remove .json extension
                    projectName = projectName.substring(0, projectName.length() - JSON_FILE_EXTENSION.length());

                    BeforeAfterAction beforeAfterAction = new BeforeAfterAction("Creating project from JSON file...", "Project created.");
                    Request<ImportResponse> importResponseRequest = workspace.getCodenvy().project().importProject(workspace.id(), projectName, file.toPath());
                    ImportResponse importResponse = beforeAfterAction.execute(importResponseRequest);
                    Project project = importResponse.project();


                    // Update it
                    beforeAfterAction = new BeforeAfterAction("Updating project configuration...", "Project configuration updated.");
                    Request<Project> updateRequest = workspace.getCodenvy().project().updateProject(project, file.toPath());
                    beforeAfterAction.execute(updateRequest);

                    return new DefaultUserProjectReference(workspace.getCodenvy(), project, workspace);
                }



                // 2] zip file ?
                if (!file.getName().endsWith(".zip")) {
                    Ansi buffer = Ansi.ansi();
                    buffer.fg(RED);
                    buffer.a("The given file '").a(file.getName()).a("' should be a zip file.");
                    buffer.reset();
                    System.out.println(buffer.toString());
                    return null;
                }
                streamToSend = ZipUtils.getZipProjectStream(file);

            } else {
                // 3] directory, need to zip the content of this directory
                isDirectory = true;
                streamToSend = ZipUtils.getZipProjectStream(file);
            }


            // needs to create project
            ProjectReference projectToCreate =
                    codenvyClient.newProjectBuilder().withName(projectName).withWorkspaceId(workspace.id()).withWorkspaceName(workspace.name()).withType(
                            "blank").build();

            BeforeAfterAction beforeAfterAction = new BeforeAfterAction("Creating project from '" + file.getName() + "' ...", "Project created.");
            Request<Project> createRequest = workspace.getCodenvy().project().create(projectToCreate);
            try {
                beforeAfterAction.execute(createRequest);
            } catch (CodenvyErrorException | CodenvyException e) {
                if (isStackTraceEnabled()) {
                    throw e;
                }
                System.out.println("Unable to create the project:" + e.getMessage());
                return null;
            }


            // Project has been created, send the data
            beforeAfterAction = new BeforeAfterAction("Uploading data from '" + file.getName() + "' ...", "Data uploaded.");
            Request<Void> importArchiveRequest = workspace.getCodenvy().project().importArchive(workspace.id(), projectToCreate, streamToSend);
            beforeAfterAction.execute(importArchiveRequest);

            if (configurationPath != null) {
                // Update it
                beforeAfterAction = new BeforeAfterAction("Updating project configuration...", "Project configuration updated.");
                Request<Project> updateRequest = workspace.getCodenvy().project().updateProject(projectToCreate, configurationPath);
                beforeAfterAction.execute(updateRequest);
            }

            UserProjectReference userProjectReference = new DefaultUserProjectReference(workspace.getCodenvy(), projectToCreate, workspace);


            // if directory, add Codenvy metadata in order to sync the project
            if (isDirectory) {
                storeMetadata(userProjectReference, file);
            }

            return userProjectReference;


        } else {

            // 4]
            // not a file. is it a URL
            URI uri;
            try {
                uri = new URI(param);
            } catch (URISyntaxException e) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a("The given value '").a(param).a("' is neither a local path nor a valid URI.");
                buffer.reset();
                System.out.println(buffer.toString());
                return null;
            }

            Importer importer = null;
            if (importerType != null) {
                importer = Importer.valueOf(importerType.toUpperCase(Locale.ENGLISH));
            } else {
                // try to autodetect type

                if ("git".equals(uri.getScheme())) {
                    importer = Importer.GIT;
                } else if (uri.toString().contains("github.com")) {
                    importer = Importer.GIT;
                }


                if (uri.toString().endsWith(".zip")) {
                    importer = Importer.ZIP;
                }
            }

            // is it a git
            if (importer == null) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a("Importer not given and auto-detect of the import type was not possible. Please provide the importer.");
                buffer.reset();
                System.out.println(buffer.toString());
                return null;
            }

            // find name if not defined
            if (projectName == null) {
                projectName = uri.getPath().replace(".", "_").replace("/", "_");
            }


            // Create factory content
            String factoryContent = "{\n" +
                                    "   \"v\" : \"2.0\",\n" +
                                    "    \"source\" : { \"project\" : { \"location\" : \"$LOCATION$\",\n" +
                                    "          \"type\" : \"$IMPORTER$\"\n" +
                                    "        }\n" +
                                    "    }\n" +
                                    "}\n";

            factoryContent = factoryContent.replace("$LOCATION$", param).replace("$IMPORTER$", importer.name().toLowerCase(Locale.ENGLISH));

            // Dump the content
            Path factoryPath;
            try {
                // create
                factoryPath = Files.createTempFile(new File(System.getProperty("java.io.tmpdir")).toPath(), "factory", JSON_FILE_EXTENSION);

                // dump
                Files.write(factoryPath, factoryContent.getBytes(Charset.defaultCharset()));
            } catch (IOException e) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a("Unable to generate factory content." + e.getMessage());
                buffer.reset();
                System.out.println(buffer.toString());
                if (isStackTraceEnabled()) {
                    throw new IllegalArgumentException("Unable to generate factory content", e);
                }
                return null;
            }

            BeforeAfterAction beforeAfterAction = new BeforeAfterAction("Creating project from remote URI '" + uri.toString() + "' ...", "Project created.");
            Request<ImportResponse> importResponseRequest = workspace.getCodenvy().project().importProject(workspace.id(), projectName, factoryPath);
            ImportResponse importResponse = beforeAfterAction.execute(importResponseRequest);
            Project project = importResponse.project();

            if (configurationPath != null) {
                // Update it
                beforeAfterAction = new BeforeAfterAction("Updating project configuration...", "Project updated.");
                Request<Project> updatedRequest = workspace.getCodenvy().project().updateProject(project, configurationPath);
                beforeAfterAction.execute(updatedRequest);
            }

            return new DefaultUserProjectReference(workspace.getCodenvy(), project, workspace);
        }

    }




    /**
     * Creates a project with the given name
     * @param name the name of the project to create
     * @param workspaceName the name of the workspace
     * @param remoteName the name of the remote
     * @param configurationPath the configuration Factory JSON file
     * @return the reference to the created project
     */
    protected UserProjectReference createProject(String name, String workspaceName, String remoteName, Path configurationPath) {

        // Remote ?
        if (remoteName == null) {
            remoteName = getDefaultRemoteName();
        }

        // try to connect to the remote
        if (getRemote(remoteName) == null) {
            System.out.println(format("The remote named %s doesn't exists", remoteName));
            return null;
        }

        // check if remote is ready
        Codenvy remoteCodenvy = readyRemotes.get(remoteName);
        if (remoteCodenvy == null) {
            System.out.println(format("The remote named %s is not yet available. Need to login first", remoteName));
            return null;
        }

        UserWorkspace userWorkspace;

        if (workspaceName == null) {
            List<UserWorkspace> workspaces = getWorkspaces(remoteName, remoteCodenvy);
            if (workspaces.size() > 1) {
                System.out.println(format("Too many workspaces in the remote %s. Please specify the name of the workspace", remoteName));
                return null;
            } else if (workspaces.isEmpty()) {
                System.out.println("No workspace found in the remote %s. Please specify another remote or create a workspace first");
                return null;
            }
            userWorkspace = workspaces.get(0);
        } else {
            // needs to find the workspace
            userWorkspace = getWorkspaceWithName(workspaceName, remoteName, remoteCodenvy);
            if (userWorkspace == null) {
                System.out.println(format("The workspace with name %s has not been found in the remote %s", workspaceName, remoteName));
                return null;
            }
        }

        // Let's create the project

        // OK, now we have everything, we can create the project
        ProjectReference projectToCreate =
                codenvyClient.newProjectBuilder().withName(name).withWorkspaceId(userWorkspace.id()).withWorkspaceName(
                        workspaceName).withType("blank").build();

        System.out.print("Creating project...");
        try {
            remoteCodenvy.project().create(projectToCreate).execute();
        } catch (CodenvyErrorException e) {
            if (isStackTraceEnabled()) {
                throw e;
            }
            System.out.println("Unable to create the project:" + e.getMessage());
            return null;
        }
        System.out.println("done !");

        List<ProjectReference> projects = remoteCodenvy.project().getWorkspaceProjects(userWorkspace.id()).execute();
        ProjectReference newProject = null;
        for (ProjectReference project : projects) {
            if (name.equals(project.name())) {
                newProject = project;
                break;
            }
        }

        if (newProject == null) {
            return null;
        }

        UserProjectReference builtUserProjectReference = new DefaultUserProjectReference(remoteCodenvy, newProject, userWorkspace);


        // Update it
        System.out.print("Now updating project configuration...");
        remoteCodenvy.project().updateProject(builtUserProjectReference.getInnerReference(), configurationPath);
        System.out.println("done !");

        return builtUserProjectReference;

    }

    protected RunOptionsBuilder getRunOptionsBuilder() {
        return codenvyClient.newRunOptionsBuilder();
    }

    protected boolean isStackTraceEnabled() {
        // gets the current session
        CommandSession commandSession = CommandSessionHolder.getSession();
        if (commandSession != null) {
            Boolean val = (Boolean)commandSession.get(PRINT_STACK_TRACES);
            if (val != null && val.booleanValue()) {
                return true;
            }
        }
        return false;
    }


    /**
     * @return the current formatter mode used at runtime
     */
    protected FormatterMode getFormatterMode() {
        FormatterMode formatterMode = (FormatterMode) session.get(FormatterMode.class.getName());
        if (formatterMode == null) {
            formatterMode = MODERN;
        }
        return formatterMode;
    }

    /**
     * Build a new Ascii array instance with the selected formatter mode
     * @return a new instance of the ascii array
     */
    protected AsciiArray buildAsciiArray() {
        return new DefaultAsciiArray().withFormatter(getFormatterMode());
    }

    /**
     * Build a new Ascii array instance with the selected formatter mode
     * @return a new instance of the ascii array
     */
    protected AsciiForm buildAsciiForm() {
        return new DefaultAsciiForm().withFormatter(getFormatterMode());
    }


    protected Codenvy getCodenvy(String remoteName) {
        return readyRemotes.get(remoteName);
    }


    protected boolean storeMetadata(UserProjectReference project, File dest) {
        try {
            CodenvyMetadata codenvyMetadata = new CodenvyMetadata(project, dest);
            codenvyMetadata.write();
        } catch (Exception e) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("Unable to write metadata in pulled project " + project.name());
            buffer.reset();
            System.out.println(buffer.toString());
            return false;
        }
        return true;
    }

}
