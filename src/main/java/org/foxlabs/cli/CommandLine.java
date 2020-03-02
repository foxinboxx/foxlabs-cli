package org.foxlabs.cli;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.Collections;
import java.util.stream.Collectors;

import org.foxlabs.validation.converter.Converter;
import org.foxlabs.validation.converter.ConverterFactory;
import org.foxlabs.validation.constraint.Constraint;
import org.foxlabs.validation.constraint.ConstraintFactory;

import org.foxlabs.util.Strings;
import org.foxlabs.util.function.Buildable;
import org.foxlabs.util.function.Getter;
import org.foxlabs.util.function.Setter;

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

  // Parameter

  /**
   * The descriptor of the command line parameter (option or argument).
   *
   * @param <C> The type of the owner command.
   * @param <V> The type of the parameter value.
   *
   * @author Fox Mulder
   * @see Option
   * @see Argument
   */
  static abstract class Parameter<C, V> {

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
     * @param builder The builder of the command line parameter descriptior.
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
     * Returns name of an environment variable to be used as the parameter value.
     *
     * @return The name of an environment variable.
     */
    public String getVariable() {
      return this.variable;
    }

    /**
     * Returns prompt message to be displayed if the required parameter is missing.
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
     * Returns string representation of the parameter.
     *
     * @return The string of the format {@code <NAME>[?]: <TYPE>}.
     */
    @Override
    public String toString() {
      return getName() + (isRequired() ? "" : "?") + ": " + getType().getName();
    }

    // Parameter.Builder

    /**
     * The builder of the command line parameter descriptior.
     *
     * @param <B> The type of the builder subclass.
     * @param <R> The type of the {@link #build()} method result.
     * @param <C> The type of the owner command.
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
       * Constructs a new builder of the command line parameter descriptor with
       * the specified parameter prototype.
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
       * Sets prompt message to be displayed if the required parameter is missing.
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

    }

  }

  // Option

  /**
   * The descriptor of the command line option.
   *
   * @param <C> The type of the owner command.
   * @param <V> The type of the option value.
   *
   * @author Fox Mulder
   */
  public static final class Option<C, V> extends Parameter<C, V> {

    /**
     * The immutable set of aliases of the option name.
     */
    private final Set<String> aliases;

    /**
     * Constructs a new option descriptor with the specified type, name and aliases.
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
              .collect(Collectors.<String, Set<String>>toCollection(LinkedHashSet::new)))
          : Collections.emptySet();
    }

    /**
     * Constructs a new option descriptor and initializes its properties
     * with properties of the specified builder.
     *
     * @param builder The builder of the command line option descriptior.
     */
    private Option(Option.Builder<?, C, V> builder) {
      super(builder);
      this.aliases = builder.prototype.aliases;
    }

    /**
     * Returns immutable set of aliases of the option name.
     *
     * @return The aliases of the option name.
     */
    public Set<String> getAliases() {
      return this.aliases;
    }

    // Option.Builder

    /**
     * The builder of the command line option descriptior.
     *
     * @param <R> The type of the {@link #build()} method result.
     * @param <C> The type of the owner command.
     * @param <V> The type of the option value.
     *
     * @author Fox Mulder
     */
    public static abstract class Builder<R, C, V>
        extends Parameter.Builder<Builder<R, C, V>, R, C, V, Option<C, V>> {

      /**
       * Constructs a new builder of the command line option descriptor with
       * the specified option type, name and aliases.
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
   * @param <C> The type of the owner command.
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
     * @param builder The builder of the command line argument descriptior.
     */
    private Argument(Argument.Builder<?, C, V> builder) {
      super(builder);
    }

    // Argument.Builder

    /**
     * The builder of the command line argument descriptior.
     *
     * @param <R> The type of the {@link #build()} method result.
     * @param <C> The type of the owner command.
     * @param <V> The type of the argument value.
     *
     * @author Fox Mulder
     */
    public static abstract class Builder<R, C, V>
        extends Parameter.Builder<Builder<R, C, V>, R, C, V, Argument<C, V>> {

      /**
       * Constructs a new builder of the command line argument descriptor with
       * the specified argument type and name.
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
