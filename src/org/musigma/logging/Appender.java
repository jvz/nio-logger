package org.musigma.logging;

/**
 *
 */
public interface Appender extends AutoCloseable {
    void append(LogEvent event);
}
