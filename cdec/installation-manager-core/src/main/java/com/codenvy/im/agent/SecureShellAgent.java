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
package com.codenvy.im.agent;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

import static com.codenvy.im.agent.AgentException.create;
import static java.lang.String.format;

/**
 * @author Alexander Reshetnyak
 * @author Dmytro Nochevnov
 */
public class SecureShellAgent extends AbstractAgent {

    private Session session;
    private final JSch jsch = getJSch();

    /** Create ssh session object and try to connect to remote host by using auth key. */
    public SecureShellAgent(final String host,
                            final int port,
                            final String user,
                            final Path privateKeyFileAbsolutePath) throws AgentException {
        try {
            session = getSession(host, port, user);
            jsch.addIdentity(privateKeyFileAbsolutePath.toString());
//            JSch.setLogger(new JschLogger());  // turn on if you need to get detailed log for all ssh operation in output. Be careful: it is verbose and could hang up IM.
        } catch (Exception e) {
            String errorMessage = format("Can't connect to host '%s@%s:%s' by using private key '%s'.", user, host, port, privateKeyFileAbsolutePath);
            throw create(errorMessage, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String execute(String command) throws AgentException, ConnectionException {
        try {
            // we need to recreate session each execute time to avoid error "com.jcraft.jsch.JSchException: socket is not established" in PuppetErrorInterrupter
            session = getSession(session.getHost(), session.getPort(), session.getUserName());
            session.connect();
        } catch (Exception e) {
            session.disconnect();
            String errorMessage = format("Can't connect to host '%s@%s:%s'.",
                                         session.getUserName(),
                                         session.getHost(),
                                         session.getPort());
            throw ConnectionException.create(errorMessage, e);
        }

        ChannelExec channel = null;
        try {
            channel = (ChannelExec)session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setPty(true);         // to avoid error "sudo: sorry, you must have a tty to run sudo" in time of execution sudo command
            channel.getOutputStream().flush();

            InputStream in = channel.getInputStream();
            InputStream error = channel.getErrStream();

            channel.connect(0);

            waitForChannelClosed(channel);

            return processOutput(command, channel.getExitStatus(), in, error);
        } catch (Exception e) {
            String errorMessage = format("Can't execute command '%s' on node '%s'.", command, session.getHost());
            throw create(errorMessage, e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }

            session.disconnect();
        }
    }

    /** for unit testing propose */
    protected JSch getJSch() {
        return new JSch();
    }

    private Session getSession(String host, int port, String user) throws JSchException {
        Session session = jsch.getSession(user, host, port);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");   // to fix algorithm negotiation fail
        session.setConfig(config);
        session.setServerAliveInterval(50 * 1000);  // 50 sec
        session.setServerAliveCountMax(150);

        return session;
    }

    private void waitForChannelClosed(Channel channel) {
        while (!channel.isClosed()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore to allow being successful interrupted by PuppetErrorInterrupter
                return;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        try {
            return format("{'host'='%s', 'port'='%s', 'user'='%s', 'identity'='%s'}",
                          session.getHost(),
                          session.getPort(),
                          session.getUserName(),
                          jsch.getIdentityNames().toString());
        } catch (JSchException e) {
            return format("{'host'='%s'}", session.getHost());
        }
    }

    public static class JschLogger implements com.jcraft.jsch.Logger {
        static java.util.Hashtable name = new java.util.Hashtable();

        static {
            name.put(new Integer(DEBUG), "DEBUG: ");
            name.put(new Integer(INFO), "INFO: ");
            name.put(new Integer(WARN), "WARN: ");
            name.put(new Integer(ERROR), "ERROR: ");
            name.put(new Integer(FATAL), "FATAL: ");
        }

        public boolean isEnabled(int level) {
            return true;
        }

        public void log(int level, String message) {
            System.err.print(name.get(new Integer(level)));
            System.err.println(message);
        }
    }

}
