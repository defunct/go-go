package com.goodworkalan.go.go.commands;

import java.util.ArrayList;
import java.util.List;

import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.library.ArtifactPart;
import com.goodworkalan.go.go.library.Include;
import com.goodworkalan.go.go.library.ResolutionPart;
import com.goodworkalan.ilk.Ilk;

// TODO Document.
@Command(parent = BootCommand.class)
public class InstallCommand implements Commandable {
    // TODO Document.
    public void execute(Environment env) {
        List<ArtifactPart> artifactParts = new ArrayList<ArtifactPart>();
        if (!env.remaining.isEmpty()) {
            for (String artifact : env.remaining) {
                ArtifactPart versioned = env.library.getArtifactPart(new Include(artifact), "dep", "jar");
                if (versioned != null) {
                    artifactParts.add(versioned);
                    env.extendClassPath(new ResolutionPart(versioned.getArtifact()));
                }
            }
            env.output(new Ilk<List<ArtifactPart>>() {}, artifactParts);
            env.invokeAfter(WriteInstallCommand.class);
        }
    }
}
