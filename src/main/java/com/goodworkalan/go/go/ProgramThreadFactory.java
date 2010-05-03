package com.goodworkalan.go.go;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.goodworkalan.go.go.library.PathPart;
import com.goodworkalan.go.go.library.PathParts;

/**
 * A thread factory that builds a custom the class loader that will search for
 * classes in an extended class path specified by the next path part collection
 * in a path parts queue.
 * 
 * @author alan
 * 
 */
class ProgramThreadFactory implements ThreadFactory {
    /**
     * A queue of path part collections used to build child class loaders that
     * search for classes in the path specified by the path part collection. An
     * extended class path is added to the queue, prior to executing a new
     * runnable in the thread pool. If this runnable is the root of an
     * execution, there queue is left empty and the class loader of the new
     * thread is not set.
     * <p>
     * The thread pool only runs one thread at a time, with a parent thread
     * launching a child and so on, so that this queue will only be referenced
     * by one thread at a time, when a parent thread creates a new child with an
     * extended class path.
     */
    public Queue<Collection<PathPart>> partsQueue = new LinkedList<Collection<PathPart>>();
    
    /** The thread count. */
    private final AtomicInteger count = new AtomicInteger();

    /**
     * Construct a thread building a custom the class loader that will search
     * for classes in an extended class path specified by the next path part
     * collection in the path parts queue.
     * 
     * @param runnable
     *            The runnable.
     * @return The new thread.
     */
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        Collection<PathPart> parts = partsQueue.poll();
        if (parts != null) {
            Thread currentThread = Thread.currentThread();
            ClassLoader parentClassLoader = currentThread.getContextClassLoader();
            thread.setContextClassLoader(PathParts.getClassLoader(parts, parentClassLoader));
            thread.setName("Jav-a-Go-Go-Child-" + count.getAndIncrement());
        } else {
            thread.setName("Jav-a-Go-Go-Root-" + count.getAndIncrement());
        }
        return thread;
    }
}
