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

import com.codenvy.im.response.NodeInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov */
public class TestRemoveNodeCommand extends AbstractTestCommand {
    private AbstractIMCommand spyCommand;

    private final static String TEST_DNS = "builder.node.com";

    @BeforeMethod
    public void initMocks() throws IOException {
        spyCommand = spy(new RemoveNodeCommand());
        performBaseMocks(spyCommand, true);
    }

    @Test
    public void testRemoveNodeCommand() throws Exception {
        doReturn(new NodeInfo()).when(mockFacade).removeNode(TEST_DNS);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("dns", TEST_DNS);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "{\n" +
                             "  \"node\" : { },\n" +
                             "  \"status\" : \"OK\"\n" +
                             "}\n");
    }

    @Test
    public void testRemoveNodeCommandWhenDnsIsEmpty() throws Exception {
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("dns", "");

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "");

        verify(mockFacade, never()).removeNode(anyString());
    }
}
