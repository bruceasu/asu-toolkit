package me.asu.net.cmd_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import me.asu.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TelnetServer implements Runnable {

    private static final Logger          LOGGER       = LoggerFactory.getLogger(TelnetServer.class);
    private static final int             DEFAULT_PORT = 5354;
    private final        int             port;
    private              ServerSocket    server       = null;
    private volatile     boolean         running      = false;
    private              ExecutorService executor     =
            new ThreadPoolExecutor(1, 64, 0L, TimeUnit.MILLISECONDS,
                    new SynchronousQueue<Runnable>(), new NamedThreadFactory("admin-console-session"));

    public TelnetServer(int port) {
        this.port = (port <= 0 || port > 65535) ? DEFAULT_PORT : port;
    }

    public TelnetServer(int port, int maxPoolSize) {
        this(port);
        if (maxPoolSize > 1) {
            if (executor != null) {
                executor.shutdownNow();
            }
            executor = new ThreadPoolExecutor(1, maxPoolSize, 0L, TimeUnit.MILLISECONDS,
                    new SynchronousQueue<Runnable>(),
                    new NamedThreadFactory("admin-console-session"));
        }
    }

    public static void main(String[] args) {
        TelnetServer server = new TelnetServer(0);
        server.run();
    }

    /**
     * The main method to start the telnet server
     */
    @Override
    public void run() {
        try {
            // establish a connection
            server = new ServerSocket(this.port);
            LOGGER.info("Server running and listening on port : " + this.port);
            running = true;
            while (running) {
                Socket s = server.accept();
                executor.execute(new TelnetWorker(s));
            }

        } catch (Exception e) {
            LOGGER.warn("Shutting down the server..");
        } finally {
            executor.shutdown();
        }

    }

    /**
     * Checks if the server is running.
     */
    public boolean isRunning() {
        return running && !server.isClosed();
    }

    /**
     * Shutdowns all the connection and the server
     */
    public void shutdown() throws IOException {
        running = false;
        if (server != null) {
            server.close();
        }
    }
}
