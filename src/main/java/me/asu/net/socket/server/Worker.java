package me.asu.net.socket.server;

import static me.asu.net.NetConstants.ERROR_CLOSED;
import static me.asu.net.NetConstants.ERROR_RECV;
import static me.asu.net.NetConstants.ERROR_SEND;
import static me.asu.net.NetConstants.ERROR_TIMEOUT;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.asu.lang.unsafe.UnsafeReferenceFieldUpdater;
import me.asu.lang.unsafe.UnsafeUpdater;
import me.asu.net.message.IMessage;
import me.asu.net.message.ProtocolMsg;
import me.asu.net.util.Stream;
import me.asu.util.Streams;

/**
 * 
 */
@Slf4j
@Getter
public class Worker implements Runnable {

    private static final
    RuntimeException NOT_SUPPORT_TYPE = new RuntimeException("not support data type");

    private static final
    UnsafeReferenceFieldUpdater<BufferedReader, Reader> READER_UPDATER =
            UnsafeUpdater.newReferenceFieldUpdater(BufferedReader.class, "in");

    private static final
    UnsafeReferenceFieldUpdater<PrintWriter, Writer> WRITER_UPDATER =
            UnsafeUpdater.newReferenceFieldUpdater(PrintWriter.class, "out");

    private final Socket socket;

    @Setter
    volatile boolean running = false;

    SendThread sendThread;
    ReceiveThread receiveThread;

    @Setter
    Handler handler;

    Stream stream;

    ChannelContext ctx;

    int readTimeout = 0;

    private BlockingDeque<SendMessage> sendingQueue = new LinkedBlockingDeque<SendMessage>();

    /**
     * @param socket
     * @param handler
     */
    public Worker(final Socket socket, Handler handler, int readTimeout) {
        this.socket      = socket;
        this.readTimeout = readTimeout;
        this.stream      = new Stream(socket);
        this.stream.setReadTimeout(this.readTimeout);
        this.ctx     = new ChannelContext(this);
        this.handler = handler;
        if (handler != null) {
            handler.onOpen(this.ctx);
        }
    }

    public void addSendData(Object data) {
        sendingQueue.add(new SendMessage(data, null));
    }

    public void addSendData(Object data, SendCallBack callBack) {
        sendingQueue.add(new SendMessage(data, callBack));
    }

    public void shutdown() {
        log.info("worker for {} is shutting down...", ctx.getAddress());
        this.running = false;
        if (stream != null && !stream.isClosed()) {
            Streams.safeClose(stream);
            stream = null;
        }
        if (handler != null) {
            handler.onClose(this.ctx);
        }
        if (receiveThread != null) receiveThread.cancel();
        if (sendThread != null) sendThread.cancel();
        log.info("worker for {} is shutdown.", ctx.getAddress());
    }


    @Override
    public void run() {
        running = true;
        receiveThread = new ReceiveThread();
        receiveThread.start();
        sendThread = new SendThread();
        sendThread.start();
        while(running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void delegateException(Throwable e, int errorCode) {
        if (handler != null) {
            handler.onError(errorCode, e, getCtx());
        }
    }

    class SendThread extends Thread {
        public void run() {
            try {
                while (running) {
                    SendMessage m = sendingQueue.take();
                    try {
                        if (m.getData() instanceof IMessage) {
                            stream.write((IMessage) m.getData());
                            if (m.getCallBack() != null) {
                                m.getCallBack().onComplete(true);
                            }
                        } else if (m.getData() instanceof byte[]) {
                            stream.write((byte[]) m.getData());
                            if (m.getCallBack() != null) {
                                m.getCallBack().onComplete(true);
                            }
                        } else {

                            delegateException(NOT_SUPPORT_TYPE, ERROR_SEND);
                        }
                    } catch (IOException ex) {
                        if (m.getCallBack() != null) {
                            m.getCallBack().onError(ex);
                        }
                        delegateException(ex, ERROR_SEND);
                    } catch (IllegalStateException ex) {
                        delegateException(ex, ERROR_SEND);
                        shutdown();
                    }
                }
            } catch (Exception e) {
                onError(e);
            }
        }
        public void cancel() {
            if(!interrupted()) interrupt();
        }
        private void onError(Exception e) {
            log.error("", e);
            delegateException(e, ERROR_SEND);
            shutdown();
        }
    }
    class ReceiveThread extends Thread {
        int nThreads = Runtime.getRuntime().availableProcessors() * 10;
        ExecutorService dispatchPool=  new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        @Override
        public void run() {
            while(running) {
                try {
                    IMessage box;
                    if (Worker.this.handler != null) {
                        box = handler.createMessage();
                    } else {
                        box = new ProtocolMsg();
                    }
                    if (Worker.this.stream.read(box)) {
                        dispatchPool.execute(()->{
                            Worker.this.handler.onRecv(box, getCtx());
                        });
                    } else {
                        IllegalStateException e =
                                new IllegalStateException("A error occurred when receiving data.");
                        onError(e);
                        break;
                    }
                } catch (Exception e) {
                    onError(e);
                }
            }
        }
        private void onError(Exception e) {
            log.error("", e);
            if (e instanceof TimeoutException) {
                delegateException(e, ERROR_TIMEOUT);
            } else if (e instanceof IllegalStateException) {
                delegateException(e, ERROR_CLOSED);
            } else if (e instanceof SocketException) {
                if ("Connection reset".equals(e.getMessage())) {
                    // 客户端断开。
                    delegateException(e, ERROR_CLOSED);
                } else {
                    delegateException(e, ERROR_RECV);
                }
            } else {
                delegateException(e, ERROR_RECV);
            }

            shutdown();
        }

        public void cancel() {
            if(!interrupted()) interrupt();
        }
    }


    @Data
    class SendMessage {
        Object       data;
        SendCallBack callBack;

        public SendMessage(Object data, SendCallBack callBack) {
            this.data     = data;
            this.callBack = callBack;
        }
    }
}
