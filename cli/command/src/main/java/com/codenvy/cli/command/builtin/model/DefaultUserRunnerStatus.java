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
package com.codenvy.cli.command.builtin.model;

import com.codenvy.cli.command.builtin.util.ascii.AsciiForm;
import com.codenvy.cli.command.builtin.util.ascii.DefaultAsciiForm;
import com.codenvy.client.model.RunnerState;
import com.codenvy.client.model.RunnerStatus;
import com.codenvy.client.model.builder.BuilderMetric;
import com.codenvy.client.model.runner.RunnerMetric;

import org.fusesource.jansi.Ansi;

import java.util.Date;
import java.util.List;

import static com.codenvy.cli.command.builtin.util.SHA1.sha1;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;

/**
 * Runner status implementation of {@link com.codenvy.cli.command.builtin.model.UserRunnerStatus}
 * @author Florent Benoit
 */
public class DefaultUserRunnerStatus implements UserRunnerStatus {

    /**
     * Runner status model
     */
    private RunnerStatus runnerStatus;

    /**
     * Project on which this runner process is linked
     */
    private UserProjectReference userProjectReference;

    /**
     * SHA-1.
     */
    private String sha1Id;

    /**
     * Default constructor
     * @param runnerStatus the status returned from Rest API
     * @param userProjectReference the user project
     */
    public DefaultUserRunnerStatus(RunnerStatus runnerStatus, UserProjectReference userProjectReference) {
        this.runnerStatus = runnerStatus;
        this.userProjectReference = userProjectReference;

        // r is for runner
        this.sha1Id = sha1("r", String.valueOf(runnerStatus.processId()) + userProjectReference.shortId());
    }

    /**
     * @return full sha1 ID
     */
    public String sha1ID() {
        return sha1Id;
    }

    /**
     * @return only the first 7 digits
     */
    public String shortId() {
        return sha1Id.substring(0, 7);
    }


    /**
     * @return the linked project
     */
    @Override
    public UserProjectReference getProject() {
        return userProjectReference;
    }

    /**
     * @return inner status
     */
    public RunnerStatus getInnerStatus() {
        return runnerStatus;
    }


    protected String bold(String name) {
        return Ansi.ansi().a(INTENSITY_BOLD).a(name).a(INTENSITY_BOLD_OFF).toString();
    }

    public String toString() {

        RunnerState state = getInnerStatus().status();

        String startTime;
        long start = getInnerStatus().startTime();
        if (start > 0) {
            startTime = new Date(start).toString();
        } else {
            startTime = "N/A";
        }

        String stopTime;
        long stop = getInnerStatus().stopTime();
        if (stop > 0) {
            stopTime = new Date(start).toString();
        } else {
            stopTime = "N/A";
        }


        String access;
        if (getInnerStatus().getWebLink() != null) {
            access = getInnerStatus().getWebLink().href();
        } else {
            access = "N/A";
        }

        // run stats and build stats
        List<BuilderMetric> builderMetricList = getInnerStatus().getBuildStats();
        List<RunnerMetric> runnerMetricList = getInnerStatus().getRunStats();

        AsciiForm form = new DefaultAsciiForm().withEntry(bold("id"), shortId())
                              .withEntry(bold("workspace"), getProject().getWorkspace().name())
                              .withEntry(bold("project"), getProject().name())
                              .withEntry(bold("run url"), access)
                              .withEntry(bold("status"), state.toString())
                              .withEntry(bold("start time"), startTime)
                              .withEntry(bold("stop time"), stopTime)
                              .withUppercasePropertyName();

        // add stats if present
        if (builderMetricList != null) {
            for (BuilderMetric builderMetric : builderMetricList) {
                String name = builderMetric.getName();
                String value = builderMetric.getValue();
                form.withEntry(bold("BUILD ").concat("(").concat(name).concat(")"), value);
            }
        }
        if (runnerMetricList != null) {
            for (RunnerMetric runnerMetric : runnerMetricList) {
                String name = runnerMetric.getName();
                String value = runnerMetric.getValue();
                form.withEntry(bold("RUN ").concat("(").concat(name).concat(")"), value);
            }
        }




        return form.toAscii();

    }
}
