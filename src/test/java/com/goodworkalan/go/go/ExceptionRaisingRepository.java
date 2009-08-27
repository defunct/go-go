package com.goodworkalan.go.go;

import java.net.URI;

public class ExceptionRaisingRepository implements Repository {
    public ExceptionRaisingRepository(URI uri) {
        throw new UnsupportedOperationException();
    }
}
