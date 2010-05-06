package com.goodworkalan.go.go;

import java.io.IOException;
import java.io.OutputStream;

public class NullOutputStream extends OutputStream {
    public static OutputStream nullStream() {
        return new NullOutputStream();
    }
    
    @Override
    public void write(int b) throws IOException {
    }
}
