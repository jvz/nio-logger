package org.musigma.logging;

import java.nio.ByteBuffer;

/**
 *
 */
public interface Layout {
    ByteBuffer encode(LogEvent event);
}
