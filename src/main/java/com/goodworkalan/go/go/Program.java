/**
 * 
 */
package com.goodworkalan.go.go;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.go.go.library.Library;
import com.goodworkalan.reflective.ReflectiveFactory;

/**
 * A Jav-a-Go-Go program.
 * 
 * @author Alan Gutierrez
 */
final class Program {
    private final List<File> libraries;
    
    private final List<String> arguments;

    private final Map<List<String>, Artifact> programs;

    public Program(List<File> libraries, Map<List<String>, Artifact> commands, List<String> arguments) {
        this.libraries = libraries;
        this.arguments = arguments;
        this.programs = commands;
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
        InputOutput io = new InputOutput();
        Executor executor = new Executor(new ReflectiveFactory(), new Library(libraries.toArray(new File[libraries.size()])), programs);
        queue.verbose(io, "start", arguments);
        long start = System.currentTimeMillis();
        int code = executor.start(io, arguments, null).code;
        queue.verbose(io, "stop",
                System.currentTimeMillis() - start,
                (double) Runtime.getRuntime().totalMemory() / 1024 / 1024,
                (double) Runtime.getRuntime().freeMemory() / 1024 / 1024);
        return code;
    }
}