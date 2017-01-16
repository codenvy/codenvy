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
package com.codenvy.im.commands;

import com.codenvy.im.agent.SecureShellAgent;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class TestRepeatCommand {

    @Mock
    private SecureShellAgent agent;
    @Mock
    private Command          command;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testThrowExceptionAndThenReturnResult() throws Exception {
        doThrow(new CommandException("Some error")).doReturn("result").when(command).execute();

        Command repeatCommand = new RepeatCommand(command, 2);
        String result = repeatCommand.execute();

        verify(command, times(2)).execute();
        assertEquals(result, "result");
    }

    @Test
    public void testThrowExceptionTwice() throws Exception {
        doThrow(new CommandException("error1"))
                .doThrow(new CommandException("error2"))
                .doThrow(new CommandException("error3"))
                .when(command).execute();

        Command repeatCommand = new RepeatCommand(command, 3);
        try {
            repeatCommand.execute();
        } catch (CommandException e) {
            assertEquals(e.getMessage(), "error3");

            e = (CommandException)e.getSuppressed()[0];
            assertEquals(e.getMessage(), "error2");

            e = (CommandException)e.getSuppressed()[0];
            assertEquals(e.getMessage(), "error1");
        }

        verify(command, times(3)).execute();
    }

    @Test
    public void testThenReturnResultImmediately() throws Exception {
        doReturn("result").when(command).execute();

        Command repeatCommand = new RepeatCommand(command, 2);
        String result = repeatCommand.execute();

        verify(command).execute();
        assertEquals(result, "result");
    }
}


