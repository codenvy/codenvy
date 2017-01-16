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
package com.codenvy.cli.command.builtin.activator;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.CommandWithAction;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Activator of the bundle. when bundle is activated we will listen for commands and then set default shell to codenvy commands.
 * By doing this, this avoid to either have : Karaf with delay console flag or to use command in shell.init scripts that failed as command is not there.
 * @author Florent Benoit
 */
public class CodenvyActivator implements BundleActivator {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CodenvyActivator.class.getName());

    /**
     * Switch command.
     */
    private static final String SWITCH_COMMAND = "default-codenvy-namespace";

    /**
     * osgi command function.
     */
    private static final String OSGI_COMMAND_FUNCTION = "osgi.command.function";

    /**
     * Command session tracker.
     */
    private ServiceTracker<CommandSession, Object> commandSessionTracker;

    /**
     * Action tracker
     */
    private ServiceTracker<CommandWithAction, Object> serviceTrackerActions;

    /**
     * Command session.
     */
    private CommandSession commandSession;

    /**
     * Switch command.
     */
    private Action switchCommand;

    /**
     * Callback when a command session is added
     * @param commandSession the session
     */
    protected void setCommandSession(CommandSession commandSession) {
        this.commandSession = commandSession;

        callAction();
    }

    /**
     * Callback when a switch command is added
     * @param commandWithAction the action
     */
    protected void setSwitchCommand(CommandWithAction commandWithAction) {
        if (commandWithAction != null) {
            this.switchCommand = commandWithAction.createNewAction();
        } else {
            this.switchCommand = null;
        }

        callAction();
    }

    /**
     * Check if we can call the action (if we have both action and command session)
     */
    protected void callAction() {
        if (commandSession != null && switchCommand != null) {
            try {
                switchCommand.execute(commandSession);
            } catch (Exception e) {
                LOGGER.log(Level.FINEST, "Unable to execute the command", e);
            }
        }
    }


    /**
     * Called when this bundle is started so the Framework can perform the
     * bundle-specific activities necessary to start this bundle. This method
     * can be used to register services or to allocate any resources that this
     * bundle needs.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context
     *         The execution context of the bundle being started.
     * @throws Exception
     *         If this method throws an exception, this bundle is
     *         marked as stopped and the Framework will remove this bundle's
     *         listeners, unregister all services registered by this bundle, and
     *         release all services used by this bundle.
     */
    @Override
    public void start(final BundleContext context) throws Exception {

        // track command session and Switch command
        this.commandSessionTracker =
                new ServiceTracker(context, CommandSession.class, new CommandSessionListener(context));
        commandSessionTracker.open();

        // tracking actions
        this.serviceTrackerActions = new ServiceTracker(context, CommandWithAction.class, new SwitchActionListener(context));

        serviceTrackerActions.open();

    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle. In general, this
     * method should undo the work that the {@code BundleActivator.start} method
     * started. There should be no active threads that were started by this
     * bundle when this bundle returns. A stopped bundle must not call any
     * Framework objects.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context
     *         The execution context of the bundle being stopped.
     * @throws Exception
     *         If this method throws an exception, the bundle is still
     *         marked as stopped, and the Framework will remove the bundle's
     *         listeners, unregister all services registered by the bundle, and
     *         release all services used by the bundle.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.serviceTrackerActions != null) {
            this.serviceTrackerActions.close();
        }
        if (this.commandSessionTracker != null) {
            this.commandSessionTracker.close();
        }
    }

    /**
     * Customizer that will check for Command Session and then call the activator with the command session object.
     */
    private class CommandSessionListener implements ServiceTrackerCustomizer<CommandSession, Object> {
        private final BundleContext context;

        public CommandSessionListener(BundleContext context) {
            this.context = context;
        }

        @Override
        public Object addingService(ServiceReference<CommandSession> reference) {
            setCommandSession(context.getService(reference));
            return null;
        }

        @Override
        public void modifiedService(ServiceReference reference, Object service) {
            // unset it for now as we only need it at first call
            setCommandSession(null);
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            setCommandSession(null);
        }
    }

    /**
     * Customizer that will check for the expected Command Action and then call the activator with the switch command.
     */
    private class SwitchActionListener implements ServiceTrackerCustomizer<CommandWithAction, Object> {
        private final BundleContext context;

        public SwitchActionListener(BundleContext context) {
            this.context = context;
        }

        @Override
        public Object addingService(ServiceReference<CommandWithAction> reference) {

            if (SWITCH_COMMAND.equals(reference.getProperty(OSGI_COMMAND_FUNCTION))) {
                setSwitchCommand(context.getService(reference));
            }
            return null;
        }

        @Override
        public void modifiedService(ServiceReference reference, Object service) {
            // unset it for now as we only need it at first call
            setSwitchCommand(null);
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            setSwitchCommand(null);
        }
    }
}
