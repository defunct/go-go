package com.goodworkalan.go.go;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

public class MavenRepository implements Repository {
    private final URI uri;
    
    public MavenRepository(URI uri) {
        this.uri = uri;
    }

    public void get(File dir, Artifact artifact) throws IOException {
        File full = new File(dir, artifact.getPath("", "jar"));
        if (!full.exists()) {
            byte[] buffer = new byte[4092];
            FileOutputStream out = new FileOutputStream(full);
            InputStream in = uri.resolve(artifact.getPath("", "jar")).toURL().openStream();
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    /**
     * Get an iterator over the artifacts in the Maven repository.
     * 
     * @return An iterator over the artifacts in the Maven repository.
     */
    public Iterable<Artifact> artifacts() {
        // FIXME A glob to match poms, then a parse method for Artifact.
        return new ArrayList<Artifact>();
    }
}
