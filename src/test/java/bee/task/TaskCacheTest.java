/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee.task;

import org.junit.jupiter.api.Test;

import bee.Task;
import bee.api.Command;
import kiss.I;

class TaskCacheTest extends TaskTestBase {

    @Test
    void noValue() {
        NoValue task = I.make(NoValue.class);
        assert NoValue.count == 0;
        task.run();
        assert NoValue.count == 1;
        task.run();
        assert NoValue.count == 1;
    }

    /**
     * Task returns no value.
     */
    protected static class NoValue extends Task {

        private static int count;

        @Command("Test")
        public void run() {
            count++;
        }
    }

    @Test
    void valued() {
        Value task = I.make(Value.class);
        assert Value.count == 0;
        assert task.run().equals("1");
        assert Value.count == 1;
        assert task.run().equals("1");
        assert Value.count == 1;
    }

    /**
     * Task returns some value.
     */
    protected static class Value extends Task {

        private static int count;

        @Command("Test")
        public String run() {
            count++;
            return String.valueOf(count);
        }
    }

    @Test
    void primitiveInt() {
        Int task = I.make(Int.class);
        assert Int.count == 0;
        assert task.run() == 1;
        assert Int.count == 1;
        assert task.run() == 1;
        assert Int.count == 1;
    }

    /**
     * Task returns primitive value.
     */
    protected static class Int extends Task {

        private static int count;

        @Command("Test")
        public int run() {
            return ++count;
        }
    }

    @Test
    void primitiveLong() {
        Long task = I.make(Long.class);
        assert Long.count == 0;
        assert task.run() == 1;
        assert Long.count == 1;
        assert task.run() == 1;
        assert Long.count == 1;
    }

    /**
     * Task returns primitive value.
     */
    protected static class Long extends Task {

        private static int count;

        @Command("Test")
        public long run() {
            return ++count;
        }
    }

    @Test
    void primitiveFloat() {
        Float task = I.make(Float.class);
        assert Float.count == 0;
        assert task.run() == 1;
        assert Float.count == 1;
        assert task.run() == 1;
        assert Float.count == 1;
    }

    /**
     * Task returns primitive value.
     */
    protected static class Float extends Task {

        private static int count;

        @Command("Test")
        public float run() {
            return ++count;
        }
    }

    @Test
    void primitiveDouble() {
        Double task = I.make(Double.class);
        assert Double.count == 0;
        assert task.run() == 1;
        assert Double.count == 1;
        assert task.run() == 1;
        assert Double.count == 1;
    }

    /**
     * Task returns primitive value.
     */
    protected static class Double extends Task {

        private static int count;

        @Command("Test")
        public double run() {
            return ++count;
        }
    }

    @Test
    void primitiveBoolean() {
        Boolean task = I.make(Boolean.class);
        assert Boolean.count == 0;
        assert task.run();
        assert Boolean.count == 1;
        assert task.run();
        assert Boolean.count == 1;
    }

    /**
     * Task returns primitive value.
     */
    protected static class Boolean extends Task {

        private static int count;

        @Command("Test")
        public boolean run() {
            count++;
            return true;
        }
    }
}
