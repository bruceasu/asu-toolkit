package me.asu.net.cmd_server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import lombok.Getter;
import lombok.Setter;
import me.asu.lang.unsafe.UnsafeReferenceFieldUpdater;
import me.asu.lang.unsafe.UnsafeUpdater;
import me.asu.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TelnetWorker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelnetWorker.class);

    private static final
    UnsafeReferenceFieldUpdater<BufferedReader, Reader> READER_UPDATER =
            UnsafeUpdater.newReferenceFieldUpdater(BufferedReader.class, "in");

    private static final
    UnsafeReferenceFieldUpdater<PrintWriter, Writer> WRITER_UPDATER =
            UnsafeUpdater.newReferenceFieldUpdater(PrintWriter.class, "out");

    private final Socket socket;

    @Getter
    @Setter
    boolean running = false;

    BufferedReader reader;

    PrintWriter out;

    @Getter
    @Setter
    private volatile boolean login = false;

    @Getter
    private String charset = "utf-8";

    @Getter
    private String inputCharset = "utf-8";

    /**
     * @param socket
     */
    public TelnetWorker(final Socket socket) throws IOException {
        this.socket = socket;
        reader      = new BufferedReader(new InputStreamReader(socket.getInputStream(), inputCharset));
        out         = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), charset), true);

        // display welcome screen
        out.println(buildWelcomeScreen());
        prompt();
    }

    public void setCharset(String charset) {
        try {
            Charset.forName(charset);
            this.charset = charset;
            out.flush();
            WRITER_UPDATER.set(out, new OutputStreamWriter(socket.getOutputStream(), charset));
        } catch (Exception e) {
            out.write("Not support this charset: " + charset);
        }
    }

    public void setInputCharset(String charset) {
        try {
            Charset.forName(charset);
            this.inputCharset = charset;
            READER_UPDATER
                    .set(reader, new InputStreamReader(socket.getInputStream(), inputCharset));
        } catch (Exception e) {
            out.write("Not support this charset: " + charset);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        running = true;
        try {
            while (running) {
                final String command = reader.readLine();
                if (command == null || Strings.isBlank(command)) {
                    prompt();
                    continue;
                }

                // handle the command
                String response = CommandDispatcher.getInstance()
                                                   .dispatch(this, command);
                if (Strings.isNotBlank(response)) {
                    out.println(response);
                }
                prompt();
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }
    }

    public void printf(String fmt, Object... args) throws IOException {
        if (args == null || args.length == 0) {
            out.printf(fmt);
        } else {
            out.printf(fmt, args);
        }
    }

    public String prompt(String message) throws IOException {
        if (Strings.isNotBlank(message)) {
            out.printf("%s ", message);
            out.flush();
        }
        return reader.readLine();
    }

    private void prompt() {
        out.print("$> ");
        out.flush();
    }

    /**
     * @return welcome screen
     */
    private String buildWelcomeScreen() {
        String cr =
                System.getProperty("os.name").matches("(W|w)indows.*") ? "\r\n"
                        : "\n";
        StringBuilder builder = new StringBuilder();
        builder.append(cr);
        builder.append("======================================================");
        builder.append(cr);
        builder.append(cr);
        builder.append("            Welcome to Telnet Admin:                  ");
        builder.append(cr);
        builder.append(cr);
        builder.append("======================================================");
        builder.append(cr);
        builder.append(cr);
        builder.append("help   : print the help message");
        builder.append(cr);
        builder.append(
                "charset: set your terminal charset for CJK characters for display, default is "
                        + charset + ".");
        builder.append(cr);
        builder.append(
                "input-charset: set your terminal charset for CJK characters for input, default is "
                        + charset + ".");
        builder.append(cr);
        builder.append("exit   : quit this programme");
        builder.append(cr);
        builder.append(cr);
        return builder.toString();
    }

}
