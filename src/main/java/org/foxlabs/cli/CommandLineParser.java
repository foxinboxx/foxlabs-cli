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

public final class CommandLineParser extends ToString.Adapter {
  
  private CommandLineParser(Builder builder) {
    
  }
  
  public void parse(String... args) {
    
  }
  
  @Override
  public StringBuilder toString(StringBuilder buf) {
    return buf;
  }
  
  public static Builder newBuilder() {
    return new Builder(null);
  }
  
  // Builder
  
  public static final class Builder implements Buildable<CommandLineParser> {
    
    protected final String program;
    
    protected final Map<String, Command> commands;
    
    protected Builder(String program) {
      this.program = program;
      this.commands = new LinkedHashMap<>();
    }
    
    public Option.Builder<CommandLineParser.Builder> option(String name) {
      return new Option.Builder<CommandLineParser.Builder>(name) {
        @Override public CommandLineParser.Builder build() {
          return CommandLineParser.Builder.this;
        }
      };
    }
    
    public Command.Builder<CommandLineParser.Builder> command(String name) {
      return new Command.Builder<CommandLineParser.Builder>(name) {
        @Override public CommandLineParser.Builder build() {
          commands.put(name.toLowerCase(), new Command(this));
          return CommandLineParser.Builder.this;
        }
      };
    }
    
    @Override
    public CommandLineParser build() {
      return new CommandLineParser(this);
    }
    
  }
  
  // Command
  
  static class Command extends ToString.Adapter {
    
    protected final String name;
    
    protected final Map<String, Option> options;
    
    protected final Map<String, Argument> arguments;
    
    protected final Map<String, Command> subcommands;
    
    protected Command(String name) {
      this.name = name;
      this.options = new LinkedHashMap<>();
      this.arguments = new LinkedHashMap<>();
      this.subcommands = new LinkedHashMap<>();
    }
    
    protected Command(Builder<?> builder) {
      this.name = builder.name;
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
      
      protected Builder(String name) {
        super(name.trim());
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
      
      public Command.Builder<Command.Builder<T>> subcommand(String name) {
        return new Command.Builder<Command.Builder<T>>(name) {
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
    
    protected Converter<?> converter;
    
    protected Constraint<?> constraint;
    
    protected Option(String name) {
      this.name = name;
    }
    
    protected Option(Builder<?> builder) {
      this.name = builder.name;
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
      
      public Builder<T> converter(Class<?> type) {
        return converter(ConverterFactory.getDefaultConverter(type));
      }
      
      public Builder<T> converter(Converter<?> converter) {
        this.converter = converter;
        return this;
      }
      
      public Builder<T> constraint(Constraint<?> constraint) {
        this.constraint = constraint;
        return this;
      }
      
    }

  }

  // Argument

  static class Argument extends ToString.Adapter {
    
    protected final String name;
    
    protected Converter<?> converter;
    
    protected Constraint<?> constraint;
    
    protected Argument(String name) {
      this.name = name;
    }
    
    protected Argument(Builder<?> builder) {
      this.name = builder.name;
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
        super(name);
        if (!NAME_REGEXP.matcher(this.name).matches()) {
          throw new IllegalArgumentException("Argument name \"" +
              Strings.escape(name) + "\" is invalid");
        }
      }
      
      public Builder<T> converter(Class<?> type) {
        return converter(ConverterFactory.getDefaultConverter(type));
      }
      
      public Builder<T> converter(Converter<?> converter) {
        this.converter = converter;
        return this;
      }
      
      public Builder<T> constraint(Constraint<?> constraint) {
        this.constraint = constraint;
        return this;
      }
      
    }
    
  }
  
  static final Pattern NAME_REGEXP =
      Pattern.compile("^[a-z]([a-z0-9_\\-\\.]*[a-z0-9])?$", Pattern.CASE_INSENSITIVE);

}
