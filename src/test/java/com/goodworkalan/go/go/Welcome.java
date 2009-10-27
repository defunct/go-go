package com.goodworkalan.go.go;

@Command(parent = Snap.class)
public class Welcome implements Commandable {
    public String greeting;
    
    @Argument
    public void addGreeting(String greeting) {
        this.greeting = greeting;
    }

    public void execute(Environment env) {
        env.io.out.println(greeting);
    }
}