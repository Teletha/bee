/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee;

import static bee.Platform.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import kiss.Codec;
import kiss.I;
import kiss.model.Model;
import bee.api.Command;

/**
 * <p>
 * Interactive user interface.
 * </p>
 * 
 * @version 2014/07/28 13:55:26
 */
public abstract class UserInterface {

    /**
     * <p>
     * Talk to user with decoration like title.
     * </p>
     * 
     * @param title
     */
    public void title(CharSequence title) {
        talk("------------------------------------------------------------");
        talk(title);
        talk("------------------------------------------------------------");
    }

    /**
     * <p>
     * Talk to user.
     * </p>
     * 
     * @param messages Your message.
     */
    public void talk(Object... messages) {
        Object last = messages[messages.length - 1];

        if (last instanceof String && ((String) last).endsWith("\r")) {
            write(build(messages));
        } else {
            write(build(messages, EOL));
        }
    }

    /**
     * <p>
     * Warn to user.
     * </p>
     * 
     * @param messages Your warning message.
     */
    public void warn(Object... messages) {
        talk("[WARN] ", messages);
    }

    /**
     * <p>
     * Declare a state of emergency.
     * </p>
     * 
     * @param message Your emergency message.
     */
    public void error(Object... messages) {
        talk("[ERROR] ", messages);
    }

    /**
     * <p>
     * Ask user about your question and return his/her answer.
     * </p>
     * 
     * @param question Your question message.
     * @return An answer.
     */
    public boolean confirm(String question) {
        String answer = ask(Platform.EOL + question + " (y/n)").toLowerCase();

        if (answer.equals("y") || answer.equals("ye") || answer.equals("yes")) {
            return true;
        } else if (answer.equals("n") || answer.equals("no")) {
            return false;
        } else {
            talk("Type 'y' or 'n'.");

            return confirm(question);
        }
    }

    /**
     * <p>
     * Ask user about your question and return his/her answer.
     * </p>
     * 
     * @param question Your question message.
     * @return An answer.
     */
    public String ask(String question) {
        return ask(question, (String) null);
    }

    /**
     * <p>
     * Ask user about your question and return his/her answer.
     * </p>
     * <p>
     * UserInterface can display a default answer and user can use it with simple action. If the
     * returned answer is incompatible with the default anwser type, default answer will be
     * returned.
     * </p>
     * 
     * @param <T> Anwser type.
     * @param question Your question message.
     * @param defaultAnswer A default anwser.
     * @return An answer.
     */
    public <T> T ask(String question, T defaultAnswer) {
        StringBuilder builder = new StringBuilder();
        builder.append(question);
        if (defaultAnswer != null) builder.append(build(" [", defaultAnswer, "]"));
        builder.append(" : ");

        // Question
        write(builder.toString());

        try {
            // Answer
            String answer = new BufferedReader(new InputStreamReader(System.in, Encoding)).readLine();

            // Remove whitespaces.
            answer = answer == null ? "" : answer.trim();

            // Validate user input.
            if (defaultAnswer == null) {
                if (answer.length() == 0) {
                    talk("Your input is empty, plese retry.");

                    // Retry!
                    return ask(question, (T) null);
                }

                // API definition
                return (T) answer;
            } else {
                Codec<T> codec = I.find(Codec.class, defaultAnswer.getClass());

                if (codec == null) {
                    codec = Model.load(defaultAnswer.getClass()).getCodec();
                }
                return answer.length() == 0 ? defaultAnswer : codec.decode(answer);
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Ask user about your question and return his/her selected item.
     * </p>
     * <p>
     * UserInterface can display a list of items and user can select it with simple action.
     * </p>
     * 
     * @param question Your question message.
     * @param items A list of selectable items.
     * @return A selected item.
     */
    public <T> T ask(String question, List<T> items) {
        if (items == null) {
            throw new Error(build("Question needs some items. [" + question, "]"));
        }

        switch (items.size()) {
        case 0:
            return null;

        case 1:
            return items.get(0); // unconditionally

        default:
            talk(question);
            talk(items);

            return items.get(select(1, items.size()) - 1);
        }
    }

    /**
     * <p>
     * Ask user about your question and return his/her selected item.
     * </p>
     * <p>
     * UserInterface can display a list of items and user can select it with simple action.
     * </p>
     * 
     * @param question Your question message.
     * @param items A list of selectable items.
     * @return A selected item.
     */
    public <E extends Enum> E ask(String question, Class<E> enumeration) {
        if (enumeration == null) {
            throw new Error(build("Question needs some items. [", question, "]"));
        }
        return ask(question, Arrays.asList(enumeration.getEnumConstants()));
    }

    /**
     * <p>
     * Select number.
     * </p>
     * 
     * @param min A minimum number.
     * @param max A maximum number.
     * @return A user input.
     */
    private int select(int min, int max) {
        try {
            int index = Integer.parseInt(ask("Input number to select one"));

            if (max < index) {
                warn("Max number is " + max + ", please retry");

                return select(min, max);
            }

            if (index < min) {
                warn("Min number is " + min + ", please retry.");

                return select(min, max);
            }
            return index;
        } catch (NumberFormatException e) {
            warn("Invalid number format, please retry.");

            return select(min, max);
        }
    }

    /**
     * <p>
     * Helper method to build message.
     * </p>
     * 
     * @param messages Your messages.
     * @return A combined message.
     */
    protected static String build(Object... messages) {
        StringBuilder builder = new StringBuilder();
        build(builder, messages);
        return builder.toString();
    }

    /**
     * <p>
     * Helper method to build message.
     * </p>
     * 
     * @param builder A message builder.
     * @param messages Your messages.
     */
    private static void build(StringBuilder builder, Object... messages) {
        for (Object message : messages) {
            if (message == null) {
                builder.append("null");
            } else {
                Class type = message.getClass();

                if (type.isArray()) {
                    buildArray(builder, type.getComponentType(), message);
                } else if (CharSequence.class.isAssignableFrom(type)) {
                    builder.append((CharSequence) message);
                } else if (Throwable.class.isAssignableFrom(type)) {
                    buildError(builder, (Throwable) message);
                } else if (List.class.isAssignableFrom(type)) {
                    buildList(builder, (List) message);
                } else {
                    builder.append(I.transform(message, String.class));
                }
            }
        }
    }

    /**
     * <p>
     * Helper method to build message from various array type.
     * </p>
     * 
     * @param builder A message builder.
     * @param type A array type.
     * @param array A message array.
     */
    private static void buildArray(StringBuilder builder, Class type, Object array) {
        if (type == int.class) {
            builder.append(Arrays.toString((int[]) array));
        } else if (type == long.class) {
            builder.append(Arrays.toString((long[]) array));
        } else if (type == float.class) {
            builder.append(Arrays.toString((float[]) array));
        } else if (type == double.class) {
            builder.append(Arrays.toString((double[]) array));
        } else if (type == boolean.class) {
            builder.append(Arrays.toString((boolean[]) array));
        } else if (type == char.class) {
            builder.append(Arrays.toString((char[]) array));
        } else if (type == byte.class) {
            builder.append(Arrays.toString((byte[]) array));
        } else if (type == short.class) {
            builder.append(Arrays.toString((short[]) array));
        } else {
            build(builder, (Object[]) array);
        }
    }

    /**
     * <p>
     * Build error message.
     * </p>
     * 
     * @param builder A message builder.
     * @param throwable An error message.
     */
    private static void buildError(StringBuilder builder, Throwable throwable) {
        StringWriter writer = new StringWriter();

        throwable.printStackTrace(new PrintWriter(writer));

        builder.append(writer.toString());
    }

    /**
     * <p>
     * Build listup message.
     * </p>
     * 
     * @param builder A message builder.
     * @param list Items.
     */
    private static void buildList(StringBuilder builder, List list) {
        if (builder.length() != 0) {
            builder.append(EOL);
        }
        builder.append(EOL);

        for (int i = 0; i < list.size(); i++) {
            builder.append("  [").append(i + 1).append("] ").append(list.get(i)).append(EOL);
        }
    }

    /**
     * <p>
     * Write message to user.
     * </p>
     * 
     * @param message
     */
    protected abstract void write(String message);

    /**
     * <p>
     * Get underlaying message listener.
     * </p>
     * 
     * @return
     */
    public abstract Appendable getInterface();

    /**
     * <p>
     * Display message about command starts.
     * </p>
     * 
     * @param name A command name.
     * @param command A coommand info.
     */
    public abstract void startCommand(String name, Command command);

    /**
     * <p>
     * Display message about command ends.
     * </p>
     * 
     * @param name A command name.
     * @param command A coommand info.
     */
    public abstract void endCommand(String name, Command command);
}
