package com.goodworkalan.go.go;

@Command(parent = Snap.class)
public class Hello extends Task {
    public String greeting;
    
    @Argument
    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    @Override
    public void execute(Environment env) {
        env.out.println(greeting);
    }
}