package com.goodworkalan.go.go;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryBuilder {
    private List<File> directories = new ArrayList<File>();
    
    private Map<String, Class<? extends RepositoryClient>> repositoryClasses = new HashMap<String, Class<? extends RepositoryClient>>();
    
    public LibraryBuilder addDirectory(File directory) {
        directories.add(directory);
        return this;
    }
    
    public LibraryBuilder addRepositoryType(String name, Class<? extends RepositoryClient> repositoryClass) {
        repositoryClasses.put(name, repositoryClass);
        return this;
    }
}
