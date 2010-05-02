package com.goodworkalan.go.go;

@Command(parent = Snap.class)
public class WatusiCommand implements Commandable {
    private String repeat;

    @Argument
    public void addRepeat(String repeat) {
        this.repeat = repeat;
    }

    public void execute(Environment env) {
        env.output(repeat);
    }
}
