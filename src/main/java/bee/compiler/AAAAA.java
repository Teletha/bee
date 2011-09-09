/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bee.compiler;

import javax.annotation.Resource;

import bee.UserNotifier;

/**
 * @version 2011/03/24 18:53:17
 */
public class AAAAA implements AnnotationValidator<Resource> {

    /**
     * @see bee.compiler.AnnotationValidator#validate(java.lang.annotation.Annotation,
     *      AST, bee.UserNotifier)
     */
    @Override
    public void validate(Resource annotation, AST ast, UserNotifier notifier) {
    }

}
