package me.asu.net.cmd_server.command;

import me.asu.net.cmd_server.TelnetCommandHandler;
import me.asu.net.cmd_server.TelnetWorker;

/**
 * ExitCommand.
 *
 * 
 * @version 1.0.0
 * @since 2017-12-20 16:26
 */
public class ExitCommand implements TelnetCommandHandler {

    public static final String NAME = "exit";
    public static final String BYE  = "Bye!";


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
        return "退出。";
    }

    @Override
    public String handle(TelnetWorker worker, String... args) {
        worker.setRunning(false);
        return BYE;
    }
}
