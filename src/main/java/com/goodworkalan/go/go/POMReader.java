package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class POMReader {
    private final File dir;

    public POMReader(File dir) {
        this.dir = dir;
    }

    void getMetaData(final Artifact artifact, final Properties properties, final Map<String, Artifact> dependencies) {
        ContentHandler handler = new DefaultHandler() {
            int depth;

            boolean parent;
            boolean capture;
            boolean props;
            
            String groupId;
            String artifactId;
            String version;
            
            StringBuilder characters = new StringBuilder();
            
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
                depth++;
                if (depth == 2 && localName.equals("parent")) {
                    parent = true;
                } else if (depth == 3 && parent) {
                    capture = true;
                } else if (depth == 2 && localName.equals("properties")) {
                    props = true;
                } else if (depth == 3 && props) {
                    capture = true;
                }
            }
            
            @Override
            public void characters(char[] ch, int start, int length)
            throws SAXException {
                if (capture) {
                    characters.append(ch, start, length);
                }
            } 
            
            @Override
            public void endElement(String uri, String localName, String qName)
            throws SAXException {
                if (depth == 2 && localName.equals("parent")) {
                    parent = false;
                    Artifact parent = new Artifact(groupId, artifactId, version);
                    groupId = artifactId = version = null;
                    getDependencyManagement(parent, dependencies);
                } else if (depth == 2 && localName.equals("properties")) {
                    props = false;
                } else if (capture) {
                    capture = false;
                    if (localName.equals("groupId")) {
                        groupId = characters.toString();
                    } else if (localName.equals("artifactId")) {
                        artifactId = characters.toString();
                    } else if (localName.equals("version")) {
                        version = characters.toString();
                    } else if (props) {
                        properties.setProperty(localName, characters.toString());
                    }
                    characters.setLength(0);
                }
                depth--;
            }
        };
        parse(artifact, handler);
    }

    void parse(final Artifact artifact, ContentHandler handler) {
        File file = new File(dir, artifact.getPath("", "pom"));
        try {
            try {
                parse(artifact, handler, new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw new GoException(POM_FILE_NOT_FOUND, e);
            }
        } catch (GoException e) {
            throw e.put("file", file);
        }
    }
    
    void parse(final Artifact artifact, ContentHandler handler, InputStream in) {
        try {
            XMLReader xr;
            try {
                xr = XMLReaderFactory.createXMLReader();
            } catch (SAXException e) {
                throw new GoException(CANNOT_CREATE_XML_PARSER, e);
            }
            xr.setContentHandler(handler);
            try {
                xr.parse(new InputSource(in));
            } catch (IOException e) {
                throw new GoException(POM_IO_EXCEPTION, e);
            } catch (SAXException e) {
                throw new GoException(POM_SAX_EXCEPTION, e);
            }
        } catch (GoException e) {
            throw e.put("artifact", artifact.toString());
        }
    }
    
    void getDependencyManagement(Artifact artifact, final Map<String, Artifact> dependencies) {
        final Properties properties = new Properties();
        properties.setProperty("project.groupId", artifact.getGroup());
        properties.setProperty("project.artifactId", artifact.getName());
        properties.setProperty("project.version", artifact.getVersion());
        getMetaData(artifact, properties, dependencies);
        final VariableProperties variables = new VariableProperties(properties);
        ContentHandler handler = new DefaultHandler() {
            int depth;
            
            boolean dependencyManagement;
            boolean deps;
            boolean capture;
            
            StringBuilder characters = new StringBuilder();
            
            String groupId;
            String artifactId;
            String version;
            String scope;
            String optional;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
                depth++;
                if (depth == 2 && localName.equals("dependencyManagement")) {
                    dependencyManagement = true;
                } else if (depth == 3 && dependencyManagement && localName.equals("dependencies")) {
                    deps = true;
                } else if (depth == 5 && deps) {
                    capture = true;
                }
            }
            
            @Override
            public void characters(char[] ch, int start, int length)
            throws SAXException {
                if (capture) {
                    characters.append(ch, start, length);
                }
            }
           
            @Override
            public void endElement(String uri, String localName, String qName)
            throws SAXException {
                if (depth == 2 && localName.equals("dependencyManagement")) {
                    dependencyManagement = false;
                } else if (depth == 3 && dependencyManagement && localName.equals("dependencies")) {
                    deps = false;
                } else if (depth == 4 && deps && localName.equals("dependency")) {
                    if (version != null && (scope == null || scope.equals("compile") || scope.equals("runtime")) && (optional == null || !"true".equals(optional))) {
                        dependencies.put(groupId + "/" + artifactId, new Artifact(groupId, artifactId, version));
                    }
                    optional = scope = version = artifactId = groupId = null;
                } else if (capture) {
                    capture = false;
                    if (localName.equals("groupId")) {
                        groupId = variables.getValue(characters.toString());
                    } else if (localName.equals("artifactId")) {
                        artifactId = variables.getValue(characters.toString());
                    } else if (localName.equals("version")) {
                        version = variables.getValue(characters.toString());
                    } else if (localName.equals("scope")) {
                        scope = variables.getValue(characters.toString());
                    } else if (localName.equals("optional")) {
                        optional = variables.getValue(characters.toString());
                    }
                    characters.setLength(0);
                }
                depth--;
            }
        };
        parse(artifact, handler);
    }
    
    public List<Artifact> getImmediateDependencies(Artifact artifact) {
        final List<Artifact> artifacts = new ArrayList<Artifact>();
        final Map<String, Artifact> dependencies = new HashMap<String, Artifact>();
        final Properties properties = new Properties();
        properties.setProperty("project.groupId", artifact.getGroup());
        properties.setProperty("project.artifactId", artifact.getName());
        properties.setProperty("project.version", artifact.getVersion());
        getMetaData(artifact, properties, dependencies);
        final VariableProperties variables = new VariableProperties(properties);
        ContentHandler handler = new DefaultHandler() {
            int depth;
            
            boolean deps;
            boolean dependency;
            
            StringBuilder characters = new StringBuilder();
            
            String groupId;
            String artifactId;
            String version;
            String scope;
            String optional;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
                depth++;
                if (depth == 2 && localName.equals("dependencies")) {
                    deps = true;
                } else if (deps && depth == 4) {
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
                    deps = false;
                } else if (deps && depth == 3) {
                    if ((scope == null || scope.equals("compile") || scope.equals("runtime")) && (optional == null || !"true".equals(optional))) {
                        String key = groupId + "/" + artifactId;
                        Artifact artifact = dependencies.get(key);
                        if (artifact == null) {
                            artifact = new Artifact(groupId, artifactId, version);
                        }
                        artifacts.add(artifact);
                    }
                    optional = scope = version = artifactId = groupId = null;
                } else if (depth == 4 && dependency) {
                    dependency = false;
                    if (localName.equals("groupId")) {
                        groupId = variables.getValue(characters.toString());
                    } else if (localName.equals("artifactId")) {
                        artifactId = variables.getValue(characters.toString());
                    } else if (localName.equals("version")) {
                        version = variables.getValue(characters.toString());
                    } else if (localName.equals("scope")) {
                        scope = variables.getValue(characters.toString());
                    } else if (localName.equals("optional")) {
                        optional = variables.getValue(characters.toString());
                    }
                    characters.setLength(0);
                }
                depth--;
            }
        };
        parse(artifact, handler);
        return artifacts;
    }
}
