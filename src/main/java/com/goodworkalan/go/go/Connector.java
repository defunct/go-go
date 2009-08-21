package com.goodworkalan.go.go;

/**
 * A connector defines the connections between a command line argument and a
 * task.
 * 
 * @author Alan Gutierrez
 */
public class Connector {
    /**
     * Begin an connect statement for the given command name.
     * 
     * @param name
     *            The command name.
     * @return A connection element to specific the connection properties.
     */
    public Connection<Connector> connect(String name) {
        return new Connection<Connector>();
    }

}
