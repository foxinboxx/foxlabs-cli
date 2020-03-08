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
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.function.Supplier;
import java.util.concurrent.Callable;

import org.foxlabs.common.Objects;
import org.foxlabs.common.Strings;
import org.foxlabs.common.function.ToString;
import org.foxlabs.common.function.Buildable;
import org.foxlabs.common.function.Getter;
import org.foxlabs.common.function.Setter;

import org.foxlabs.validation.converter.Converter;
import org.foxlabs.validation.converter.ConverterFactory;
import org.foxlabs.validation.constraint.Constraint;
import org.foxlabs.validation.constraint.ConstraintFactory;

import static org.foxlabs.common.Predicates.*;

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

    @Override public CommandLine build() {
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
   * @see Command.Builder
   */
  public static final class Command<C> extends ToString.Adapter {

    /**
     * The name of the command.
     */
    private final String name;

    /**
     * The description of the command.
     */
    private String description;

    /**
     * The options of the command (keys are option names).
     */
    private final Map<String, Option<C, ?>> options;

    /**
     * The arguments of the command (keys are argument names).
     */
    private final Map<String, Argument<C, ?>> arguments;

    /**
     * The subcommands of the command (keys are subcommand names).
     */
    private final Map<String, Command<?>> subcommands;

    /**
     * The provider of the command handlers.
     */
    private Supplier<C> provider;

    /**
     * Whether the command is runnable.
     */
    private Boolean runnable;

    /**
     * Constructs a new command descriptor with the specified name.
     *
     * @param name The name of the command.
     */
    private Command(String name) {
      this.name =  require(name, STRING_NON_WHITESPACED);
      this.options = new LinkedHashMap<>();
      this.arguments = new LinkedHashMap<>();
      this.subcommands = new LinkedHashMap<>();
    }

    /**
     * Constructs a new command descriptor and initializes its properties
     * with properties of the specified builder.
     *
     * @param builder The builder of the command descriptor.
     */
    private Command(Command.Builder<?, C> builder) {
      // Check and set required properties first
      this.provider = requireNonNull(builder.prototype.provider);
      // Name should already be checked
      this.name = builder.prototype.name;
      // Set remaining properties
      this.description = builder.prototype.description;
      this.options = Collections.unmodifiableMap(
          new LinkedHashMap<>(builder.prototype.options));
      this.arguments = Collections.unmodifiableMap(
          new LinkedHashMap<>(builder.prototype.arguments));
      this.subcommands = Collections.unmodifiableMap(
          new LinkedHashMap<>(builder.prototype.subcommands));
    }

    /**
     * Returns name of the command.
     *
     * @return The name of the command.
     */
    public String getName() {
      return name;
    }

    /**
     * Returns description of the command.
     *
     * @return The description of the command.
     */
    public String getDescription() {
      return description;
    }

    /**
     * Returns options of the command.
     *
     * @return The immutable map of options of the command.
     */
    public Map<String, Option<C, ?>> getOptions() {
      return options;
    }

    /**
     * Returns arguments of the command.
     *
     * @return The immutable map of arguments of the command.
     */
    public Map<String, Argument<C, ?>> getArguments() {
      return arguments;
    }

    /**
     * Returns subcommands of the command.
     *
     * @return The immutable map of subcommands of the command.
     */
    public Map<String, Command<?>> getSubcommands() {
      return subcommands;
    }

    /**
     * Returns provider of the command handlers.
     *
     * @return The provider of the command handlers.
     */
    public Supplier<C> getProvider() {
      return provider;
    }

    /**
     * Determines whether the command can be executed or is just a parameters
     * holder.
     *
     * @return {@code true} if the command handler implements {@link Runnable}
     *         or {@link Callable} interface.
     */
    public boolean isRunnable() {
      if (runnable == null) {
        if (provider != null) {
          final C handler = provider.get();
          runnable = handler instanceof Runnable || handler instanceof Callable;
        } else {
          return false;
        }
      }
      return runnable;
    }

    /**
     * Appends string representation of the command descriptor to the specified
     * buffer.
     *
     * <p>
     * The format is {@code <NAME> [OPTIONS] [SUBCOMMANDS] [ARGUMENTS]}.
     * Where:
     * <ul>
     *   <li>{@code NAME} - the command name.</li>
     *   <li>{@code OPTIONS} - a list of command options separated by a space
     *       character.</li>
     *   <li>{@code SUBCOMMANDS} - a list of subcommand names enclosed in the
     *       {@code ()} brackets and separated by the {@code |} character.
     *       The leading {@code ?} character means that subcommands are optional
     *       and the command can be executed on its own.</li>
     *   <li>{@code ARGUMENTS} - a list of command arguments separated by a
     *       space character.</li>
     * </ul>
     * </p>
     *
     * @param buffer The buffer to append.
     * @return A reference to the specified buffer.
     * @see CommandLine.Option#toString(StringBuilder)
     * @see CommandLine.Argument#toString(StringBuilder)
     */
    @Override public StringBuilder toString(StringBuilder buffer) {
      // <NAME>
      buffer.append(name);
      // [OPTIONS]
      options.values().forEach((option) -> option.toString(buffer.append(" ")));
      // [SUBCOMMANDS]
      final Iterator<String> itr = subcommands.keySet().iterator();
      if (itr.hasNext()) {
        buffer.append(" (");
        buffer.append(itr.next());
        itr.forEachRemaining((name) -> buffer.append(" | ").append(name));
        buffer.append(")");
        if (isRunnable()) {
          buffer.append("?");
        }
      }
      // [ARGUMENTS]
      arguments.values().forEach((argument) -> argument.toString(buffer.append(" ")));
      // Done
      return buffer;
    }

    // Command.Builder

    /**
     * The builder of the command descriptor.
     *
     * @param <R> The type of the {@link #build()} method result.
     * @param <C> The type of the command handler.
     *
     * @author Fox Mulder
     */
    public static abstract class Builder<R, C> implements Buildable<R> {

      /**
       * The command prototype.
       */
      private final Command<C> prototype;

      /**
       * Constructs a new command descriptor with the specified name.
       *
       * @param name The name of the command.
       */
      private Builder(String name) {
        this.prototype = new Command<C>(name);
      }

      /**
       * Sets description of the command.
       *
       * @param text The description of the command.
       * @return A reference to this builder.
       */
      public Command.Builder<R, C> description(String text) {
        prototype.description = require(text, STRING_NON_BLANK);
        return this;
      }

      /**
       * Returns reference to the {@link Option.Builder} for further
       * construction of the command option.
       *
       * @param <V> The type of the option value.
       * @param type The type of the option.
       * @param name The name of the option.
       * @param aliases The aliases of the option name.
       * @return A reference to the {@link Option.Builder}.
       * @throws IllegalStateException if option with the specified name is
       *         already defined.
       */
      public <V> Option.Builder<Command.Builder<R, C>, C, V> option(Class<V> type, String name, String... aliases) {
        final Builder<R, C> self = this;
        return new Option.Builder<Command.Builder<R, C>, C, V>(type, name, aliases) {
          @Override public Command.Builder<R, C> build() {
            final Option<C, V> option = new Option<>(this);
            if (self.prototype.options.putIfAbsent(option.name, option) != null) {
              throw new IllegalStateException("Command " + self.prototype.name +
                  ": Option already defined: " + option.name);
            }
            return self;
          }
        };
      }

      /**
       * Returns reference to the {@link Argument.Builder} for further
       * construction of the command argument.
       *
       * @param <V> The type of the argument value.
       * @param type The type of the argument.
       * @param name The name of the argument.
       * @return A reference to the {@link Argument.Builder}.
       * @throws IllegalStateException if argument with the specified name is
       *         already defined.
       */
      public <V> Argument.Builder<Command.Builder<R, C>, C, V> argument(Class<V> type, String name) {
        final Builder<R, C> self = this;
        return new Argument.Builder<Command.Builder<R, C>, C, V>(type, name) {
          @Override public Command.Builder<R, C> build() {
            final Argument<C, V> argument = new Argument<>(this);
            if (self.prototype.arguments.putIfAbsent(argument.name, argument) != null) {
              throw new IllegalStateException("Command " + self.prototype.name +
                  ": Argument already defined: " + argument.name);
            }
            return self;
          }
        };
      }

      /**
       * Returns reference to the {@link Command.Builder} for further
       * construction of the subcommand.
       *
       * @param <S> The type of the subcommand handler.
       * @param name The name of the subcommand.
       * @return A reference to the {@link Command.Builder}.
       * @throws IllegalStateException if subcommand with the specified name is
       *         already defined.
       */
      public <S> Command.Builder<Command.Builder<R, C>, S> subcommand(String name) {
        final Builder<R, C> self = this;
        return new Command.Builder<Command.Builder<R, C>, S>(name) {
          @Override public Command.Builder<R, C> build() {
            final Command<S> subcommand = new Command<>(this);
            if (self.prototype.subcommands.putIfAbsent(subcommand.name, subcommand) != null) {
              throw new IllegalStateException("Command " + self.prototype.name +
                  ": Subcommand already defined: " + subcommand.name);
            }
            return self;
          }
        };
      }

      /**
       * Sets provider of the command handlers.
       *
       * @param factory The provider of the command handlers.
       * @return A reference to this builder.
       */
      public Command.Builder<R, C> provider(Supplier<C> factory) {
        prototype.provider = requireNonNull(factory);
        return this;
      }

      /**
       * Returns string representation of the builder current state.
       *
       * @return A string representation of the builder current state.
       * @see CommandLine.Command#toString(StringBuilder)
       */
      @Override public String toString() {
        return prototype.toString();
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
   * @see Parameter.Builder
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
      this.type = requireNonNull(type);
      this.name = require(name, STRING_NON_WHITESPACED);
      this.converter = ConverterFactory.getDefaultConverter(this.type);
      this.constraint = ConstraintFactory.identity();
      this.getter = Getter.nullStub();
      this.setter = Setter.unsupportedStub();
    }

    /**
     * Constructs a new parameter descriptor and initializes its properties
     * with properties of the specified builder.
     *
     * @param builder The builder of the parameter descriptor.
     */
    private Parameter(Parameter.Builder<?, ?, C, V, ?> builder) {
      this.type = builder.prototype.type;
      this.name = builder.prototype.name;
      this.description = builder.prototype.description;
      this.property = builder.prototype.property;
      this.variable = builder.prototype.variable;
      this.prompt = builder.prototype.prompt;
      this.constraint = builder.prototype.constraint;
      this.converter = builder.prototype.converter;
      this.getter = builder.prototype.getter;
      this.setter = builder.prototype.setter;
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
      return type;
    }

    /**
     * Returns name of the parameter.
     *
     * @return The name of the parameter.
     */
    public String getName() {
      return name;
    }

    /**
     * Returns description of the parameter.
     *
     * @return The description of the parameter.
     */
    public String getDescription() {
      return description;
    }

    /**
     * Returns name of a system property to be used as the parameter value.
     *
     * @return The name of a system property.
     */
    public String getProperty() {
      return property;
    }

    /**
     * Returns name of an environment variable to be used as the parameter
     * value.
     *
     * @return The name of an environment variable.
     */
    public String getVariable() {
      return variable;
    }

    /**
     * Returns prompt message to be displayed if the required parameter is
     * missing.
     *
     * @return The prompt message.
     */
    public String getPrompt() {
      return prompt;
    }

    /**
     * Returns converter to be used to decode the parameter value from string.
     *
     * @return The converter of the parameter value.
     */
    public Converter<V> getConverter() {
      return converter;
    }

    /**
     * Returns constraint to be applied to the parameter value.
     *
     * @return The constraint of the parameter value.
     */
    public Constraint<? super V> getConstraint() {
      return constraint;
    }

    /**
     * Returns The getter to be used to get the parameter value.
     *
     * @return The getter of the parameter value.
     */
    public Getter<C, V, ?> getGetter() {
      return getter;
    }

    /**
     * Returns The setter to be used to set the parameter value.
     *
     * @return The setter of the parameter value.
     */
    public Setter<C, V, ?> getSetter() {
      return setter;
    }

    /**
     * Determines whether to display the parameter value during input prompt.
     *
     * @return {@code true} if the parameter value will be displayed.
     */
    public boolean isPassword() {
      return password;
    }

    /**
     * Determines whether the parameter is required or optional.
     *
     * @return {@code true} if the parameter is required.
     */
    public boolean isRequired() {
      return required;
    }

    /**
     * Determines whether the parameter should appear in the usage or help
     * output.
     *
     * @return {@code true} if the parameter is hidden.
     */
    public boolean isHidden() {
      return hidden;
    }

    /**
     * Appends the parameter attributes string to the specified buffer.
     *
     * <p>
     * The possible characters of the attributes string are:
     * <ul>
     *   <li>{@code R} - readable (parameter has getter).</li>
     *   <li>{@code W} - writable (parameter has setter).</li>
     *   <li>{@code H} - hidden (parameter should not appear in the usage and
     *       help output).</li>
     * </ul>
     * </p>
     *
     * @param buffer The buffer to append.
     * @return A reference to the specified buffer.
     */
    protected StringBuilder appendAttributes(StringBuilder buffer) {
      buffer.append(getter != null ? "R" : "");
      buffer.append(setter != null ? "W" : "");
      buffer.append(hidden ? "H" : "");
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
      if (Strings.isNonEmpty(property)) {
        buffer.append(separator).append("#{").append(property).append("}");
        separator = " | ";
      }
      // Environment variable
      if (Strings.isNonEmpty(variable)) {
        buffer.append(separator).append("${").append(variable).append("}");
        separator = " | ";
      }
      // Input prompt
      if (Strings.isNonEmpty(prompt)) {
        buffer.append(separator).append(password ? "***" : "?");
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
     * The builder of the parameter descriptor.
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
        prototype.description = require(text, STRING_NON_BLANK);
        return Objects.cast(this);
      }

      /**
       * Sets name of a system property to be used as the parameter value.
       *
       * @param name The name of a system property.
       * @return A reference to this builder.
       */
      public B property(String name) {
        prototype.property = require(name, STRING_NON_WHITESPACED);
        return Objects.cast(this);
      }

      /**
       * Sets name of an environment variable to be used as the parameter value.
       *
       * @param name The name of an environment variable.
       * @return A reference to this builder.
       */
      public B variable(String name) {
        prototype.variable = require(name, STRING_NON_WHITESPACED);
        return Objects.cast(this);
      }

      /**
       * Sets prompt message to be displayed if the required parameter is
       * missing.
       *
       * @param message The prompt message.
       * @return A reference to this builder.
       */
      public B prompt(String message) {
        prototype.prompt = require(message, STRING_NON_BLANK);
        return Objects.cast(this);
      }

      /**
       * Sets converter to be used to decode the parameter value from string.
       *
       * @param converter The converter of the parameter value.
       * @return A reference to this builder.
       */
      public B converter(Converter<V> converter) {
        prototype.converter = requireNonNull(converter);
        return Objects.cast(this);
      }

      /**
       * Sets constraint to be applied to the parameter value.
       *
       * @param constraint The constraint of the parameter value.
       * @return A reference to this builder.
       */
      public B constraint(Constraint<? super V> constraint) {
        prototype.constraint = requireNonNull(constraint);
        return Objects.cast(this);
      }

      /**
       * Sets getter to be used to get the parameter value.
       *
       * @param getter The getter of the parameter value.
       * @return A reference to this builder.
       */
      public B getter(Getter<C, V, ?> getter) {
        prototype.getter = requireNonNull(getter);
        return Objects.cast(this);
      }

      /**
       * Sets setter to be used to set the parameter value.
       *
       * @param setter The setter of the parameter value.
       * @return A reference to this builder.
       */
      public B setter(Setter<C, V, ?> setter) {
        prototype.setter = requireNonNull(setter);
        return Objects.cast(this);
      }

      /**
       * Disables display of the parameter value during input prompt.
       *
       * @return A reference to this builder.
       */
      public B password() {
        prototype.password = true;
        return Objects.cast(this);
      }

      /**
       * Marks the parameter as required.
       *
       * @return A reference to this builder.
       */
      public B required() {
        prototype.required = true;
        return Objects.cast(this);
      }

      /**
       * Marks the parameter as optional.
       *
       * @return A reference to this builder.
       */
      public B optional() {
        prototype.required = false;
        return Objects.cast(this);
      }

      /**
       * Marks the parameter as hidden.
       *
       * @return A reference to this builder.
       */
      public B hidden() {
        prototype.hidden = true;
        return Objects.cast(this);
      }

      /**
       * Returns string representation of the builder current state.
       *
       * @return A string representation of the builder current state.
       * @see CommandLine.Option#toString(StringBuilder)
       * @see CommandLine.Argument#toString(StringBuilder)
       */
      @Override public String toString() {
        return prototype.toString();
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
   * @see Option.Builder
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
              .peek((alias) -> require(alias, STRING_NON_WHITESPACED))
              .filter((alias) -> !alias.equals(name))
              .collect(Collectors.<String, Set<String>>toCollection(LinkedHashSet::new)))
          : Collections.emptySet();
    }

    /**
     * Constructs a new option descriptor and initializes its properties
     * with properties of the specified builder.
     *
     * @param builder The builder of the option descriptor.
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
      return aliases;
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
     *       {@code W} - writable (option has setter), {@code H} - hidden
     *       (option should not appear in the usage and help output).</li>
     *   <li>{@code NAME} - the name of the option.</li>
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
    @Override public StringBuilder toString(StringBuilder buffer) {
      // <LB>
      buffer.append(required ? "<" : "[");
      // [ATTRS]
      appendAttributes(buffer).append(" ");
      // <NAME>
      buffer.append(name);
      // [ALIASES]
      aliases.forEach((alias) -> buffer.append(" | ").append(alias));
      // <TYPE>
      buffer.append(" : ").append(type.getName());
      // <RB>
      buffer.append(required ? ">" : "]");
      // <INIT>
      appendDefaults(buffer.append(" = (")).append(")");
      // Done
      return buffer;
    }

    // Option.Builder

    /**
     * The builder of the option descriptor.
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
   * @see Argument.Builder
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
     * @param builder The builder of the argument descriptor.
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
     *       {@code W} - writable (argument has setter), {@code H} - hidden
     *       (parameter should not appear in the usage and help output).</li>
     *   <li>{@code NAME} - the name of the argument.</li>
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
    @Override public StringBuilder toString(StringBuilder buffer) {
      // <LB>
      buffer.append(required ? "<" : "[");
      // [ATTRS]
      appendAttributes(buffer).append(" ");
      // <NAME> : <TYPE>
      buffer.append(name).append(" : ").append(type.getName());
      // <INIT>
      appendDefaults(buffer.append(" = (")).append(")");
      // <RB>
      buffer.append(required ? ">" : "]");
      // Done
      return buffer;
    }

    // Argument.Builder

    /**
     * The builder of the argument descriptor.
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
