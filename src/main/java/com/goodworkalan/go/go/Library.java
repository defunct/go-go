package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Library {
    private final File dir;
    
    public Library(File dir) {
        this.dir = dir;
    }
    
    public boolean contains(Artifact artifact) {
        String path = artifact.getPath("", "jar");
        File file = new File(dir, path);
        return file.exists();
    }
    

    
    private void resolve(List<Repository> repositories, List<Artifact> artifacts, List<Artifact> dependencies, Set<String> seen) {
        if (!dependencies.isEmpty()) {
            List<Artifact> subDependencies = new ArrayList<Artifact>();
            for (Artifact dependency : dependencies) {
                if (!seen.contains(dependency.getKey())) {
                    seen.add(dependency.getKey());
                    artifacts.add(dependency);
                    if (!contains(dependency)) {
                        for (Repository repository : repositories) {
                            System.out.println(repository);
                        }
                    }
                    subDependencies.addAll(new POMReader(dir).getImmediateDependencies(dependency));
                }
            }
            resolve(repositories, artifacts, subDependencies, seen);
        }
    }

    public List<Artifact> resolve(List<Repository> repositories, List<Artifact> artifacts) {
        List<Artifact> dependencies = new ArrayList<Artifact>();
        resolve(repositories, dependencies, artifacts, new HashSet<String>());
        return dependencies;
    }
    
    public List<Artifact> resolve(List<Artifact> artifacts) {
        return resolve(Collections.<Repository>emptyList(), artifacts);
    }
    
    public ClassLoader getClassLoader(List<Artifact> artifacts, ClassLoader parent, Set<String> seen) {
        List<File> path = new ArrayList<File>();
        for (Artifact artifact : resolve(artifacts)) {
            if (!seen.contains(artifact.getKey())) {
                seen.add(artifact.getKey());
                path.add(new File(dir, artifact.getPath("", "jar")));
            }
        }
        if (path.isEmpty()) {
            return parent;
        }
        URL[] urls = new URL[path.size()];
        for (int i = 0, stop = path.size(); i < stop; i++) {
            try {
                urls[i] = new URL("jar:" + path.get(i).toURL().toExternalForm() + "!/");
            } catch (MalformedURLException e) {
                throw new GoException(0, e);
            }
        }
        return new URLClassLoader(urls, parent);
    }
}
