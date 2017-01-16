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
package com.codenvy.im.cli.command;

import com.codenvy.cli.command.builtin.AbsCommand;
import com.codenvy.cli.command.builtin.MultiRemoteCodenvy;
import com.codenvy.cli.preferences.Preferences;
import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.cli.preferences.CodenvyOnpremPreferences;
import com.codenvy.im.console.Console;
import com.codenvy.im.event.Event;
import com.codenvy.im.facade.IMArtifactLabeledFacade;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.response.DownloadArtifactInfo;
import com.codenvy.im.response.DownloadProgressResponse;
import com.codenvy.im.response.InstallArtifactInfo;
import com.codenvy.im.response.InstallArtifactStepInfo;
import com.codenvy.im.response.UpdateArtifactInfo;
import com.codenvy.im.utils.Version;
import org.eclipse.che.dto.server.DtoFactory;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static com.codenvy.im.utils.InjectorBootstrap.INJECTOR;
import static java.lang.String.format;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public abstract class AbstractIMCommand extends AbsCommand {
    private IMArtifactLabeledFacade  facade;
    private ConfigManager            configManager;
    private CodenvyOnpremPreferences codenvyOnpremPreferences;
    private Console                  console;

    private static final Logger LOG = Logger.getLogger(AbstractIMCommand.class.getSimpleName());  // use java.util.logging instead of slf4j

    protected static boolean updateImClientDone;

    public AbstractIMCommand() {
        facade = INJECTOR.getInstance(IMArtifactLabeledFacade.class);
        configManager = INJECTOR.getInstance(ConfigManager.class);
    }

    @Override
    public void init() {
        super.init();

        initConsole();
        initDtoFactory();
        initCodenvyOnpremPreferences();
    }

    @Override
    protected Void execute() throws Exception {
        try {
            init();
            if (!updateImClientDone) {
                updateImCliClientIfNeeded();
                updateImClientDone = true;
            }

            doExecuteCommand();
        } catch (Exception e) {
            getConsole().printErrorAndExit(e);
        } finally {
            getConsole().reset();
        }

        return null;
    }

    protected abstract void doExecuteCommand() throws Exception;

    protected IMArtifactLabeledFacade getFacade() {
        return facade;
    }

    protected ConfigManager getConfigManager() {
        return configManager;
    }

    protected CodenvyOnpremPreferences getCodenvyOnpremPreferences() {
        return codenvyOnpremPreferences;
    }

    protected Console getConsole() {
        return console;
    }

    protected void logEventToSaasCodenvy(Event event) {
        try {
            getFacade().logSaasAnalyticsEvent(event);
        } catch(Exception e) {
            LOG.log(Level.WARNING, "Error of logging event to SaaS Codenvy: " + e.getMessage(), e);   // do not interrupt main process
        }
    }

    protected boolean isInteractive() {
        return super.isInteractive();
    }

    /** is protected for testing propose **/
    protected MultiRemoteCodenvy getMultiRemoteCodenvy() {
        return super.getMultiRemoteCodenvy();
    }

    /** is package private for testing propose **/
    void initConsole() {
        try {
            console = Console.create(isInteractive());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** is package private for testing propose **/
    void updateImCliClientIfNeeded() {
        final String updateFailMessage = "WARNING: automatic update of IM CLI client failed. See logs for details.\n";

        try {
            // get latest update of IM CLI client
            Artifact imArtifact = createArtifact(InstallManagerArtifact.NAME);
            List<UpdateArtifactInfo> updates = getFacade().getAllUpdatesAfterInstalledVersion(imArtifact);
            if (updates.isEmpty()) {
                return;
            }
            UpdateArtifactInfo update = updates.get(updates.size() - 1);
            Version versionToUpdate = Version.valueOf(update.getVersion());

            // download update of IM CLI client
            if (update.getStatus().equals(UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD)) {
                getFacade().startDownload(imArtifact, versionToUpdate);

                while (true) {
                    DownloadProgressResponse downloadProgressResponse = getFacade().getDownloadProgress();

                    if (downloadProgressResponse.getStatus().equals(DownloadArtifactInfo.Status.FAILED)) {
                        // log error and continue working
                        LOG.log(Level.SEVERE, format("Fail of automatic download of update of IM CLI client. Error: %s", downloadProgressResponse.getMessage()));
                        getConsole().printError(updateFailMessage, isInteractive());
                        return;
                    }

                    if (downloadProgressResponse.getStatus().equals(DownloadArtifactInfo.Status.DOWNLOADED)) {
                        break;
                    }
                }
            }

            // update IM CLI client
            InstallOptions installOptions = new InstallOptions();
            installOptions.setConfigProperties(Collections.emptyMap());
            installOptions.setInstallType(InstallType.SINGLE_SERVER);
            installOptions.setStep(0);

            String stepId = getFacade().update(imArtifact, versionToUpdate, installOptions);
            getFacade().waitForInstallStepCompleted(stepId);
            InstallArtifactStepInfo updateStepInfo = getFacade().getUpdateStepInfo(stepId);
            if (updateStepInfo.getStatus() == InstallArtifactInfo.Status.FAILURE) {
                // log error and continue working
                LOG.log(Level.SEVERE, format("Fail of automatic install of update of IM CLI client. Error: %s", updateStepInfo.getMessage()));
                getConsole().printError(updateFailMessage, isInteractive());
                return;
            }

            getConsole().println("The Codenvy CLI is out of date. We are doing an automatic update. Relaunch.\n");
            getConsole().exit(0);

        } catch (Exception e) {
            // log error and continue working
            LOG.log(Level.SEVERE, format("Fail of automatic update of IM CLI client. Error: %s", e.getMessage()));
            getConsole().printError(updateFailMessage, isInteractive());
        }
    }

    private void initDtoFactory() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            DtoFactory.getInstance();
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    private void initCodenvyOnpremPreferences() {
        final Preferences globalPreferences = (Preferences)session.get(Preferences.class.getName());
        codenvyOnpremPreferences = new CodenvyOnpremPreferences(globalPreferences,
                                                                getMultiRemoteCodenvy());
    }
}
