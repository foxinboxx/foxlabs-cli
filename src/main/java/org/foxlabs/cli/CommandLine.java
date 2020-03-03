/*
 * Copyright (C) 2020 FoxLabs
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

import java.util.Iterator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.concurrent.Callable;

import org.foxlabs.validation.converter.Converter;
import org.foxlabs.validation.converter.ConverterFactory;
import org.foxlabs.validation.constraint.Constraint;
import org.foxlabs.validation.constraint.ConstraintFactory;

import org.foxlabs.util.Strings;
import org.foxlabs.util.ToString;
import org.foxlabs.util.function.Buildable;
import org.foxlabs.util.function.Getter;
import org.foxlabs.util.function.Setter;

/**
 *
 * @author Fox Mulder
 * @see CommandLine.Command
 * @see CommandLine.Option
 * @see CommandLine.Argument
 */
public class CommandLine {

  final Map<String, Option<?, ?>> options;

  private final Map<String, Argument<?, ?>> arguments;

  protected CommandLine() {
    this.options = new LinkedHashMap<>();
    this.arguments = new LinkedHashMap<>();
  }

  protected CommandLine(CommandLine.Builder builder) {
    this.options = new LinkedHashMap<>();
    this.arguments = new LinkedHashMap<>();
  }

  // Builder

  public static final class Builder extends CommandLine implements Buildable<CommandLine> {

    public <C extends Runnable & Cloneable> Object command(String name, C command) {
      return null;
    }

    public <C, V> Option.Builder<CommandLine.Builder, C, V> option(Class<V> type, String name, String... aliases) {
      return new Option.Builder<CommandLine.Builder, C, V>(type, name, aliases) {
        @Override public CommandLine.Builder build() {
          CommandLine.Builder.this.options.put(this.prototype.name, new Option<C, V>(this));
          return CommandLine.Builder.this;
        }
      };
    }

    @Override
    public CommandLine build() {
      return new CommandLine(this);
    }

  }

  // Command

  /**
   * The descriptor of the command line command.
   *
   * @param <C> The type of the command handler.
   *
   * @author Fox Mulder
   */
  public static final class Command<C extends Cloneable> extends ToString.Adapter {

    /**
     * The name of the command.
     */
    private final String name;

    /**
     * The options of the command (keys are option names as is).
     */
    private final Map<String, Option<C, ?>> options;

    /**
     * The arguments of the command (keys are argument names in upper case).
     */
    private final Map<String, Argument<C, ?>> arguments;

    /**
     * The subcommands of the command (keys are subcommand names in lower case).
     */
    private final Map<String, Command<?>> subcommands;

    /**
     * The handler of the command.
     */
    private C handler;

    /**
     * Constructs a new command descriptor with the specified name.
     *
     * @param name The name of the command.
     */
    private Command(String name) {
      this.name = Strings.requireNonBlank(name);
      this.options = new LinkedHashMap<>();
      this.arguments = new LinkedHashMap<>();
      this.subcommands = new LinkedHashMap<>();
    }

    /**
     * Constructs a new command descriptor and initializes its properties
     * with properties of the specified builder.
     *
     * @param builder The builder of the command descriptior.
     */
    private Command(Command.Builder<?, C> builder) {
      this.name = builder.prototype.name;
      this.options = Collections.unmodifiableMap(
          new LinkedHashMap<>(builder.prototype.options));
      this.arguments = Collections.unmodifiableMap(
          new LinkedHashMap<>(builder.prototype.arguments));
      this.subcommands = Collections.unmodifiableMap(
          new LinkedHashMap<>(builder.prototype.subcommands));
      this.handler = Objects.requireNonNull(builder.prototype.handler);
    }

    /**
     * Returns name of the command.
     *
     * @return The name of the command.
     */
    public String getName() {
      return this.name;
    }

    /**
     * Returns options of the command.
     *
     * @return The immutable map of options of the command.
     */
    public Map<String, Option<C, ?>> getOptions() {
      return this.options;
    }

    /**
     * Returns arguments of the command.
     *
     * @return The immutable map of arguments of the command.
     */
    public Map<String, Argument<C, ?>> getArguments() {
      return this.arguments;
    }

    /**
     * Returns subscommands of the command.
     *
     * @return The immutable map of subcommands of the command.
     */
    public Map<String, Command<?>> getSubcommands() {
      return this.subcommands;
    }

    /**
     * Returns copy of the command handler.
     *
     * @return A copy of the command handler.
     */
    public C getHandler() {
      // FIXME Should return deep clone of the command handler
      return this.handler;
    }

    /**
     * Determines whether the command can be executed or is just a parameters
     * holder.
     *
     * @return {@code true} if the command can be executed.
     */
    public boolean isRunnable() {
      return this.handler instanceof Runnable
          || this.handler instanceof Callable;
    }

    /**
     * Appends string representation of the command descriptor to the specified
     * buffer.
     *
     * <p>
     * The format is {@code <NAME> [OPTIONS] [SUBCOMMANDS] [ARGUMENTS]}.
     * Where:
     * <ul>
     *   <li>{@code NAME} - the command name in lower case.</li>
     *   <li>{@code OPTIONS} - a list of command options separated by a space
     *       character.</li>
     *   <li>{@code SUBCOMMANDS} - a list of subcommand names in lower case
     *       enclosed in the {@code ()} brackets and separated by the {@code |}
     *       character. The leading {@code ?} character means that subcommands
     *       are optional and the command can be executed on its own.</li>
     *   <li>{@code ARGUMENTS} - a list of command arguments separated by a
     *       space character.</li>
     * </ul>
     * </p>
     *
     * @param buffer The buffer to append.
     * @return A reference to the specified buffer.
     * @see Option#toString(StringBuilder)
     * @see Argument#toString(StringBuilder)
     */
    @Override
    public StringBuilder toString(StringBuilder buffer) {
      buffer.append(getName().toLowerCase());
      getOptions().values().forEach((option) -> option.toString(buffer.append(" ")));
      final Iterator<String> itr = getSubcommands().keySet().iterator();
      if (itr.hasNext()) {
        buffer.append(" (");
        buffer.append(itr.next());
        itr.forEachRemaining((name) -> buffer.append(" | ").append(name));
        buffer.append(")");
        if (isRunnable()) {
          buffer.append("?");
        }
      }
      getArguments().values().forEach((argument) -> argument.toString(buffer.append(" ")));
      return buffer;
    }

    // Command.Builder

    public static abstract class Builder<R, C extends Cloneable> implements Buildable<R> {

      private final Command<C> prototype;

      private Builder(String name) {
        this.prototype = new Command<C>(name);
      }

      public <V> Option.Builder<Command.Builder<R, C>, C, V> option(Class<V> type, String name, String... aliases) {
        return new Option.Builder<Command.Builder<R, C>, C, V>(type, name, aliases) {
          @Override public Command.Builder<R, C> build() {
            Command.Builder.this.prototype.options.put(name, new Option<C, V>(this));
            return Command.Builder.this;
          }
        };
      }

      public <V> Argument.Builder<Command.Builder<R, C>, C, V> argument(Class<V> type, String name) {
        return new Argument.Builder<Command.Builder<R, C>, C, V>(type, name) {
          @Override public Command.Builder<R, C> build() {
            Command.Builder.this.prototype.arguments.put(name.toUpperCase(), new Argument<C, V>(this));
            return Command.Builder.this;
          }
        };
      }

      public <S extends Runnable & Cloneable> Command.Builder<Command.Builder<R, C>, S> subcommand(String name) {
        return new Command.Builder<Command.Builder<R, C>, S>(name) {
          @Override public Command.Builder<R, C> build() {
            Command.Builder.this.prototype.subcommands.put(name.toLowerCase(), new Command<S>(this));
            return Command.Builder.this;
          }
        };
      }

      public Command.Builder<R, C> handler(C handler) {
        this.prototype.handler = Objects.requireNonNull(handler);
        return this;
      }

      /**
       * Returns string representation of the builder current state.
       *
       * @return A string representation of the builder current state.
       * @see Command#toString(StringBuilder)
       */
      @Override
      public String toString() {
        return this.prototype.toString();
      }

    }

  }

  // Parameter

  /**
   * The descriptor of the command line parameter (option or argument).
   *
   * @param <C> The type of the owner command handler.
   * @param <V> The type of the parameter value.
   *
   * @author Fox Mulder
   * @see Option
   * @see Argument
   */
  static abstract class Parameter<C, V> extends ToString.Adapter {

    /**
     * The type of the parameter.
     */
    protected final Class<V> type;

    /**
     * The name of the parameter.
     */
    protected final String name;

    /**
     * The description of the parameter.
     */
    protected String description;

    /**
     * The name of a system property to be used as the parameter value.
     */
    protected String property;

    /**
     * The name of an environment variable to be used as the parameter value.
     */
    protected String variable;

    /**
     * The prompt message to be displayed if the required parameter is missing.
     */
    protected String prompt;

    /**
     * The converter to be used to decode the parameter value from string.
     */
    protected Converter<V> converter;

    /**
     * The constraint to be applied to the parameter value.
     */
    protected Constraint<? super V> constraint;

    /**
     * The getter to be used to get the parameter value.
     */
    protected Getter<C, V, ?> getter;

    /**
     * The setter to be used to set the parameter value.
     */
    protected Setter<C, V, ?> setter;

    /**
     * Whether to display the parameter value during input prompt.
     */
    protected boolean password;

    /**
     * Whether the parameter is required or optional.
     */
    protected boolean required;

    /**
     * Whether the parameter should appear in the usage or help output.
     */
    protected boolean hidden;

    /**
     * Constructs a new parameter descriptor with the specified type and name.
     *
     * @param type The type of the parameter.
     * @param name The name of the parameter.
     */
    private Parameter(Class<V> type, String name) {
      this.type = Objects.requireNonNull(type);
      this.name = Strings.requireNonBlank(name);
      // Set default converter and constraint
      this.converter = ConverterFactory.getDefaultConverter(this.type);
      this.constraint = ConstraintFactory.identity();
    }

    /**
     * Constructs a new parameter descriptor and initializes its properties
     * with properties of the specified builder.
     *
     * @param builder The builder of the parameter descriptior.
     */
    private Parameter(Parameter.Builder<?, ?, C, V, ?> builder) {
      // Check and set required properties first
      this.converter = Objects.requireNonNull(builder.prototype.converter);
      this.constraint = Objects.requireNonNull(builder.prototype.constraint);
      this.setter = Objects.requireNonNull(builder.prototype.setter);
      // Type and name should already be checked
      this.type = builder.prototype.type;
      this.name = builder.prototype.name;
      // Set remaining properties
      this.description = builder.prototype.description;
      this.property = builder.prototype.property;
      this.variable = builder.prototype.variable;
      this.prompt = builder.prototype.prompt;
      this.getter = builder.prototype.getter;
      this.password = builder.prototype.password;
      this.required = builder.prototype.required;
      this.hidden = builder.prototype.hidden;
    }

    /**
     * Returns type of the parameter.
     *
     * @return The type of the parameter.
     */
    public Class<V> getType() {
      return this.type;
    }

    /**
     * Returns name of the parameter.
     *
     * @return The name of the parameter.
     */
    public String getName() {
      return this.name;
    }

    /**
     * Returns description of the parameter.
     *
     * @return The description of the parameter.
     */
    public String getDescription() {
      return this.description;
    }

    /**
     * Returns name of a system property to be used as the parameter value.
     *
     * @return The name of a system property.
     */
    public String getProperty() {
      return this.property;
    }

    /**
     * Returns name of an environment variable to be used as the parameter
     * value.
     *
     * @return The name of an environment variable.
     */
    public String getVariable() {
      return this.variable;
    }

    /**
     * Returns prompt message to be displayed if the required parameter is
     * missing.
     *
     * @return The prompt message.
     */
    public String getPrompt() {
      return this.prompt;
    }

    /**
     * Returns converter to be used to decode the parameter value from string.
     *
     * @return The converter of the parameter value.
     */
    public Converter<V> getConverter() {
      return this.converter;
    }

    /**
     * Returns constraint to be applied to the parameter value.
     *
     * @return The constraint of the parameter value.
     */
    public Constraint<? super V> getConstraint() {
      return this.constraint;
    }

    /**
     * Returns The getter to be used to get the parameter value.
     *
     * @return The getter of the parameter value.
     */
    public Getter<C, V, ?> getGetter() {
      return this.getter;
    }

    /**
     * Returns The setter to be used to set the parameter value.
     *
     * @return The setter of the parameter value.
     */
    public Setter<C, V, ?> getSetter() {
      return this.setter;
    }

    /**
     * Determines whether to display the parameter value during input prompt.
     *
     * @return {@code true} if the parameter value will be displayed.
     */
    public boolean isPassword() {
      return this.password;
    }

    /**
     * Determines whether the parameter is required or optional.
     *
     * @return {@code true} if the parameter is required.
     */
    public boolean isRequired() {
      return this.required;
    }

    /**
     * Determines whether the parameter should appear in the usage or help
     * output.
     *
     * @return {@code true} if the parameter is hidden.
     */
    public boolean isHidden() {
      return this.hidden;
    }

    /**
     * Appends the parameter attributes string to the specified buffer.
     *
     * <p>
     * The possible characters of the attributes string are:
     * <ul>
     *   <li>{@code R} - readable (parameter has getter).</li>
     *   <li>{@code W} - writeable (parameter has setter).</li>
     *   <li>{@code H} - hidden (parameter should not appear in the usage and
     *       help output).</li>
     * </ul>
     * </p>
     *
     * @param buffer The buffer to append.
     * @return A reference to the specified buffer.
     */
    protected StringBuilder appendAttributes(StringBuilder buffer) {
      buffer.append(getGetter() != null ? "R" : "");
      buffer.append(getSetter() != null ? "W" : "");
      buffer.append(isHidden() ? "H" : "");
      return buffer;
    }

    /**
     * Appends the sequence of possible sources of the parameter default value
     * separated by the {@code |} character to the specified buffer.
     *
     * <p>
     * The possible sources of the default value are (in order of priority):
     * <ol>
     *   <li>System property (e.g. <code>#{property.name}</code>).</li>
     *   <li>Environment variable (e.g. <code>${VARIABLE_NAME}</code>).</li>
     *   <li>Input prompt (e.g. {@code ?} - normal prompt or
     *       {@code ***} - password prompt).</li>
     *   <li>Nothing of the above (e.g. {@code null}).</li>
     * </ol>
     * </p>
     *
     * @param buffer The buffer to append.
     * @return A reference to the specified buffer.
     */
    protected StringBuilder appendDefaults(StringBuilder buffer) {
      String separator = "";
      // System property
      if (Strings.isNonEmpty(getProperty())) {
        buffer.append(separator).append("#{").append(getProperty()).append("}");
        separator = " | ";
      }
      // Environment variable
      if (Strings.isNonEmpty(getVariable())) {
        buffer.append(separator).append("${").append(getVariable()).append("}");
        separator = " | ";
      }
      // Input prompt
      if (Strings.isNonEmpty(getPrompt())) {
        buffer.append(separator).append(isPassword() ? "***" : "?");
        separator = " | ";
      }
      // Nothing of the above
      if (separator.isEmpty()) {
        buffer.append("null");
      }
      // Done
      return buffer;
    }

    // Parameter.Builder

    /**
     * The builder of the parameter descriptior.
     *
     * @param <B> The type of the builder subclass.
     * @param <R> The type of the {@link #build()} method result.
     * @param <C> The type of the owner command handler.
     * @param <V> The type of the parameter value.
     * @param <P> The type of the parameter prototype.
     *
     * @author Fox Mulder
     * @see Option.Builder
     * @see Argument.Builder
     */
    @SuppressWarnings("unchecked")
    static abstract class Builder<B extends Builder<B, R, C, V, P>, R, C, V, P extends Parameter<C, V>>
        implements Buildable<R> {

      /**
       * The parameter prototype.
       */
      protected final P prototype;

      /**
       * Constructs a new builder of the parameter descriptor with the
       * specified parameter prototype.
       *
       * @param prototype The parameter prototype.
       */
      private Builder(P prototype) {
        this.prototype = prototype;
      }

      /**
       * Sets description of the parameter.
       *
       * @param text The description of the parameter.
       * @return A reference to this builder.
       */
      public B description(String text) {
        this.prototype.description = Strings.requireNonBlank(text);
        return (B) this;
      }

      /**
       * Sets name of a system property to be used as the parameter value.
       *
       * @param name The name of a system property.
       * @return A reference to this builder.
       */
      public B property(String name) {
        this.prototype.property = Strings.requireNonBlank(name);
        return (B) this;
      }

      /**
       * Sets name of an environment variable to be used as the parameter value.
       *
       * @param name The name of an environment variable.
       * @return A reference to this builder.
       */
      public B variable(String name) {
        this.prototype.variable = Strings.requireNonBlank(name);
        return (B) this;
      }

      /**
       * Sets prompt message to be displayed if the required parameter is
       * missing.
       *
       * @param message The prompt message.
       * @return A reference to this builder.
       */
      public B prompt(String message) {
        this.prototype.prompt = Strings.requireNonBlank(message);
        return (B) this;
      }

      /**
       * Sets converter to be used to decode the parameter value from string.
       *
       * @param converter The converter of the parameter value.
       * @return A reference to this builder.
       */
      public B converter(Converter<V> converter) {
        this.prototype.converter = Objects.requireNonNull(converter);
        return (B) this;
      }

      /**
       * Sets constraint to be applied to the parameter value.
       *
       * @param constraint The constraint of the parameter value.
       * @return A reference to this builder.
       */
      public B constraint(Constraint<? super V> constraint) {
        this.prototype.constraint = Objects.requireNonNull(constraint);
        return (B) this;
      }

      /**
       * Sets getter to be used to get the parameter value.
       *
       * @param getter The getter of the parameter value.
       * @return A reference to this builder.
       */
      public B getter(Getter<C, V, ?> getter) {
        this.prototype.getter = Objects.requireNonNull(getter);
        return (B) this;
      }

      /**
       * Sets setter to be used to set the parameter value.
       *
       * @param setter The setter of the parameter value.
       * @return A reference to this builder.
       */
      public B setter(Setter<C, V, ?> setter) {
        this.prototype.setter = Objects.requireNonNull(setter);
        return (B) this;
      }

      /**
       * Disables display of the parameter value during input prompt.
       *
       * @return A reference to this builder.
       */
      public B password() {
        this.prototype.password = true;
        return (B) this;
      }

      /**
       * Marks the parameter as required.
       *
       * @return A reference to this builder.
       */
      public B required() {
        this.prototype.required = true;
        return (B) this;
      }

      /**
       * Marks the parameter as optional.
       *
       * @return A reference to this builder.
       */
      public B optional() {
        this.prototype.required = false;
        return (B) this;
      }

      /**
       * Marks the parameter as hidden.
       *
       * @return A reference to this builder.
       */
      public B hidden() {
        this.prototype.hidden = true;
        return (B) this;
      }

      /**
       * Returns string representation of the builder current state.
       *
       * @return A string representation of the builder current state.
       * @see Option#toString(StringBuilder)
       * @see Argument#toString(StringBuilder)
       */
      @Override
      public String toString() {
        return this.prototype.toString();
      }

    }

  }

  // Option

  /**
   * The descriptor of the command line option.
   *
   * @param <C> The type of the owner command handler.
   * @param <V> The type of the option value.
   *
   * @author Fox Mulder
   */
  public static final class Option<C, V> extends Parameter<C, V> {

    /**
     * The aliases of the option name.
     */
    private final Set<String> aliases;

    /**
     * Constructs a new option descriptor with the specified type, name and
     * aliases.
     *
     * @param type The type of the option.
     * @param name The name of the option.
     * @param aliases The aliases of the option name.
     */
    private Option(Class<V> type, String name, String... aliases) {
      super(type, name);
      this.aliases = aliases.length > 0
          ? Collections.unmodifiableSet(Arrays.stream(aliases)
              .peek(Strings::requireNonBlank)
              .filter((alias) -> !alias.equals(name))
              .collect(Collectors.<String, Set<String>>toCollection(LinkedHashSet::new)))
          : Collections.emptySet();
    }

    /**
     * Constructs a new option descriptor and initializes its properties
     * with properties of the specified builder.
     *
     * @param builder The builder of the option descriptior.
     */
    private Option(Option.Builder<?, C, V> builder) {
      super(builder);
      this.aliases = builder.prototype.aliases;
    }

    /**
     * Returns aliases of the option name.
     *
     * @return The immutable set of aliases of the option name.
     */
    public Set<String> getAliases() {
      return this.aliases;
    }


    /**
     * Appends string representation of the option descriptor to the specified
     * buffer.
     *
     * <p>
     * The format is: {@code <LB>[ATTRS] <NAME> [ALIASES] : <TYPE><RB> = <INIT>}.
     * Where:
     * <ul>
     *   <li>{@code LB}, {@code RB} - the enclosing brackets ({@code <>} for
     *       required option and {@code []} for optional).
     *   <li>{@code ATTRS} - the option attributes string consists of the
     *       following characters: {@code R} - readable (option has getter),
     *       {@code W} - writeable (option has setter), {@code H} - hidden
     *       (option should not appear in the usage and help output).</li>
     *   <li>{@code NAME} - the name of the option unchanged.</li>
     *   <li>{@code ALIASES} - a list of the option aliases separated by the
     *       {@code |} character.</li>
     *   <li>{@code TYPE} - the type name of the option.</li>
     *   <li>{@code INIT} - the sequence of possible sources of the option
     *       default value enclosed in the {@code ()} brackets and separated by
     *       the {@code |} character.</p>
     * </ul>
     * </p>
     *
     * @param buffer The buffer to append.
     * @return A reference to the specified buffer.
     */
    @Override
    public StringBuilder toString(StringBuilder buffer) {
      buffer.append(isRequired() ? "<" : "[");
      appendAttributes(buffer).append(" ");
      buffer.append(getName());
      getAliases().forEach((alias) -> buffer.append(" | ").append(alias));
      buffer.append(" : ").append(getType().getName());
      buffer.append(isRequired() ? ">" : "]");
      appendDefaults(buffer.append(" = (")).append(")");
      return buffer;
    }

    // Option.Builder

    /**
     * The builder of the option descriptior.
     *
     * @param <R> The type of the {@link #build()} method result.
     * @param <C> The type of the owner command handler.
     * @param <V> The type of the option value.
     *
     * @author Fox Mulder
     */
    public static abstract class Builder<R, C, V>
        extends Parameter.Builder<Builder<R, C, V>, R, C, V, Option<C, V>> {

      /**
       * Constructs a new builder of the option descriptor with the specified
       * option type, name and aliases.
       *
       * @param type The type of the option.
       * @param name The name of the option.
       * @param aliases The aliases of the option name.
       */
      private Builder(Class<V> type, String name, String... aliases) {
        super(new Option<C, V>(type, name, aliases));
      }

    }

  }

  // Argument

  /**
   * The descriptor of the command line argument.
   *
   * @param <C> The type of the owner command handler.
   * @param <V> The type of the argument value.
   *
   * @author Fox Mulder
   */
  public static final class Argument<C, V> extends Parameter<C, V> {

    /**
     * Constructs a new argument descriptor with the specified type and name.
     *
     * @param type The type of the argument.
     * @param name The name of the argument.
     */
    private Argument(Class<V> type, String name) {
      super(type, name);
    }

    /**
     * Constructs a new argument descriptor and initializes its properties
     * with properties of the specified builder.
     *
     * @param builder The builder of the argument descriptior.
     */
    private Argument(Argument.Builder<?, C, V> builder) {
      super(builder);
    }

    /**
     * Appends string representation of the argument descriptor to the specified
     * buffer.
     *
     * <p>
     * The format is: {@code <LB>[ATTRS] <NAME> : <TYPE> = <INIT><RB>}.
     * Where:
     * <ul>
     *   <li>{@code LB}, {@code RB} - the enclosing brackets ({@code <>} for
     *       required argument and {@code []} for optional).
     *   <li>{@code ATTRS} - the argument attributes string consists of the
     *       following characters: {@code R} - readable (argument has getter),
     *       {@code W} - writeable (argument has setter), {@code H} - hidden
     *       (parameter should not appear in the usage and help output).</li>
     *   <li>{@code NAME} - the name of the argument in upper case.</li>
     *   <li>{@code TYPE} - the type name of the argument.</li>
     *   <li>{@code INIT} - the sequence of possible sources of the argument
     *       default value enclosed in the {@code ()} brackets and separated by
     *       the {@code |} character.</p>
     * </ul>
     * </p>
     *
     * @param buffer The buffer to append.
     * @return A reference to the specified buffer.
     */
    @Override
    public StringBuilder toString(StringBuilder buffer) {
      buffer.append(isRequired() ? "<" : "[");
      appendAttributes(buffer).append(" ");
      buffer.append(getName().toUpperCase()).append(" : ").append(getType().getName());
      appendDefaults(buffer.append(" = (")).append(")");
      buffer.append(isRequired() ? ">" : "]");
      return buffer;
    }

    // Argument.Builder

    /**
     * The builder of the argument descriptior.
     *
     * @param <R> The type of the {@link #build()} method result.
     * @param <C> The type of the owner command handler.
     * @param <V> The type of the argument value.
     *
     * @author Fox Mulder
     */
    public static abstract class Builder<R, C, V>
        extends Parameter.Builder<Builder<R, C, V>, R, C, V, Argument<C, V>> {

      /**
       * Constructs a new builder of the argument descriptor with the specified
       * argument type and name.
       *
       * @param type The type of the argument.
       * @param name The name of the argument.
       */
      private Builder(Class<V> type, String name) {
        super(new Argument<C, V>(type, name));
      }

    }

  }

}
