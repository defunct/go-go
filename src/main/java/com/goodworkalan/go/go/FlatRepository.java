package com.goodworkalan.go.go;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class FlatRepository implements Repository {
    private final URI uri;
    
    public FlatRepository(URI uri) {
        this.uri = uri;
    }
    
    public void fetch(File dest, Artifact artifact, Library library) throws IOException {
        if (!dest.exists()) {
            byte[] buffer = new byte[4092];
            FileOutputStream out = new FileOutputStream(dest);
            InputStream in = uri.resolve(artifact.getFileName("", "jar")).toURL().openStream();
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
