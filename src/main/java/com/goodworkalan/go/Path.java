package com.goodworkalan.go;

import go.go.MixException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class Path {
    /** The list of files in the path. */
    private final List<File> files;

    /**
     * Create a path with the given list of files.
     * 
     * @param files
     *            The list of files in the path.
     */
    public Path(List<File> files) {
        this.files = files;
    }
    
    public ClassLoader createClassLoader(ClassLoader parent) {
        URL[] urls = new URL[files.size()];
        for (int i = 0, stop = files.size(); i < stop; i++) {
            try {
                urls[i] = new URL("jar:" + files.get(i).toURL().toExternalForm() + "!/");
            } catch (MalformedURLException e) {
                throw new MixException(0, e);
            }
        }
        return new URLClassLoader(urls, parent);
    }
}
