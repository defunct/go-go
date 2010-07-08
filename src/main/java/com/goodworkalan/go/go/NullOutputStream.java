package com.goodworkalan.go.go;

import java.io.OutputStream;

/**
 * An output stream that does nothing.
 *
 * @author Alan Gutierrez
 */
public class NullOutputStream extends OutputStream {
    /**
     * Create a null output stream.
     * 
     * @return A null output stream.
     */
    public static OutputStream nullStream() {
        return new NullOutputStream();
    }

    /**
     * Does nothing.
     * 
     * @param b
     *            The byte to write.
     */
    @Override
    public void write(int b) {
    }
}
