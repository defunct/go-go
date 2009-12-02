/**
 * 
 */
package com.goodworkalan.go.go;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class Program {
    public File dir;
    
    public String[] arguments;
    
    public Program(File dir, String...arguments) {
        this.dir = dir;
        this.arguments = arguments;
    }

    /**
     * Split out into an object that can be reconstructed, maybe call it Fork or
     * something, or process. There is only one during the execution of the
     * program, so it can be a singleton, but really, it can be passed into the
     * CommandInterpreter, call it GrandPooBah until you refactor all the names.
     * It takes a working directory as an argument. Move all this to an object
     * called Go.
     * <p>
     * FIXME On our way to fork, but not quite right yet. Remember that Mix is
     * an interpreter, that interprets an artifacts file. We have artifacts file
     * here. What do we do? Auto-magically reuse it when we push a new program
     * into the queue? Or do we specify that as part of the program arguments,
     * artifacts file or list, Go arguments, additional arguments.
     * <p>
     * To start, maybe all the arguments can get collected into a hash, so you
     * can just pass those on, a hash of string to objects.
     * <p>
     * Then you can have either a file or a list of artifacts as the first
     * argument, or rather, a list of includes.
     * <p>
     * You can provide a read only copy of the arguments and list of includes,
     * you can provide that in the environment. That is the bootstrap, or the
     * information for the interpreter.
     * <p>
     * I'd say, that would be the least conventional, most all the parts laid
     * out on the bench way to do it. Not necessarily configurable, but you
     * can't argue with the fact that all these actors are present.
     * <p>
     * There are three layers of operations. Launch Java. Load the jars that
     * will bootstrap your application. That application can load more jars if
     * it likes. Actually, the jars are discovered during the load of the
     * interpreted file. Maybe, they shouldn't be.
     * <p>
     * Actually, your going to pull in other artifacts by the combination of the
     * local configuration file with the working directory. (Which, for mix, the
     * local configuration file should move into src/mix/resources.) Then, you
     * might pick up some artifacts while loading jars (why? when will you ever
     * use that?), and then you'll pick up more artifacts as you resolve those
     * dependencies. Then your program runs. In Mix I fiddle with the thread
     * class loader some more, to add the projects, but now I can leave those
     * there, you can create helpers, for your application, classes that perform
     * necessary tasks.
     * <p>
     * Can't you get rid of configuration by that interface? Why not just find
     * another way to add an artifact file? You don't use it. It can go. Why
     * would a jar want to ask for artifacts? Why would that be in a jar and NOT
     * in the repository. It doesn't belong in a jar at all. Feels like dead to
     * me. Like a really bad choice that will only attract flies.
     * <p>
     * Process this until the maybes are gone.
     * 
     * @param queue
     *            The program queue.
     * @return The desired exit code of program.
     */
    public int run(ProgramQueue queue) {
        LinkedList<String> args = new LinkedList<String>(Arrays.asList(arguments));
        if (args.isEmpty()) {
            throw new GoException(0);
        }
        List<Include> includes = new ArrayList<Include>();
        boolean debug = false;
        for (Iterator<String> each = args.iterator(); each.hasNext();) {
            String argument = each.next();
            if (argument.equals("--")) {
                break;
            } else if (argument.startsWith("--go:")) {
                if (argument.equals("--go:debug")) {
                    debug = true;
                } else if (argument.equals("--go:no-debug")) {
                    debug = false;
                } else if (argument.startsWith("--go:artifacts=")) {
                    File artifacts = new File(argument.substring(argument.indexOf('=') + 1));
                    if (artifacts.exists()) {
                        includes.addAll(Artifacts.read(artifacts));
                    }
                } else if (argument.startsWith("--go:define=")) {
                    String[] definition = argument.substring(argument.indexOf('=') + 1).split(":", 2);
                    if (definition.length != 2) {
                        throw new GoError(0);
                    }
                    System.out.println(definition[0] + "=" + definition[1]);
                    System.setProperty(definition[0], definition[1]);
                } else {
                    throw new GoException(0);
                }
                each.remove();
            }
        }
        File artifacts = new File(args.removeFirst());
        try {
            BufferedReader configuration = new BufferedReader(new FileReader(artifacts));
            String line;
            while ((line = configuration.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("@")) {
                    String[] argument = line.split("\\s+", 3);
                    if (argument.length == 1) {
                        throw new GoException(0);
                    }
                    if (argument[1].equals("debug")) {
                        debug = true;
                    } else if (argument[1].equals("no-debug")) {
                        debug = false;
                    } else if (argument[1].equals("artifacts")) {
                        File additional = new File(argument[2]);
                        if (additional.exists()) {
                            includes.addAll(Artifacts.read(additional));
                        }
                    } else {
                        throw new GoException(0);
                    }
                }
            }
            configuration.close();
        } catch (IOException e) {
            throw new GoException(0, e);
        }
        includes.addAll(Artifacts.read(artifacts));
        CommandInterpreter ci = new CommandInterpreter(includes);
        try {
            String name = artifacts.getName().toLowerCase();
            if (name.endsWith(".bat")) {
                name = name.substring(0, name.length() - 4);
            }
            ci.command(artifacts.getName());
            args.addFirst(artifacts.getName());
        } catch (GoException e) {
        }
        if (debug) {
            System.out.println(args);
        }
        int code = ci.execute(new InputOutput(), args);
        if (debug) {
            System.out.printf("%.2fM/%.2fM\n", 
                    (double) Runtime.getRuntime().totalMemory() /  1024 / 1024,
                    (double) Runtime.getRuntime().freeMemory() / 1024 / 1024);
        }
        return code;
    }
}