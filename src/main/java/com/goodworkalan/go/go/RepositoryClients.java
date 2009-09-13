package com.goodworkalan.go.go;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

public class RepositoryClients {
    public static Map<String, RepositoryClient> load(ClassLoader classLoader) {
        ReflectiveFactory reflectiveFactory = new ReflectiveFactory();
        Map<String, RepositoryClient> map = new HashMap<String, RepositoryClient>();
        Enumeration<URL> resources;
        try {
            resources = classLoader.getResources("META-INF/services/com.goodworkalan.go.go.CommandInterpreter");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                BufferedReader lines = new BufferedReader(new InputStreamReader(url.openStream()));
                String className;
                while ((className = lines.readLine()) != null) {
                    if (className.trim().equals("")) {
                        continue;
                    }
                    Class<?> foundClass;
                    try {
                        foundClass = classLoader.loadClass(className);
                    } catch (ClassNotFoundException e) {
                        throw new GoException(0, e);
                    }
                    if (RepositoryClient.class.isAssignableFrom(foundClass)) {
                        RepositoryClient client;
                        try {
                            client = (RepositoryClient) reflectiveFactory.getConstructor(foundClass).newInstance();
                        } catch (ReflectiveException e) {
                            throw new GoException(0, e);
                        }
                        map.put(client.getTypeName(), client);
                    } else {
                        throw new GoException(0);
                    }
                }
                
            }
        } catch (IOException e) {
            throw new GoException(0, e);
        }
        return map;
    }

}
