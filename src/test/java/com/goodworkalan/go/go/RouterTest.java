package com.goodworkalan.go.go;

public class RouterTest {
    public final static class TestRouter implements Router {
        public void route(Connector connector) {
            connector
                .connect("mix")
                    .connect("compile", Compile.class).end()
                    .connect("test", JUnit.class).end()
                    .end()
                .connect("send")
                    .connect("publish", Publish.class).end()
                    .end();
        }
    }
    
    public void route() {
        CommandInterpreter ci = new CommandInterpreter();
    }
}
