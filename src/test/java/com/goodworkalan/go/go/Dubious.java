package com.goodworkalan.go.go;

public class Dubious extends Task {
    public void setSomething(String string) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    private void setPrivate(String integer) {
    }
}
