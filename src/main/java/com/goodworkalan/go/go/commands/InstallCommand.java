package com.goodworkalan.go.go.commands;

import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.library.ArtifactPart;
import com.goodworkalan.go.go.library.Include;
import com.goodworkalan.go.go.library.ResolutionPart;

@Command(parent = BootCommand.class)
public class InstallCommand implements Commandable {
    public void execute(Environment env) {
        if (!env.remaining.isEmpty()) {
            for (String artifact : env.remaining) {
                ArtifactPart versioned = env.library.getArtifactPart(new Include(artifact), "dep", "jar");
                if (versioned != null) {
                    env.extendClassPath(new ResolutionPart(versioned.getArtifact()));
                }
            }
            env.invokeAfter(WriteInstallCommand.class);
        }
    }
}
