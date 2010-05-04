package com.goodworkalan.go.go.commands;

import com.goodworkalan.go.go.Argument;
import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.GoError;
import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.go.go.library.ArtifactPart;
import com.goodworkalan.go.go.library.Include;
import com.goodworkalan.go.go.library.ResolutionPart;

@Command(parent = BootCommand.class)
public class InstallCommand implements Commandable {
    /** The artifact to install. */
    @Argument
    public Artifact artifact;
    
    public void execute(Environment env) {
        if (artifact == null) {
            throw new GoError('a', 0);
        }
        ArtifactPart versioned = env.library.getArtifactPart(new Include(artifact), "dep", "jar");
        if (versioned != null) {
            env.extendClassPath(new ResolutionPart(versioned.getArtifact()));
            env.invokeAfter(new WriteInstallCommand(versioned.getArtifact()));
        }
    }
}
