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
 * Declares a command.
 * 
 * @author Fox Mulder
 * @see CommandLine
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    
    /**
     * Command name. If name is not specified then name of annotated class or
     * interface will be used. Note that if class or interface name starts with
     * or ends with <code>Command</code> word then it will be ommitted (e.g.
     * command name for class named <code>HelpCommand</code> will be
     * <code>help</code>).
     */
    String name() default "";
    
    /**
     * Key of command description message in resource bundle. If description
     * is not specified then <code>command.COMMAND.description</code> will be
     * used; where <code>COMMAND</code> is this command name.
     */
    String description() default "";
    
}
