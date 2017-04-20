package io.cdep.cdep.io;

import io.cdep.annotations.NotNull;
import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_FAINT;
import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Methods for dealing with command-line IO, messages, errors, etc.
 */
public class IO {
  final private static IO io = new IO();
  private PrintStream out = AnsiConsole.out;
  private PrintStream err = AnsiConsole.err;
  private boolean ansi = true;

  /**
   * Set the out stream and return the prior out stream.
   */
  public static PrintStream setOut(PrintStream out) {
    PrintStream original = io.out;
    io.out = out;
    return original;
  }

  /**
   * Set the error stream and return the prior out stream.
   */
  public static PrintStream setErr(PrintStream err) {
    PrintStream original = io.err;
    io.err = err;
    return original;
  }

  /**
   * Whether or not streams support ansi codes
   */
  public static boolean setAnsi(boolean ansi) {
    boolean original = io.ansi;
    io.ansi = ansi;
    return original;
  }

  /**
   * Print an info message.
   */
  public static void info(@NotNull String format, Object... args) {
    io.infoImpl(format, args);
  }

  /**
   * Print an info message.
   */
  public static void infogreen(@NotNull String format, Object... args) {
    io.infogreenImpl(format, args);
  }
  /**
   * Print an info message with a line-feed.
   */
  public static void infoln(Object format, Object... args) {
    io.infoImpl(format + "\n", args);
  }

  /**
   * Print an info message with a line-feed.
   */
  public static void errorln(Object format, Object... args) {
    io.infoImpl(format + "\n", args);
  }

  private void infoImpl(@NotNull String format, Object... args) {
    if (ansi) {
      out.print(ansi().a(INTENSITY_FAINT).fg(WHITE).a(String.format(format, args)).reset());
    } else {
      out.printf(format, args);
    }
  }

  private void errorImpl(@NotNull String format, Object... args) {
    if (ansi) {
      err.printf(format, args);
    } else {
      err.print(ansi().fg(RED).a(String.format(format, args)).reset());
    }
  }

  private void infogreenImpl(@NotNull String format, Object... args) {
    if (ansi) {
      out.print(ansi().a(INTENSITY_FAINT).fg(GREEN).a(String.format(format, args)).reset());
    } else {
      out.printf(format, args);
    }
  }
}
