/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bee.util;

import static bee.Platform.EOL;

import java.io.Serializable;
import java.util.function.Function;

import kiss.I;
import kiss.Signal;
import kiss.WiseRunnable;

/**
 * Simple Markdown-like memo.
 */
public class Notation implements Serializable {

    private static final long serialVersionUID = 6425166532989248520L;

    /** Indent character. */
    static final String INDENT = "\t";

    /** Line pattern. */
    private static final String LINE = "(\r\n|[\n\r\u2028\u2029\u0085])";

    /** Actual buffer. */
    private final StringBuilder builder = new StringBuilder();

    /** The current section level. */
    private int level = 0;

    /**
     * Write title.
     * 
     * @param title
     */
    public void title(String title) {
        int size = Math.round(title.length() * 1.2f);

        switch (level) {
        case 0:
            builder.append("=".repeat(size)).append(EOL);
            builder.append(title).append(EOL);
            builder.append("=".repeat(size)).append(EOL);
            break;

        case 1:
            builder.append("-".repeat(size)).append(EOL);
            builder.append(title).append(EOL);
            builder.append("-".repeat(size)).append(EOL);
            break;

        default:
            builder.append(INDENT.repeat(level)).append("#".repeat(level + 1)).append(" ").append(title).append(EOL);
            break;
        }
    }

    /**
     * Declare section nest.
     * 
     * @param section
     */
    public void section(WiseRunnable section) {
        level++;
        section.run();
        level--;
    }

    /**
     * Write paragraph.
     * 
     * @param paragraph
     */
    public void p(String paragraph) {
        builder.append(INDENT.repeat(level)).append(paragraph.replaceAll(LINE, "$1" + INDENT.repeat(level))).append(EOL);
    }

    /**
     * Write list.
     * 
     * @param items
     * @param descriptor
     */
    public <T> void list(T[] items, Function<T, String> descriptor) {
        list(I.signal(items), descriptor);
    }

    /**
     * Write list.
     * 
     * @param items
     */
    public <T> void list(Iterable<String> items) {
        list(I.signal(items), Function.identity());
    }

    /**
     * Write list.
     * 
     * @param items
     * @param descriptor
     */
    public <T> void list(Iterable<T> items, Function<T, String> descriptor) {
        list(I.signal(items), descriptor);
    }

    /**
     * Write list.
     * 
     * @param items
     * @param descriptor
     */
    public <T> void list(Signal<T> items, Function<T, String> descriptor) {
        items.to(item -> {
            builder.append(INDENT.repeat(level)).append("- ").append(descriptor.apply(item)).append(EOL);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return builder.toString();
    }
}