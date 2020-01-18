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
 *   <li><code>[command] [options] [arguments]</code></li>
 *   <li><code>[options] [command] [arguments]</code></li>
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
 *   <li>Command name should not begin with a hyphen (<code>-</code>) character
 *       to avoid ambiguity with options.</li>
 *   <li>Command name consists of alphanumeric characters, dashes
 *       (<code>-</code>), dots (<code>.</code>) and underscores
 *       (<code>_</code>).</li>
 *   <li>Command names are case insensitive.</li>
 *   <li>Subcommands are not supported.</li>
 * </ul>
 * 
 * <p><b>Options</b></p>
 * 
 * Options syntax follows POSIX and GNU conventions with minor changes:
 * <ul>
 *   <li>Arguments are options if they begin with a hyphen delimiter
 *       (<code>-</code>).</li>
 *   <li>Multiple options may follow a hyphen delimiter in a single token if
 *       the options do not take arguments. Thus, <code>-abc</code> is
 *       equivalent to <code>-a -b -c</code>.</li>
 *   <li>Option names are single alphanumeric characters.</li>
 *   <li>Certain options require an argument.</li>
 *   <li>An option and its argument may or may not appear as separate tokens.
 *       In other words, the whitespace separating them is optional.
 *       Thus, <code>-o foo</code> and <code>-ofoo</code> are equivalent.</li>
 *   <li>Options precede other non-option arguments.</li>
 *   <li>Options may be supplied in any order, or appear multiple times.</li>
 *   <li>Long options consist of <code>--</code> followed by a name made of
 *       alphanumeric characters, dashes (<code>-</code>), dots (<code>.</code>)
 *       and underscores (<code>_</code>).</li>
 *   <li>To specify an argument for a long option, the <code>--name=value</code>
 *       or <code>--name:value</code> or <code>--name value</code> syntax should
 *       be used.</li>
 *   <li>Boolean options do not require arguments explicitly, but if you want
 *       to provide explicit value then <code>-f false</code> and
 *       <code>--flag false</code> forms should be avoided because of ambiguity
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
 *       quotation mark (<code>"</code>) characters. Thus, <code>"foo bar"</code>
 *       will be considered as a single <code>foo bar</code> argument and not
 *       as two separate arguments <code>"foo</code> and <code>bar"</code>.</li>
 * </ul>
 * 
 * @author Fox Mulder
 * @see Command
 * @see Option
 * @see Argument
 */
public class CommandLine {
    
    /**
     * Command line program name.
     */
    private final String program;
    
    /**
     * Constructs a new command line with the specified program name.
     * 
     * @param program Command line program name.
     */
    public CommandLine(String program) {
        this.program = program.trim();
    }
    
    public CommandLine(String program, Runnable... commands) {
        this(program);
        for (Runnable command : commands) {
            addCommand(command);
        }
    }
    
    /**
     * Returns command line program name.
     * 
     * @return Command line program name.
     */
    public String getProgram() {
        return program;
    }
    
    /**
     * Adds a new command to this command line.
     * 
     * @param command A command to add.
     * @throws CommandLineException if command declaration is invalid.
     */
    public CommandLine addCommand(Runnable command) {
        // TODO
        return this;
    }
    
    /**
     * Runs command provided in the specified command line arguments.
     * Note that before this method call you need to add all possible commands
     * using the {@link #addCommand(Runnable)} method.
     * 
     * @param args Command line arguments received from <code>main</code> method.
     */
    public void run(String... args) {
        // TODO
    }
    
    /**
     * Runs the specified command using the specified command line arguments.
     * Call this method directly if your program does not support commands at
     * all and may take only options and/or arguments.
     * 
     * @param command Command to run.
     * @param args Command line arguments received from <code>main</code> method.
     * @throws CommandLineException if command declaration is invalid.
     */
    public void run(Runnable command, String... args) {
        // TODO
    }
    
    public void printUsage() {
        
    }
    
    public void printHelp(String commandName) {
        if (commandName == null) {
            printUsage();
        }
    }
    
}
