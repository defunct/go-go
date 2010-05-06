package com.goodworkalan.go.go.commands;

import java.io.File;

import com.goodworkalan.comfort.io.Files;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.library.DirectoryPart;

public class ExtendInvokeCommand implements Commandable {
    public void execute(Environment env) {
        env.extendClassPath(new DirectoryPart(Files.file(new File("."), "src", "main", "java").getAbsoluteFile()));
        env.invokeAfter(ExtendCommand.class);
    }
}
