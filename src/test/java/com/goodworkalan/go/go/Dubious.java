package com.goodworkalan.go.go;

public class Dubious implements Commandable {
    public void setSomething(String string) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    private void setPrivate(String integer) {
    }

    public void execute(Environment env) {
    }
}
