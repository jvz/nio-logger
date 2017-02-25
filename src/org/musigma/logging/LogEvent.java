package org.musigma.logging;

/**
 *
 */
public class LogEvent {
    private final CharSequence message;
    private final long timestamp;

    public LogEvent(CharSequence message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public CharSequence getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
