package com.goodworkalan.go.go;


public interface Commandable extends Arguable {
    // FIXME How do return an error? You don't, right? You raise an exception.
    public void execute(Environment env);
}
