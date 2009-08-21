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
                    .end()
                .connect("say")
                    .connect("hello", Hello.class).end()
                    .end();
        }
    }
    
    public void route() {
//        CommandInterpreter ci = new CommandInterpreter("com.goodworkalan.go.go");
        CommandLine commandLine = new CommandLine();
        Command gogo = new Command("go.go");
        gogo.add("configure", "clap://thread/com/goodworkalan/go/go/api/router-test.go");
        commandLine.add(gogo);
        commandLine.add(new Command("mix"));
        commandLine.add(new Command("compile"));
//        ci.main(commandLine);
    }
}
