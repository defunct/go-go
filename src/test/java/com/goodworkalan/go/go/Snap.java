package com.goodworkalan.go.go;

public class Snap implements Commandable {
    private boolean verbose;
    
    @Argument
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public boolean isVerbose() {
        return verbose;
    }
    
    public void execute(Environment env) {
    }
}
