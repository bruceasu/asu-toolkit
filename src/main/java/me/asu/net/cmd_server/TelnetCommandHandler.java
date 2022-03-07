package me.asu.net.cmd_server;


public interface TelnetCommandHandler {

    /**
     * 命令.
     *
     * @return 命令
     */
    String name();

    /**
     * 简短描述.
     *
     * @return 简短描述
     */
    String shortDescription();

    /**
     * 命令帮助.
     *
     * @return 命令帮助
     */
    String help();

    /**
     * 命令处理.
     *
     * @param worker TelnetWorker
     * @param args   参数
     * @return 结果，通常用于返回终端显示。
     */
    String handle(TelnetWorker worker, String... args);
}
