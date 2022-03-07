package me.asu.net.socket.client;

import static me.asu.net.NetConstants.ERROR_SEND;

import java.util.concurrent.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.asu.net.message.IMessage;
import me.asu.net.util.Stream;
import me.asu.util.NamedThreadFactory;

/**
 * SimpleClientThreadAsync.
 * @version 1.0.0
 * @since 2017-10-11 17:51
 */
@Data
@Slf4j
public class SimpleClientThreadAsync extends SimpleClient {

    private LinkedBlockingDeque<Object> msgQueueToServer = new LinkedBlockingDeque<Object>();
    private boolean                     running          = false;
    private ExecutorService             es               = Executors
            .newFixedThreadPool(2, new NamedThreadFactory("SimpleClientThreadAsync"));

    public SimpleClientThreadAsync() {
        stream = new Stream();
    }

    public SimpleClientThreadAsync(String host, int port) {
        this();
        this.host = host;
        this.port = port;
    }

    public SimpleClientThreadAsync(Delegate delegate, String host, int port) {
        this();
        this.delegate = delegate;
        this.host = host;
        this.port = port;
    }

    /**
     * 启动异步进程
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;

        connect();
        startThreads();
    }

    /**
     * 停止异步进程
     */
    public void stop() {
        if (!running) {
            return;
        }
        running = false;

        stopThreads();
        closeConn();
    }

    /**
     * 异步发送报文。
     * @param message {@link IMessage} 报文。
     */
    public void add(IMessage message) {
        boolean succ = msgQueueToServer.offer(message);
        if (!succ) {
            onError(ERROR_SEND, message);
        }
    }

    /**
     * 异步发送报文。
     * @param message byte[] 报文。
     */
    public void add(byte[] message) {
        boolean succ = msgQueueToServer.offer(message);
        if (!succ) {
            onError(ERROR_SEND, null);
        }
    }

    private void startThreads() {
        es.submit(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    if (!isConnected()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                        continue;
                    }

                    receive();
                }
            }
        });
        es.submit(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    if (!isConnected()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                        continue;
                    }
                    Object msg = null;
                    try {
                        msg = msgQueueToServer.take();
                        if (msg == null) {
                            continue;
                        }
                    } catch (InterruptedException e) {
                        break;
                    }

                    try {
                        if (msg instanceof IMessage) {
                            IMessage box = (IMessage) msg;
                            send(box);
                        } else if (msg instanceof byte[]) {
                            byte[] data = (byte[]) msg;
                            send(data);
                        }
                    } catch (Exception e) {
                        if (msg instanceof IMessage) {
                            onError(ERROR_SEND, (IMessage) msg);
                        } else {
                            onError(ERROR_SEND, null);
                        }
                    }
                }
            }
        });
    }

    private void stopThreads() {
        es.shutdownNow();
    }

}
