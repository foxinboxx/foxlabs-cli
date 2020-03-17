/* 
 * Copyright (C) 2018 FoxLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.foxlabs.cli;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Declares a command option.
 * 
 * <p>This annotation should be applied to command properties (class fields
 * and getter/setter methods). Option properties declared as <code>static</code>
 * are not allowed; properties should be writeable (annotated class fields
 * should not be declared as <code>final</code>, annotated getter methods
 * should have corresponding setter methods).</p>
 * 
 * @author Fox Mulder
 * @see CommandLineParser
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {
    
    /**
     * Array of possible command option names (aliases). If array of names is
     * empty then name of annotated field or getter/setter method will be used.
     * Do not use <code>-</code> and <code>--</code> prefixes in names declared
     * with this annotation.
     */
    String[] names() default {};
    
    /**
     * Key of option description message in resource bundle. If description
     * is not specified then <code>command.COMMAND.option.OPTION.description</code>
     * will be used; where <code>OPTION</code> is command name and
     * <code>OPTION</code> is this option name.
     */
    String description() default "";
    
    /**
     * Whether this option is required. By default options are optional.
     */
    boolean required() default false;
    
}
