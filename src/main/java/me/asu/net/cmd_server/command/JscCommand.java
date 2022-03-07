package me.asu.net.cmd_server.command;

import java.io.*;
import java.net.URL;
import javax.script.Bindings;
import me.asu.net.cmd_server.TelnetCommandHandler;
import me.asu.net.cmd_server.TelnetWorker;
import me.asu.script.JsUtils;

/**
 * JscCommand.
 * 运行javascript脚本命令。
 * jsc command [var1 var2 ...]
 */
public class JscCommand implements TelnetCommandHandler {

    public static final String NAME = "jsc";

    private JsUtils jsUtils = new JsUtils();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String help() {
        return NAME + " usage: " + NAME + " js [var1, var2, ... ]";
    }

    @Override
    public String shortDescription() {
        return "eval javascript.";
    }

    @Override
    public String handle(TelnetWorker worker, String... args) {
        if (args != null && args.length > 0) {
            String js = args[0];
            Bindings bindings = jsUtils.createBindings();
            bindings.put("worker", worker);
            if (args.length > 1) {
                String[] jsArgs = new String[args.length - 1];
                System.arraycopy(args, 1, jsArgs, 0, jsArgs.length);
                bindings.put("args", jsArgs);
                bindings.put("argc", jsArgs.length);
            } else {
                bindings.put("args", new String[0]);
                bindings.put("argc", 0);
            }
            try {
                String script = loadJs(js);
                if (script == null) {
                    return "no such script";
                }
                return String.valueOf(jsUtils.eval(script, bindings));
            } catch (Exception e) {
                return e.getMessage();
            }
        } else {
            return help();
        }
    }


    private String loadJs(String js) throws IOException {
        String path = "js/" + js + ".js";
        URL resource = JsUtils.class.getClassLoader().getResource(path);
        InputStream resourceAsStream;
        if (resource.getProtocol().equals("file")) {
            System.out.println(resource.getFile());
            resourceAsStream = new FileInputStream(resource.getFile());
        } else {
            resourceAsStream = JsUtils.class.getClassLoader().getResourceAsStream(path);
        }
        if (resourceAsStream == null) {
            return null;
        }
        int available = resourceAsStream.available();
        byte[] b = new byte[available];
        resourceAsStream.read(b);
        return new String(b, "utf-8");
    }
}
