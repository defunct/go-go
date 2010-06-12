package com.goodworkalan.go.go;


import static org.testng.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.testng.annotations.Test;

/**
 * Unit tests for the {@link CommandNode} class.
 *
 * @author Alan Gutierrez
 */
public class CommandNodeTest {
    /** Ignore arguments that are not really arguments. */
    @Test
    public void filterArguments() {
        CommandNode commandNode = new CommandNode(new InputOutput(), NoArguments.class);
        assertEquals(commandNode.getArguments().size(), 0);
    }
    
    /** Argument duplicated. */
    @Test
    public void duplicateArgument() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputOutput io = new InputOutput(System.in, System.out, new PrintStream(out));
        new CommandNode(io, DuplicateArgumentMethod.class);
        assertEquals(out.toString(), "WARNING: Command class com.goodworkalan.go.go.DuplicateArgumentMethod has multiple definitions of argument [name].\n");
    }
    
    /** Set argument names with annotation. */
    @Test
    public void annotationNamed() {
        CommandNode commandNode = new CommandNode(new InputOutput(), AnnotationNamedArguments.class);
        assertEquals(commandNode.getArguments().size(), 2);
        assertEquals(commandNode.getArguments().get("foo"), String.class);
    }
}
