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

    public void execute(String[][] remaining, String[] aruguments) {
        if (snap.isVerbose()) {
            System.err.println("About to greet with greeting: " + greeting);
        }
        System.out.println(greeting);
        if (snap.isVerbose()) {
            System.err.println("Greeting emitted: " + greeting);
        }
    }
}