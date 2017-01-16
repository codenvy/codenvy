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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Display a spinning wait action.
 * @author Florent Benoit
 */
public class WaitingAction<T> {

    /**
     * Wait until signal is to stop waiting
     */
    private volatile boolean wait = true;

    /**
     * Text to display when action is performing
     */
    private String textWait;

    /**
     * Text to display after action is performed.
     */
    private String textAfter;

    /**
     * Condition.
     */
    private WaitingActionCondition waitingActionCondition;

    /**
     * Request
     */
    private Request<T> request;

    /**
     * Build a new waiting action by providing text to display and after request execute
     * @param textWait the text to display
     * @param textAfter the text after
     */
    public WaitingAction(String textWait, String textAfter, Request<T> request, WaitingActionCondition waitingActionCondition) {
        this.textWait = textWait;
        this.textAfter = textAfter;
        this.request = request;
        this.waitingActionCondition = waitingActionCondition;

    }


    /**
     * Display a spinning wait action.
     * @throws Exception
     *         if unable to compute a result
     */
    private T request() {

        try {
            new ConsoleReader().resetPromptLine(textWait, "", 1);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write", e);
        }
        List<String> progress = Arrays.asList("|", "/", "-", "\\");

        int index = 0;

        T result = null;
        while (wait) {
            try {
                new ConsoleReader().resetPromptLine(textWait, progress.get(index), 1);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to write", e);
            }

            // execute the request
            result = request.execute();

            // check
            WaitingActionConditionStateImpl<T> waitingActionConditionState = new WaitingActionConditionStateImpl(result);
            waitingActionCondition.check(waitingActionConditionState);

            // if condition is ok no need to wait any longer
            wait = !waitingActionConditionState.isComplete();

            String textUpdate = waitingActionConditionState.newText();
            if (textUpdate != null) {
                textWait = textUpdate;
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

        return result;
    }


    /**
     * Execute the codenvy request {@link com.codenvy.client.Request} and then display before and after text
     * @return the codenvy
     */
    public  T execute() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            Future<T> newStatusTask = executorService.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return request();
                }
            });
            try {
                return newStatusTask.get(5, TimeUnit.MINUTES);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.out.println("Unable to get updated " + request);
                return null;
            }
        } finally {
            executorService.shutdown();
        }
    }



}

