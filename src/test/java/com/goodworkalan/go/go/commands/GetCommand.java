package com.goodworkalan.go.go.commands;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;

// TODO Document.
@Command(parent = Snap.class)
public class GetCommand implements Commandable {
    // TODO Document.
    public void execute(Environment env) {
        assertEquals(env.get(String.class, 0), "Snap was here!");
        assertNull(env.get(Integer.class, 0));
    }
}
