package com.goodworkalan.go.go;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LibraryBuilder {
    private List<File> directories = new ArrayList<File>();
    
    public LibraryBuilder addDirectory(File directory) {
        directories.add(directory);
        return this;
    }
}
