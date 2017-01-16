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
package com.codenvy.cli.command.builtin.helper;

import jline.console.ConsoleReader;

import com.codenvy.client.Request;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Display a spinning wait action.
 * @author Florent Benoit
 */
public class BeforeAfterAction  {

    /**
     * Wait until signal is to stop waiting
     */
    private volatile boolean wait = true;

    /**
     * Runner is stopped
     */
    private CountDownLatch runnerStopped = new CountDownLatch(1);

    /**
     * Text to display when action is performing
     */
    private String textWait;

    /**
     * Text to display after action is performed.
     */
    private String textAfter;

    /**
     * Build a new waiting action by providing text to display and after request execute
     * @param textWait the text to display
     * @param textAfter the text after
     */
    public BeforeAfterAction(String textWait, String textAfter) {
        this.textWait = textWait;
        this.textAfter = textAfter;
    }


    /**
     * Display a spinning wait action.
     * @throws Exception
     *         if unable to compute a result
     */
    private void waiting() {

        try {
            new ConsoleReader().resetPromptLine(textWait, "", 1);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write", e);
        }
        List<String> progress = Arrays.asList("|", "/", "-", "\\");

        int index = 0;

        while (wait) {
            try {
                new ConsoleReader().resetPromptLine(textWait, progress.get(index), 1);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to write", e);
            }

            // Wait 3 seconds
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Unable to wait", e);
            }

            index++;

            if (index >= progress.size()) {
                index = 0;
            }

        }
        try {
            new ConsoleReader().resetPromptLine(textAfter, "", 0);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write", e);
        }
        System.out.println();

        runnerStopped.countDown();
    }

    /**
     * Stop displaying output and then display the after text.
     * @return true if it was done sucessfully
     */
    private boolean stop() {
        // already stopped ?
        if (!wait) {
            return true;
        }
        wait = false;
        // wait display is finished
        try {
            runnerStopped.await();
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    /**
     * Execute the codenvy request {@link com.codenvy.client.Request} and then display before and after text
     * @return the codenvy
     */
    public <T> T execute(Request<T> request) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    waiting();
                }
            }).start();
            return request.execute();
        } finally {
            stop();
        }
    }



}

