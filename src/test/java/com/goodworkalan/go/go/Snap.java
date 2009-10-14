package com.goodworkalan.go.go;

public class Snap extends Task {
    private boolean verbose;
    
    @Argument
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public boolean isVerbose() {
        return verbose;
    }
}
