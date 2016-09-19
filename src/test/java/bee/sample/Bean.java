/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee.sample;

import bee.sample.annotation.SourceAnnotation;

/**
 * @version 2012/11/12 13:31:37
 */
public class Bean {

    private String name;

    /**
     * Getter
     */
    @SourceAnnotation
    public String getName() {
        return name;
    }

    /**
     * Setter
     */
    public void setName(String name) {
        this.name = name;
    }
}
