package io.cdep.cdep.io;

import java.io.PrintStream;

/**
 * Methods for dealing with command-line IO, messages, errors, etc.
 */
public class IO {
  final private static IO io = new IO();
  private PrintStream out = System.out;

  /**
   * Set the out stream and return the prior out stream.
   */
  public static PrintStream setOut(PrintStream out) {
    PrintStream original = io.out;
    io.out = out;
    return original;
  }

  private void infoImpl(String format, Object ... args) {
    out.printf(format, args);
  }

  /**
   * Print an info message.
   */
  public static void info(String format, Object ... args) {
    io.infoImpl(format, args);
  }

  /**
   * Print an info message with a line-feed.
   */
  public static void infoln(Object format, Object ... args) {
    io.infoImpl(format + "\n", args);
  }
}
