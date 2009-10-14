package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Bundle {
    private final ArrayList<Include> includes = new ArrayList<Include>();
    
    public Bundle() {
    }

    public void required(Artifact artifact, Artifact...exclude) {
        includes.add(new Include(false, artifact, exclude));
    }
    
    public void optional(Artifact artifact, Artifact...exclude) {
        includes.add(new Include(true, artifact, exclude));
    }
    
    public List<Include> getIncludes() {
        return Collections.unmodifiableList(includes);
    }
}
