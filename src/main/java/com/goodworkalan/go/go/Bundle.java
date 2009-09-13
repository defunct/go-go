package com.goodworkalan.go.go;

import java.net.URI;


public interface Bundle {
    public void repsitory(String type, URI uri);

    public void include(Artifact artifact);
}
