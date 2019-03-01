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
 * Declares a positional argument.
 * 
 * <p>This annotation should be applied to command properties (class fields
 * and getter/setter methods). Argument properties declared as <code>static</code>
 * are not allowed; properties should be writeable (annotated class fields
 * should not be declared as <code>final</code>, annotated getter methods
 * should have corresponding setter methods).</p>
 * 
 * @author Fox Mulder
 * @see CommandLine
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {
    
    /**
     * Command argument name. If name is not specified then name of annotated
     * field or getter/setter method will be used.
     */
    String name() default "";
    
    /**
     * Key of argument description message in resource bundle. If description
     * is not specified then <code>command.COMMAND.argument.ARGUMENT.description</code>
     * will be used; where <code>COMMAND</code> is command name and
     * <code>ARGUMENT</code> is this argument name.
     */
    String description() default "";
    
    /**
     * Argument sequence index. The first argument index is 1, the second is 2, ...
     * Command line argument indexes should be unique and should not have gaps.
     * If argument type is array or collection then argument should be last in
     * sequence of arguments.
     */
    int index() default 1;
    
    /**
     * Whether this argument is required. Please note that sequence of
     * required arguments should not have gaps. For example, if argument 3 is
     * required then arguments 1 and 2 should be required too. By default
     * argument is optional.
     */
    boolean required() default false;
    
}
