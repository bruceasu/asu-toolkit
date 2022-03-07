package me.asu.net.cmd_server.command;

import me.asu.net.cmd_server.TelnetCommandHandler;
import me.asu.net.cmd_server.TelnetWorker;
import java.nio.charset.Charset;

public class InputCharsetCommand implements TelnetCommandHandler {

    public static final String NAME = "input-charset";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String help() {
        return NAME + "用法: " + NAME + " <charset>";
    }

    @Override
    public String shortDescription() {
        return "设置输入编码。";
    }

    @Override
    public String handle(TelnetWorker worker, String... args) {
        if (args == null || args.length == 0) {
            return help();
        }
        String charset = args[0];
        try {
            Charset.forName(charset);
            worker.setInputCharset(charset);
            return "设置输入编码为" + charset;
        } catch (Exception e) {
            return "Not support this charset: " + charset;
        }
    }
}
