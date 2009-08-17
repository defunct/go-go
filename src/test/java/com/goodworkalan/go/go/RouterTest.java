package com.goodworkalan.go.go;

import org.testng.annotations.Test;

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
    
    @Test
    public void route() {
        CommandInterpreter ci = new CommandInterpreter();
        Command gogo = new Command("go.go");
        gogo.addParameter("configure", "clap://thread/com/goodworkalan/go/go/api/router-test.go");
        gogo.addCommand(new Command("mix"));
        gogo.addCommand(new Command("compile"));
        ci.main(gogo.toArguments());
    }
}
