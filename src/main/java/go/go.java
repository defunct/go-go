package go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class go {
    public static List<File> getLibraries() {
        // We want to go quick and do nothing fancy. Find the those important
        // jars in a managed repository, then hand it off to the full monty.
        
        // We'll be using an environment variable JAV_A_GO_GO_PATH to specify
        // our search path.
        String path = System.getenv().get("JAV_A_GO_GO_PATH");
        
        // If we don't have a path in the environment, then we'll set it to use
        // the repository of the current user.
        if (path == null) {
            String home = System.getProperty("user.home");
            if (home == null) {
                throw new RuntimeException("Cannot determine home directory.");
            }
            path = home + File.separator + ".m2" + File.separator + "repository";
        }
        
        // Turn the path into a list.
        List<File> libraries = new ArrayList<File>();
        StringTokenizer tokenizer = new StringTokenizer(path, File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            File directory = new File(tokenizer.nextToken());
            if (directory.isDirectory()) {
                libraries.add(directory);
            }
        }

        return libraries;
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length == 0) {
            System.out.println("Try, try again.");
            System.exit(1);
        }

        List<File> libraries = getLibraries();
        
        if (libraries.isEmpty()) {
            throw new RuntimeException("No viable libaries in Jav-a-Go-Go path.");
        }

        // Here's a list of the bootstrap dependencies for Jav-a-Go-Go.
        String[] dependencies = new String[] {
            "com/goodworkalan/go-go/0.1.3/go-go-0.1.3.jar",
            "com/goodworkalan/reflective/0.1/reflective-0.1.jar"
        };
        
        List<URL> urls = new ArrayList<URL>();
        List<String> missing = new ArrayList<String>();

        // Locate them and create a list of URIs.
        DEPENDENCY: for (String dep : dependencies) {
            for (File directory : libraries) {
                File jar = new File(directory, dep);
                if (jar.exists() && jar.canRead()) {
                    try {
                        urls.add(new URL("jar:" + jar.toURI().toURL().toExternalForm() + "!/"));
                    } catch (MalformedURLException e) {
                        // Happens never.
                    }
                    continue DEPENDENCY;
                }
            }
            missing.add(dep);
        }

        // If we're missing any of our requirements, then let's panic.
        if (!missing.isEmpty()) {
            throw new RuntimeException(missing.toString());
        }

        // Now we can build a new class loader and assign it to the current
        // thread.
        URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);

        // From that class loader we fetch the next main method to call.
        Class<?> ciClass = classLoader.loadClass("com.goodworkalan.go.go.CommandInterpreter");

        // Execute that method and our work here is done.
        ciClass.getMethod("main", List.class, new String[0].getClass()).invoke(null, new Object[] { libraries, arguments });
    }
}

/* vim: set tw=80 et:  */
