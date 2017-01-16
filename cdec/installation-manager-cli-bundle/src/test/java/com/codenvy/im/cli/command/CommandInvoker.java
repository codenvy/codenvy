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

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.AnsiOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * This class allows to test and invoke a Karaf command.
 * @author Florent Benoit
 */
public class CommandInvoker {

    /**
     * Command used by this invoker.
     */
    private Action command;

    /**
     * List of options for this command
     */
    private final List<FieldInvoker<Option>> options;

    /**
     * List of arguments for this command
     */
    private final List<FieldInvoker<Argument>> arguments;

    /**
     * Input stream.
     */
    private InputStream inputStream;

    /**
     * Output stream.
     */
    private final ByteArrayOutputStream outputStream;

    /**
     * Error stream.
     */
    private final ByteArrayOutputStream errorStream;

    /**
     * CommandSession.
     */
    private final CommandSession commandSession;

    /**
     * Build an invoker of commands
     * @param command
     */
    public CommandInvoker(Action command, CommandSession commandSession) {
        this.command = command;
        this.options = new ArrayList<>();
        this.arguments = new ArrayList<>();
        this.outputStream = new ByteArrayOutputStream();
        this.errorStream = new ByteArrayOutputStream();
        this.commandSession = commandSession;
        introspect();
    }

    /**
     * Search all options and arguments of the given command
     */
    protected void introspect() {
        Class<?> clazz = command.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                Option option = field.getAnnotation(Option.class);
                if (option != null) {
                    options.add(new FieldOptionInvoker(command, field, option));
                }
                Argument argument = field.getAnnotation(Argument.class);
                if (argument != null) {
                    arguments.add(new FieldArgumentInvoker(command, field, argument));
                }
            }
            clazz = clazz.getSuperclass();
        }

    }

    /**
     * Allows to set an argument for the given action.
     * @param name the name of the argument
     * @param value the value of the argument
     * @return this {@link com.codenvy.cli.command.builtin.CommandInvoker}
     */
    public <T> CommandInvoker argument(String name, T value) {
        boolean injected = false;
        for (FieldInvoker<Argument> fieldInvoker : arguments) {
            if (fieldInvoker.getName().equals(name)) {
                fieldInvoker.inject(value);
                injected = true;
                break;
            }
        }
        if (!injected) {
            throw new IllegalStateException("Wanted to inject argument '" + name + "' but it was not found");
        }
        return this;
    }

    /**
     * Allows to set an option for the given action.
     * @param name the name of the option
     * @param value the value of the option
     * @return this {@link com.codenvy.cli.command.builtin.CommandInvoker}
     */
    public <T> CommandInvoker option(String name, T value) {
        boolean injected = false;
        for (FieldInvoker<Option> fieldInvoker : options) {
            if (fieldInvoker.getName().equals(name)) {
                fieldInvoker.inject(value);
                injected = true;
                break;
            }
        }
        if (!injected) {
            throw new IllegalStateException("Wanted to inject option '" + name + "' but it was not found");
        }
        return this;
    }

    public CommandSession getCommandSession() {
        return commandSession;
    }

    /**
     * Allows to give input lines as parameter
     * @param lines the given content
     * @return this {@link com.codenvy.cli.command.builtin.CommandInvoker}
     */
    public CommandInvoker setSystemIn(String lines) {
        this.inputStream = new ByteArrayInputStream(lines.getBytes());
        return this;
    }



    public Result invoke() throws Exception {
        if (commandSession == null) {
            throw new IllegalStateException("Unable to invoke as command session has not been given");
        }
        return invoke(commandSession);
    }

    public Result invoke(CommandSession commandSession) throws Exception {
        // Set our streams

        InputStream previousIn = System.in;
        PrintStream previousOut = System.out;
        PrintStream previousErr = System.out;

        System.setIn(this.inputStream);
        System.setOut(new PrintStream(this.outputStream));
        System.setErr(new PrintStream(this.errorStream));

        try {
            command.execute(commandSession);
        } finally {
            System.setIn(previousIn);
            System.setOut(previousOut);
            System.setErr(previousErr);
        }

        return new Result(outputStream, errorStream);
    }


    static class Result {

        private ByteArrayOutputStream outputStream;
        private ByteArrayOutputStream errorStream;

        private boolean ansiMode = true;

        public Result(ByteArrayOutputStream outputStream, ByteArrayOutputStream errorStream) {
            this.outputStream = outputStream;
            this.errorStream = errorStream;
        }

        public Result disableAnsi() {
            this.ansiMode = false;
            return this;
        }

        private String removeAnsi(final String content) {
            if (content == null) {
                return null;
            }
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                AnsiOutputStream aos = new AnsiOutputStream(baos);
                aos.write(content.getBytes());
                aos.flush();
                return baos.toString();
            } catch (IOException e) {
                return content;
            }
        }

        public String getOutputStream() {
            return getContent(outputStream);
        }

        public String getErrorStream() {
            return getContent(errorStream);
        }

        protected String getContent(ByteArrayOutputStream stream) {
            String content;
            try {
                content = stream.toString("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Unable to find encoding", e);
            }
            // Do we have to remove ANSI support ?
            if (!ansiMode) {
                return removeAnsi(content);
            }
            return content;
        }

    }



    /**
     * Helper class used to inject values in the action commands.
     * @param <T>
     */
    abstract class FieldInvoker<T> {

        private Action command;

        private T object;

        private Field field;

        public FieldInvoker(Action command, Field field, T object) {
            this.command = command;
            this.field = field;
            this.object = object;
        }

        public abstract String getName();

        public <T> void inject(T value) {
            this.field.setAccessible(true);
            try {
                this.field.set(command, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot inject the value", e);
            }
        }

        protected T getObject() {
            return object;
        }

    }

    class FieldOptionInvoker extends FieldInvoker<Option> {

        public FieldOptionInvoker(Action command, Field field, Option object) {
            super(command, field, object);
        }

        @Override
        public String getName() {
            return getObject().name();
        }
    }

    class FieldArgumentInvoker extends FieldInvoker<Argument> {

        public FieldArgumentInvoker(Action command, Field field, Argument object) {
            super(command, field, object);
        }

        @Override
        public String getName() {
            return getObject().name();
        }
    }
}
