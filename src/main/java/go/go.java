package go;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class go implements Runnable, UncaughtExceptionHandler {
    public final List<File> libraries;

    public final String[] arguments;
    
    public int exit;
    
    public go(List<File> libraries, String[] arguments) {
        this.libraries = libraries;
        this.arguments = arguments;
    }
    
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
            File directory = new File(tokenizer.nextToken()).getAbsoluteFile();
            if (directory.isDirectory()) {
                libraries.add(directory);
            }
        }

        return libraries;
    }

    /**
     * Executes a Jav-a-Go-Go application using the given arguments.
     * 
     * @param arguments
     *            The command line arguments.
     * @throws Exception
     *             For any exception.
     */
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
        String[][] artifacts = new String[][] {
                new String[] { "go-go", "0.1.4.6" },
                new String[] { "danger", "0.1" },
                new String[] { "verbiage", "0.1.0.1" },
                new String[] { "infuse", "0.1" },
                new String[] { "retry", "0.1" },
                new String[] { "class-boxer", "0.1" },
                new String[] { "class-association", "0.1" },
                new String[] { "reflective", "0.1" }
        };
        
        List<URL> urls = new ArrayList<URL>();
        List<String> missing = new ArrayList<String>();

        // Locate them and create a list of URIs.
        DEPENDENCY: for (String[] artifact : artifacts) {
            String dep = "com/github/bigeasy/" + artifact[0] + "/" + artifact[0] + "/" + artifact[1] + "/" + artifact[0] + "-" + artifact[1] + ".jar";
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

        go goGo = new go(libraries, arguments);
        
        Thread thread = new Thread(goGo, "Jav-a-Go-Go-Boot");
        
        // Now we can build a new class loader and assign it to the current
        // thread.
        URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
        thread.setUncaughtExceptionHandler(goGo);
        thread.setContextClassLoader(classLoader);
        thread.start();
        for (;;) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                continue;
            }
        }
        if (goGo.exit != 0) {
            System.exit(goGo.exit);
        }
    }
    
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
    }

    public void run() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        // From that class loader we fetch the next main method to call.
        Class<?> ciClass;
        try {
            ciClass = classLoader.loadClass("com.goodworkalan.go.go.Go");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        // Execute that method and our work here is done.
        try {
            exit = (Integer) ciClass.getMethod("execute", List.class, new String[0].getClass()).invoke(null, libraries, arguments);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}

/* vim: set tw=80 et:  */
