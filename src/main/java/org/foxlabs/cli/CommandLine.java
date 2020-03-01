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

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import org.foxlabs.validation.converter.Converter;
import org.foxlabs.validation.converter.ConverterFactory;
import org.foxlabs.validation.constraint.Constraint;

import org.foxlabs.util.Buildable;
import org.foxlabs.util.Strings;
import org.foxlabs.util.ToString;

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
 * @see Command
 * @see Option
 * @see Argument
 */
public final class CommandLine {

  private CommandLine(Builder builder) {
    
  }

  public void run(String... args) {

  }

  // Builder

  public static final class Builder implements Buildable<CommandLine> {

    protected Program program;

    public Program.Builder program(String name) {
      return program(name, null);
    }

    public Program.Builder program(String name, Runnable handler) {
      return new Program.Builder(name, handler) {
        @Override public CommandLine.Builder build() {
          program = new Program(this);
          return CommandLine.Builder.this;
        }
      };
    }

    @Override
    public CommandLine build() {
      return new CommandLine(this);
    }

  }

  // Program

  static class Program extends ToString.Adapter {

    protected final String name;

    protected final Runnable handler;

    protected final Map<String, Option> options;

    protected final Map<String, Argument> arguments;

    protected final Map<String, Command> commands;

    protected Program(String name, Runnable handler) {
      this.name = name;
      this.handler = handler;
      this.options = new LinkedHashMap<>();
      this.arguments = new LinkedHashMap<>();
      this.commands = new LinkedHashMap<>();
    }

    protected Program(Program.Builder builder) {
      this.name = builder.name;
      this.handler = builder.handler;
      this.options = new LinkedHashMap<>(builder.options);
      this.arguments = new LinkedHashMap<>(builder.arguments);
      this.commands = new LinkedHashMap<>(builder.commands);
    }

    // Program.Builder

    public static abstract class Builder extends Program implements Buildable<CommandLine.Builder> {

      protected Builder(String name, Runnable handler) {
        super(name.trim(), handler);
      }

      public Option.Builder<Program.Builder> option(String name) {
        return new Option.Builder<Program.Builder>(name) {
          @Override public Program.Builder build() {
            options.put(name, new Option(this));
            return Program.Builder.this;
          }
        };
      }

      public Argument.Builder<Program.Builder> argument(String name) {
        return new Argument.Builder<Program.Builder>(name) {
          @Override public Program.Builder build() {
            arguments.put(name.toUpperCase(), new Argument(this));
            return Program.Builder.this;
          }
        };
      }

      public Command.Builder<Program.Builder> command(String name) {
        return command(name, null);
      }

      public Command.Builder<Program.Builder> command(String name, Runnable handler) {
        return new Command.Builder<Program.Builder>(name, handler) {
          @Override public Program.Builder build() {
            commands.put(name.toLowerCase(), new Command(this));
            return Program.Builder.this;
          }
        };
      }

    }

    @Override
    public StringBuilder toString(StringBuilder buf) {
      buf.append(name).append("\n");
      return buf;
    }

  }

  // Command

  static class Command extends ToString.Adapter {

    /**
     * Name of this command.
     */
    protected final String name;

    /**
     * Handler of this command.
     */
    protected final Runnable handler;

    /**
     * Options of this command (keys are option names as is).
     */
    protected final Map<String, Option> options;

    /**
     * Arguments of this command (keys are argument names in upper case).
     */
    protected final Map<String, Argument> arguments;

    /**
     * Subcommands of this command (keys are subcommand names in lower case).
     */
    protected final Map<String, Command> subcommands;

    protected Command(String name, Runnable handler) {
      this.name = name;
      this.handler = handler;
      this.options = new LinkedHashMap<>();
      this.arguments = new LinkedHashMap<>();
      this.subcommands = new LinkedHashMap<>();
    }

    protected Command(Builder<?> builder) {
      this.name = builder.name;
      this.handler = builder.handler;
      this.options = new LinkedHashMap<>(builder.options);
      this.arguments = new LinkedHashMap<>(builder.arguments);
      this.subcommands = new LinkedHashMap<>(builder.subcommands);
    }

    @Override
    public StringBuilder toString(StringBuilder buf) {
      buf.append(name).append("\n");
      return buf;
    }

    // Command.Builder

    public static abstract class Builder<T> extends Command implements Buildable<T> {

      protected Builder(String name, Runnable handler) {
        super(name.trim(), handler);
        if (!NAME_REGEXP.matcher(this.name).matches()) {
          throw new IllegalArgumentException("Command name \"" +
              Strings.escape(name) + "\" is invalid");
        }
      }

      public Option.Builder<Command.Builder<T>> option(String name) {
        return new Option.Builder<Command.Builder<T>>(name) {
          @Override public Command.Builder<T> build() {
            options.put(name, new Option(this));
            return Command.Builder.this;
          }
        };
      }

      public Argument.Builder<Command.Builder<T>> argument(String name) {
        return new Argument.Builder<Command.Builder<T>>(name) {
          @Override public Command.Builder<T> build() {
            arguments.put(name.toUpperCase(), new Argument(this));
            return Command.Builder.this;
          }
        };
      }

      public Command.Builder<Command.Builder<T>> subcommand(String name, Runnable handler) {
        return new Command.Builder<Command.Builder<T>>(name, handler) {
          @Override public Command.Builder<T> build() {
            subcommands.put(name.toLowerCase(), new Command(this));
            return Command.Builder.this;
          }
        };
      }

    }

  }

  // Option

  static class Option extends ToString.Adapter {

    protected final String name;

    protected boolean required;

    protected Converter<?> converter;

    protected Constraint<?> constraint;

    protected Option(String name) {
      this.name = name;
      this.required = false;
    }

    protected Option(Builder<?> builder) {
      this.name = builder.name;
      this.required = builder.required;
      this.converter = builder.converter;
      this.constraint = builder.constraint;
    }

    @Override
    public StringBuilder toString(StringBuilder buf) {
      buf.append(name.length() > 1 ? "-" : "--").append(name).append("\n");
      return buf;
    }

    // Option.Builder

    public static abstract class Builder<T> extends Option implements Buildable<T> {

      protected Builder(String name) {
        super(name.trim());
        if (!NAME_REGEXP.matcher(this.name).matches()) {
          throw new IllegalArgumentException("Option name \"" +
              Strings.escape(name) + "\" is invalid");
        }
      }

      public Option.Builder<T> required() {
        this.required = true;
        return this;
      }

      public Option.Builder<T> type(Class<?> type) {
        return converter(ConverterFactory.getDefaultConverter(type));
      }

      public Option.Builder<T> converter(Converter<?> converter) {
        this.converter = converter;
        return this;
      }

      public Option.Builder<T> constraint(Constraint<?> constraint) {
        this.constraint = constraint;
        return this;
      }

    }

  }

  // Argument

  static class Argument extends ToString.Adapter {

    protected final String name;

    protected boolean required;

    protected Converter<?> converter;

    protected Constraint<?> constraint;

    protected Argument(String name) {
      this.name = name;
      this.required = true;
    }

    protected Argument(Builder<?> builder) {
      this.name = builder.name;
      this.required = builder.required;
      this.converter = builder.converter;
      this.constraint = builder.constraint;
    }

    @Override
    public StringBuilder toString(StringBuilder buf) {
      buf.append(name).append("\n");
      return buf;
    }

    // Argument.Builder

    public static abstract class Builder<T> extends Argument implements Buildable<T> {

      protected Builder(String name) {
        super(name.trim());
        if (!NAME_REGEXP.matcher(this.name).matches()) {
          throw new IllegalArgumentException("Argument name \"" +
              Strings.escape(name) + "\" is invalid");
        }
      }

      public Argument.Builder<T> optional() {
        this.required = false;
        return this;
      }

      public Argument.Builder<T> type(Class<?> type) {
        return converter(ConverterFactory.getDefaultConverter(type));
      }

      public Argument.Builder<T> converter(Converter<?> converter) {
        this.converter = converter;
        return this;
      }

      public Argument.Builder<T> constraint(Constraint<?> constraint) {
        this.constraint = constraint;
        return this;
      }

    }

  }
  
  // Regular expression to validate names of commands, options and arguments
  static final Pattern NAME_REGEXP = Pattern.compile("^[a-z]([a-z0-9_\\-\\.]*[a-z0-9])?$",
      Pattern.CASE_INSENSITIVE);

}
