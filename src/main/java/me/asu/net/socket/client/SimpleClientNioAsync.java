package me.asu.net.socket.client;

import static me.asu.net.socket.NetConstants.ERROR_SEND;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.asu.net.socket.uitl.NioStream;
import me.asu.net.message.*;

/**
 * SimpleClientNioAsync.
 *
 * @version 1.0.0
 * @since 2017-10-11 17:51
 */
@Data
@Slf4j
public class SimpleClientNioAsync {

    protected Delegate  delegate;
    protected String    host;
    protected int       port;
    protected NioStream stream;
    ExecutorService es = Executors.newSingleThreadExecutor();

    public SimpleClientNioAsync(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        SocketAddress address = new InetSocketAddress(host, port);
        SocketChannel channel = SocketChannel.open(address);
        stream = new NioStream(channel);
    }

    public SimpleClientNioAsync(Delegate delegate, String host, int port) throws IOException {
        this.delegate = delegate;
        this.host = host;
        this.port = port;
        stream.setDelegate(delegate);
        SocketAddress address = new InetSocketAddress(host, port);
        SocketChannel channel = SocketChannel.open(address);
        stream = new NioStream(channel);
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
        stream.setDelegate(delegate);
    }

    public void send(IMessage message) {
        boolean succ = stream.send(message);
        if (!succ) {
            if (delegate != null) {
                delegate.onError(ERROR_SEND, message);
            }
        }
    }

    public void start() {
        es.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    stream.listen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void stop() {
        stream.setRunning(false);
        es.shutdownNow();
    }
}
