/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee;

@SuppressWarnings("serial")
public class TaskCancel extends RuntimeException {

    /**
     * @param message
     */
    public TaskCancel(String message) {
        super(message);
    }
}