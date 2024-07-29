package me.asu.net.socket.client;

import static me.asu.net.socket.NetConstants.ERROR_SEND;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.asu.net.message.*;
import me.asu.net.util.DirectByteBufferCleaner;

/**
 * NioClient
 *
 * @version 1.0.0
 * @since 2017-10-11 17:40
 */
@Slf4j
public class NioClient {

    private final static int READ_CHUNK_SIZE      = 4 * 1024;
    private final static int READ_BUFFER_MAX_SIZE = -1;
    protected String host;
    protected int    port;
    @Getter
    @Setter
    volatile boolean running = false;
    @Getter
    @Setter
    Delegate delegate;
    SocketChannel       channel;
    ReadThread          readThread;
    MessageParserThread messageParserThread;
    ConcurrentLinkedQueue<ByteBuffer> buffers = new ConcurrentLinkedQueue<ByteBuffer>();
    private int                   readBufferMaxSize;
    private ByteArrayOutputStream readBufferOutputStream;
    private Selector              selector;

    public NioClient(String host, int port) throws IOException {
        this(host, port, READ_CHUNK_SIZE, READ_BUFFER_MAX_SIZE);
    }

    public NioClient(String host, int port, int readChunkSize) throws IOException {
        this(host, port, readChunkSize, READ_BUFFER_MAX_SIZE);
    }

    public NioClient(String host, int port, int readChunkSize, int readBufferMaxSize)
            throws IOException {
        this.host = host;
        this.port = port;
        SocketAddress address = new InetSocketAddress(host, port);
        SocketChannel channel = SocketChannel.open(address);
        init(channel, readChunkSize, readBufferMaxSize);
    }

    private void init(SocketChannel channel, int readChunkSize, int readBufferMaxSize)
            throws IOException {
        channel.configureBlocking(false);
        this.selector = Selector.open();
        this.channel = channel;
        this.channel.register(this.selector, SelectionKey.OP_READ);
        while (!channel.finishConnect()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        onConnOpen(channel);

        this.readBufferMaxSize = readBufferMaxSize;
        readChunkSize = readChunkSize > 0 ? readChunkSize : READ_CHUNK_SIZE;
        readBufferOutputStream = new ByteArrayOutputStream(readChunkSize << 1);

        running = true;
    }


    public void send(IMessage message) {
        if (!channel.isConnected()) {
            try {
                reconnect();
            } catch (IOException e) {
                e.printStackTrace();
                onError(ERROR_SEND, message);
                return;
            }
        }
        try {
            ByteBuffer wrap = ByteBuffer.wrap(message.pack());
            do {
                channel.write(wrap);
            } while (wrap.hasRemaining());
            if (delegate != null) {
                delegate.onSend(message);
            }
        } catch (Exception e) {
            if (!(e instanceof InterruptedException)) {
                log.error("", e);
            }
            onError(ERROR_SEND, message);
        }
    }

    public void start() {
        readThread = new ReadThread();
        readThread.start();
        messageParserThread = new MessageParserThread();
        messageParserThread.start();
    }

    public void stop() {
        setRunning(false);
        if (readThread != null) {
            readThread.interrupt();
        }
        if (messageParserThread != null) {
            messageParserThread.interrupt();
        }
    }

    private void reconnect() throws IOException {
        SocketAddress address = new InetSocketAddress(host, port);
        channel.connect(address);
        channel.configureBlocking(false);
        channel.register(this.selector, SelectionKey.OP_READ);
        while (!channel.finishConnect()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // ignore;
            }
        }
        onConnOpen(channel);
    }

    protected void onError(int code, IMessage ibox) {
        if (delegate != null) {
            delegate.onError(code, ibox);
        }
    }

    protected void onConnClose(SocketChannel channel) {
        if (delegate != null) {
            try {
                InetSocketAddress address = (InetSocketAddress) channel.getLocalAddress();
                delegate.onClose(address.getHostName(), address.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onConnOpen(SocketChannel channel) {
        if (delegate != null) {
            try {
                InetSocketAddress address = (InetSocketAddress) channel.getLocalAddress();
                delegate.onOpen(address.getHostName(), address.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onSendMsgToServer(IMessage ibox) {
        delegate.onSend(ibox);
    }

    protected void onRecvMsgFromServer(IMessage ibox) {
        if (delegate != null) {
            delegate.onRecv(ibox);
        }
    }

    class ReadThread extends Thread {

        @Override
        public void run() {

            while (running) {
                try {
                    selector.select();
                    Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
                    while (ite.hasNext()) {
                        SelectionKey key = ite.next();
                        ite.remove();
                        if (key.isReadable()) {
                            do {
                                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(512);
                                SocketChannel channel = (SocketChannel) key.channel();
                                int read = channel.read(byteBuffer);
                                if (read == -1) {
                                    channel.close();
                                    reconnect();
                                    break;
                                } else if (read == 0) {
                                    DirectByteBufferCleaner.clean(byteBuffer);
                                    break;
                                } else if (read == 512) {
                                    byteBuffer.flip();
                                    buffers.add(byteBuffer);
                                } else {
                                    byteBuffer.flip();
                                    buffers.add(byteBuffer);
                                    break;
                                }
                            } while (true);
                            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                        }

                    }
                } catch (CancelledKeyException e) {
                    log.error("", e);
                    break;
                } catch (IOException e) {
                    log.error("", e);
                    break;
                }
            }
        }
    }

    class MessageParserThread extends Thread {

        @Override
        public void run() {
            while (running) {
                ProtocolMsg box;
                if (delegate != null) {
                    box = delegate.createMessage();
                } else {
                    box = new ProtocolMsg();
                }
                try {
                    boolean succ = read(box);
                    if (succ) {
                        onRecvMsgFromServer(box);
                    } else {
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }

        private boolean read(IMessage box) throws IOException {
            // 直接只支持
            while (true) {
                byte[] tmpBuffer = readBufferOutputStream.toByteArray();
                if (readBufferMaxSize >= 0 && tmpBuffer.length > readBufferMaxSize) {
                    // 数据过大，可能错位了。
                    return false;
                }

                if (tmpBuffer.length > 0) {
                    // 说明还是可以尝试一下的
                    int ret = box.unpack(tmpBuffer, 0, tmpBuffer.length);
                    if (ret > 0) {
                        // 说明成功
                        readBufferOutputStream.reset();
                        readBufferOutputStream.write(tmpBuffer, ret, tmpBuffer.length - ret);
                        return true;
                    } else if (ret < 0) {
                        // 说明数据错乱了
                        readBufferOutputStream.reset();
                        return false;
                    }
                }
                // load more data
                if (buffers.isEmpty()) {
                    return false;
                }
                while (!buffers.isEmpty()) {
                    ByteBuffer buffer = null;
                    try {
                        buffer = buffers.remove();
                    } catch (Exception e) {
                        break;
                    }

                    byte[] readChunkBuffer = null;
                    if (buffer.hasArray()) {
                        final byte[] array = buffer.array();
                        final int arrayOffset = buffer.arrayOffset();
                        readChunkBuffer = Arrays.copyOfRange(array, arrayOffset + buffer.position(),
                                arrayOffset + buffer.limit());
                    } else {
                        readChunkBuffer = new byte[buffer.remaining()];
                        buffer.get(readChunkBuffer);
                    }
                    readBufferOutputStream.write(readChunkBuffer);
                    DirectByteBufferCleaner.clean(buffer);

                }
            }
        }
    }

}
