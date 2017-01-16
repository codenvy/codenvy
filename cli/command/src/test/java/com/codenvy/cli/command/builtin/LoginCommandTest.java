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

import com.codenvy.client.dummy.DummyCodenvyClient;

import org.apache.felix.service.command.CommandSession;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test of the login command
 * 
 * @author Florent Benoit
 */
public class LoginCommandTest extends AbsCommandTest {


    protected CommandInvoker getInvoker() {
        LoginCommand loginCommand = new LoginCommand();


        DummyCodenvyClient codenvyClient = new DummyCodenvyClient();

        // prepare
        CommandSession commandSession = prepare(loginCommand, codenvyClient);

        return new CommandInvoker(loginCommand, commandSession);
    }


    /**
     * Test password with & and ! characters
     */
    @Test
    public void testPasswordStrange() throws Exception {

        final String username = "florent";
        final String password = "&!fgA%MPnB&qAEuHj#";
        final String remoteName = "myRemote";

        // specify remote and username
        CommandInvoker commandInvoker = getInvoker().argument("username", username).option("--remote", remoteName);

        // set strange password !
        commandInvoker.setSystemIn(password + System.lineSeparator());

        // Mock remote
        CommandSession commandSession = commandInvoker.getCommandSession();
        Mockito.reset(commandSession);
        MultiRemoteCodenvy remoteCodenvy = mock(MultiRemoteCodenvy.class);
        doReturn(remoteCodenvy).when(commandSession).get(MultiRemoteCodenvy.class.getName());
        doReturn(true).when(remoteCodenvy).hasAvailableRemotes();

        // Check we have the same password in input
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Assert.assertEquals(args[0], remoteName);
                Assert.assertEquals(args[1], username);
                Assert.assertEquals(args[2], password);
                return null;
            }
        }).when(remoteCodenvy).login(anyString(), anyString(), anyString());

        // invoke command
        commandInvoker.invoke();

    }

}
