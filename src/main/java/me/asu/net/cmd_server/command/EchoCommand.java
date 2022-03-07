package me.asu.net.cmd_server.command;


import me.asu.net.cmd_server.TelnetCommandHandler;
import me.asu.net.cmd_server.TelnetWorker;

public class EchoCommand implements TelnetCommandHandler {

    public static final String NAME = "echo";


    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String help() {
        return NAME + "用法：" + NAME;
    }

    @Override
    public String shortDescription() {
        return "回显。";
    }

    @Override
    public String handle(TelnetWorker worker, String... args) {
        if (args != null && args.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (String s : args) {
                builder.append(s).append(' ');
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
            return builder.toString();
        } else {
            return "";
        }
    }
}
