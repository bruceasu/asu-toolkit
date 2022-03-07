package me.asu.net.socket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.asu.util.NamedThreadFactory;
import me.asu.util.Strings;

/**
 * 用于低并发，高吞吐量场景。
 *
 * 
 * @since 2018/11/13
 */
@Data
@Slf4j
public class Server implements Runnable {

    private          int          port         = 0;
    private          int          realPort     = 0;
    private          String       host         = "0.0.0.0";
    private          ServerSocket serverSocket = null;
    private volatile boolean      running      = false;
    private ExecutorService ioExecutor;
    private ExecutorService boss;
    private Handler         handler;
    private int readTimeout = 90000;

    public Server() {
        this("0.0.0.0", 0);
    }

    public Server(int port) {
        this("0.0.0.0", port);
    }

    public Server(String host, int port) {
        if (Strings.isNotBlank(host)) {
            this.host = host.trim();
        }

        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Out of range of port (" + port + ")");
        }
        this.port = port;
        String property = System.getProperty("socket.serverSocket.pool.max", "1000");
        int maxSize = Integer.parseInt(property);
        ioExecutor = new ThreadPoolExecutor(1, maxSize, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(), new NamedThreadFactory("socket-serverSocket"));
        boss = Executors
                .newSingleThreadExecutor(new NamedThreadFactory("socket-serverSocket-boss"));
    }

    public Server withHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public int start() {
        boss.submit(this);
        while (!running) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        if (this.port == 0) {
            return this.realPort;
        } else {
            return this.port;
        }
    }

    /**
     * The main method to start the telnet serverSocket
     */
    @Override
    public void run() {
        try {
            // establish a connection
            serverSocket = new ServerSocket(this.port);
            realPort = serverSocket.getLocalPort();
            log.info("Server running and listening on port : " + realPort);
            running = true;
            while (running) {
                Socket s = serverSocket.accept();
                ioExecutor.execute(new Worker(s, handler, readTimeout));
            }
        } catch (Exception e) {
            log.warn("Shutting down the serverSocket..");
        }
    }

    /**
     * Checks if the serverSocket is running.
     */
    public boolean isRunning() {
        return running && !serverSocket.isClosed();
    }

    /**
     * Shutdowns all the connection and the serverSocket
     */
    public void shutdown() throws IOException {
        running = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (boss != null) {
            boss.shutdown();
        }
        if (ioExecutor != null) {
            ioExecutor.shutdown();
        }
    }
}
