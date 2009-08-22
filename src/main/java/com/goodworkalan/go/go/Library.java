package com.goodworkalan.go.go;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

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
    
    public List<Artifact> getImmediateDependencies(Artifact artifact) {
        final List<Artifact> artifacts = new ArrayList<Artifact>();
        ContentHandler handler = new DefaultHandler() {
            int depth;
            
            boolean dependencies;
            boolean dependency;
            
            StringBuilder characters = new StringBuilder();
            
            String group;
            String name;
            String version;
            String scope;
            String optional;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
                depth++;
                if (depth == 2 && localName.equals("dependencies")) {
                    dependencies = true;
                } else if (dependencies && depth == 4) {
                    dependency = true;
                }
            }
            
            @Override
            public void characters(char[] ch, int start, int length)
            throws SAXException {
                if (dependency) {
                    characters.append(ch, start, length);
                }
            }
           
            @Override
            public void endElement(String uri, String localName, String qName)
            throws SAXException {
                if (depth == 2 && localName.equals("dependencies")) {
                    dependencies = false;
                } else if (dependencies && depth == 3) {
                    if ((scope == null || scope.equals("compile") || scope.equals("runtime")) && (optional == null || !"true".equals(optional))) {
                        artifacts.add(new Artifact(group, name, version));
                    }
                    optional = scope = version = name = group = null;
                } else if (depth == 4 && dependency) {
                    dependency = false;
                    if (localName.equals("groupId")) {
                        group = characters.toString();
                    } else if (localName.equals("artifactId")) {
                        name = characters.toString();
                    } else if (localName.equals("version")) {
                        version = characters.toString();
                    } else if (localName.equals("scope")) {
                        scope = characters.toString();
                    } else if (localName.equals("optional")) {
                        optional = characters.toString();
                    }
                    characters.setLength(0);
                }
                depth--;
            }
        };
        XMLReader xr;
        try {
            xr = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            throw new GoException(0, e);
        }
        xr.setContentHandler(handler);
        File file = new File(dir, artifact.getPath("", "pom"));
        try {
            xr.parse(new InputSource(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw new GoException(0, e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new GoException(0, e);
        }
        return artifacts;
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
                    subDependencies.addAll(getImmediateDependencies(dependency));
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
