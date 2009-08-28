package com.goodworkalan.go.go;

import static org.testng.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.testng.annotations.Test;
import org.xml.sax.helpers.DefaultHandler;

public class POMReaderTest {
    @Test
    public void noProperty() {
        POMReader pom = new POMReader(new File("src/test/resources/repository"));
        Properties properties = new Properties();
        pom.getMetaData(new Artifact("example", "example", "1.0.0"), properties, new HashMap<String, Artifact>());
    }
    
    @Test
    public void getDepdencyManagement() {
        POMReader pom = new POMReader(new File("src/test/resources/repository"));
        Map<String, Artifact> map = new HashMap<String, Artifact>();
        pom.getDependencyManagement(new Artifact("example", "example-parent", "1.0.0"),  map);
        assertEquals(map.get("test/optional-false").toString(), "test/optional-false/1.0.0");
        assertEquals(map.get("test/scope-compile").toString(), "test/scope-compile/1.0.0");
        assertEquals(map.get("test/scope-runtime").toString(), "test/scope-runtime/1.0.0");
    }
    
    @Test
    public void getImmedateDependencies() {
        POMReader pom = new POMReader(new File("src/test/resources/repository"));
        List<Artifact> artifacts = pom.getImmediateDependencies(new Artifact("example", "example", "1.0.0"));
        Set<String> names = new HashSet<String>();
        for (Artifact artifact : artifacts) {
            names.add(artifact.toString());
        }
        assertEquals(names.size(), 3);
        assertTrue(names.contains("test/scope-compile/1.0.0"));
        assertTrue(names.contains("test/scope-runtime/1.0.0"));
        assertTrue(names.contains("test/optional-false/1.0.0"));
    }
    
    @Test
    public void cannotCreateReader() {
        String driverClassName = System.getProperty("org.xml.sax.driver");
        System.setProperty("org.xml.sax.driver", "java.lang.String");
        final POMReader pom = new POMReader(new File("src/test/resources/repository"));
        new GoExceptionCatcher(GoException.CANNOT_CREATE_XML_PARSER, new Runnable() {
            public void run() {
                pom.getMetaData(new Artifact("example", "example", "1.0.0"), new Properties(), new HashMap<String, Artifact>());
            }
        }).run();
        if (driverClassName == null) {
            System.clearProperty("org.xml.sax.driver");
        } else { 
            System.setProperty("org.xml.sax.driver", driverClassName);
        }
    }
    
    @Test
    public void pomIOException() {
        final File dir = new File("src/test/resources/repository");
        final POMReader pom = new POMReader(dir);
        new GoExceptionCatcher(GoException.POM_IO_EXCEPTION, new Runnable() {
            public void run() {
                Artifact artifact = new Artifact("example", "example", "1.0.0");
                try {
                    pom.parse(artifact, new DefaultHandler(), new FileInputStream(new File(dir, artifact.getPath("", "pom"))) {
                        @Override
                        public synchronized int read(byte[] b, int off, int len) throws IOException {
                            throw new IOException();
                        }
                    });
                } catch (FileNotFoundException e) {
                }
            }
        }).run();
    }
    
    
    @Test
    public void saxException() {
        final POMReader pom = new POMReader(new File("src/test/resources/repository"));
        new GoExceptionCatcher(GoException.POM_SAX_EXCEPTION, new Runnable() {
            public void run() {
                pom.getImmediateDependencies(new Artifact("example", "example-error", "1.0.0"));
            }
        }).run();
    }
    
    
    @Test
    public void fileNotFound() {
        final POMReader pom = new POMReader(new File("src/test/resources/repository"));
        new GoExceptionCatcher(GoException.POM_FILE_NOT_FOUND, new Runnable() {
            public void run() {
                pom.getImmediateDependencies(new Artifact("example", "example-not-found", "1.0.0"));
            }
        }).run();
    }
}
