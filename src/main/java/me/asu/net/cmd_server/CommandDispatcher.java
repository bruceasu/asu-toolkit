package me.asu.net.cmd_server;


import java.util.*;
import lombok.Data;
import me.asu.net.cmd_server.command.*;
import me.asu.util.Strings;
import me.asu.han.TableGenerator;


public class CommandDispatcher {

    private Map<String, TelnetCommandHandler> subCommands = new HashMap<String, TelnetCommandHandler>();

    private CommandDispatcher() {
        addCommand("quit", new ExitCommand());
        addCommand(new ExitCommand());
        addCommand(new AuthCommand());
        addCommand(new HelpCommand());
        addCommand(new CharsetCommand());
        addCommand(new InputCharsetCommand());
        addCommand(new EchoCommand());
        addCommand(new JscCommand());
    }

    public static CommandDispatcher getInstance() {
        return SingletonHolder.instance;
    }

    public void addCommand(String cmd, TelnetCommandHandler handler) {
        subCommands.put(cmd, handler);
    }

    public void addCommand(TelnetCommandHandler handler) {
        subCommands.put(handler.name(), handler);
    }

    public TelnetCommandHandler getCommand(String cmd) {
        return subCommands.get(cmd);
    }

    public TelnetCommandHandler removeCommand(String cmd) {
        return subCommands.remove(cmd);
    }

    public String commandList() {
        List<String> headersList = Arrays.asList("支持的命令", "");
        List<List<String>> rowsList = new ArrayList<List<String>>();
        TableGenerator tg = new TableGenerator();

        List<String> cmds = new ArrayList<String>(subCommands.keySet());
        Collections.sort(cmds);
        for (String cmd : cmds) {
            TelnetCommandHandler handler = subCommands.get(cmd);
            rowsList.add(Arrays.asList(cmd, handler.shortDescription()));
        }
        return tg.generateTable(headersList, rowsList);
    }

    public String dispatch(TelnetWorker worker, String command) {
        CmdLine cl = CmdLine.parse(command);
        TelnetCommandHandler telnetCommandHandler = subCommands.get(cl.cmd);
        if (telnetCommandHandler == null) {
            return "unknown command: " + cl.cmd;
        }

        return telnetCommandHandler.handle(worker, cl.args);
    }

    private static class SingletonHolder {

        static CommandDispatcher instance = new CommandDispatcher();
    }

    @Data
    static class CmdLine {

        String   cmd  = "";
        String[] args = new String[0];

        public static CmdLine parse(String command) {
            CmdLine cl = new CmdLine();
            if (Strings.isBlank(command)) {
                return cl;
            }
            command = cleanup(command);
            String[] split = command.split(" ");
            cl.cmd = split[0];
            if (split.length > 1) {
                cl.args = new String[split.length - 1];
                System.arraycopy(split, 1, cl.args, 0, cl.args.length);
            }

            return cl;
        }

        private static String cleanup(String cmd) {
            cmd = Strings.trim(cmd);
            for (int i = 0; i < cmd.length(); i++) {
                if (Character.isISOControl(cmd.charAt(i))) {
                    return cmd.substring(i);
                }
            }

            return cmd;
        }
    }
}
