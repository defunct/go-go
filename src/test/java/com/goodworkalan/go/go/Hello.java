package com.goodworkalan.go.go;

public class Hello extends Task {
    public Snap snap;

    public String greeting;
    
    @Argument
    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
    
    @Argument
    public void setSnap(Snap snap) {
        this.snap = snap;
    }

    @Override
    public void execute(Environment env) {
        if (snap.isVerbose()) {
            env.err.println("About to greet with greeting: " + greeting);
        }
        env.out.println(greeting);
        if (snap.isVerbose()) {
            env.err.println("Greeting emitted: " + greeting);
        }
    }
}