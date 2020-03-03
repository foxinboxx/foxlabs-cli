/*
 * Copyright (C) 2017 FoxLabs
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

/**
 * <p>This command line framework is tightly integrated with
 * <a href="https://foxlabs.org/foxlabs-validation/">FoxLabs Validation Framework</a>
 * that provides powerful features used for conversion and validation of
 * command line options and arguments.</p>
 *
 * <p><b>Command Line Syntax</b></p>
 *
 * The command line allows two variants of syntax:
 * <ul>
 *   <li>{@code [command] [options] [arguments]}</li>
 *   <li>{@code [options] [command] [arguments]}</li>
 * </ul>
 *
 * In other words, command may precede options or follow right after them but
 * not in between. Arguments are always remaining part that follows right after
 * command and its options.
 *
 * <p><b>Commands</b></p>
 *
 * Command syntax follows the rules below:
 * <ul>
 *   <li>Command name consists of alphanumeric characters, dashes ({@code -}),
 *       dots ({@code .}) and underscores ({@code _}).</li>
 *   <li>Command name should begin with a letter ({@code [a-zA-Z]}) character.</li>
 *   <li>Command names are case insensitive.</li>
 * </ul>
 *
 * <p><b>Options</b></p>
 *
 * Options syntax follows POSIX and GNU conventions with minor changes:
 * <ul>
 *   <li>Arguments are options if they begin with a hyphen delimiter
 *       ({@code -}).</li>
 *   <li>Multiple options may follow a hyphen delimiter in a single token if
 *       the options do not take arguments. Thus, {@code -abc} is
 *       equivalent to {@code -a -b -c}.</li>
 *   <li>Option names are single alphanumeric characters.</li>
 *   <li>Certain options require an argument.</li>
 *   <li>An option and its argument may or may not appear as separate tokens.
 *       In other words, the whitespace separating them is optional.
 *       Thus, {@code -o foo} and {@code -ofoo} are equivalent.</li>
 *   <li>Options precede other non-option arguments.</li>
 *   <li>Options may be supplied in any order, or appear multiple times.</li>
 *   <li>Long options consist of {@code --} followed by a name made of
 *       alphanumeric characters, dashes ({@code -}), dots ({@code .})
 *       and underscores ({@code _}).</li>
 *   <li>To specify an argument for a long option, the {@code --name=value}
 *       or {@code --name:value} or {@code --name value} syntax should
 *       be used.</li>
 *   <li>Boolean options do not require arguments explicitly, but if you want
 *       to provide explicit value then {@code -f false} and
 *       {@code --flag false} forms should be avoided because of ambiguity
 *       with arguments.</li>
 *   <li>Option names are case sensitive.</li>
 * </ul>
 *
 * <p><b>Arguments</b></p>
 *
 * Arguments are of free form syntax except the following rules:
 * <ul>
 *   <li>Arguments should be separated by one or more whitespaces from each
 *       other.</li>
 *   <li>Arguments may include whitespaces if they are enclosed in double
 *       quotation mark ({@code "}) characters. Thus, {@code "foo bar"}
 *       will be considered as a single {@code foo bar} argument and not
 *       as two separate arguments {@code "foo} and {@code bar"}.</li>
 * </ul>
 *
 * @author Fox Mulder
 */
public final class CommandLineParser {

  public void parse(String... args) {

  }

}
