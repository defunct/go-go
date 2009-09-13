package com.goodworkalan.go.go;

import java.net.URI;

public class RepositoryLine {
    public final String type;
    
    public final URI uri;
    
    public RepositoryLine(String type, URI uri) {
        if (type.length() == 0) {
            throw new GoException(0);
        }
        if (!Character.isJavaIdentifierStart(type.charAt(0))) {
            throw new GoException(0);
        }
        for (int i = 1, stop = type.length(); i < stop; i++) {
            if (!Character.isJavaIdentifierPart(type.charAt(i))) {
                throw new GoException(0);
            }
        }
        this.type = type;
        this.uri = uri;
    }    
}
