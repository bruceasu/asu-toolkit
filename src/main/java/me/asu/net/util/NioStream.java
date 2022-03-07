package me.asu.net.util;

import static me.asu.net.NetConstants.ERROR_SEND;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.asu.net.socket.client.Delegate;
import me.asu.net.message.IMessage;
import me.asu.net.message.ProtocolMsg;

/**
 * NioStream
 *
 * @version 1.0.0
 * @since 2017-10-11 17:40
 */
@Slf4j
public class NioStream {

    private final static int                   READ_CHUNK_SIZE      = 4 * 1024;
    private final static int                   READ_BUFFER_MAX_SIZE = -1;
    private final        ByteBuffer            buffer;
    private              byte[]                readChunkBuffer      = null;
    private              int                   readBufferMaxSize;
    private              ByteArrayOutputStream readBufferOutputStream;
    private              Selector              selector;
    @Getter
    @Setter
    private              Delegate              delegate;
    @Getter
    @Setter
    private volatile     boolean               running              = false;

    private LinkedBlockingDeque<IMessage> msgQueueToServer = new LinkedBlockingDeque<IMessage>();

    public NioStream(SocketChannel channel) throws IOException {
        this(channel, READ_CHUNK_SIZE, READ_BUFFER_MAX_SIZE);
    }

    public NioStream(SocketChannel channel, int readChunkSize)
    throws IOException {
        this(channel, readChunkSize, READ_BUFFER_MAX_SIZE);
    }

    public NioStream(SocketChannel channel,
            int readChunkSize,
            int readBufferMaxSize)
    throws IOException {
        channel.configureBlocking(false);
        this.readBufferMaxSize = readBufferMaxSize;
        readChunkSize          = readChunkSize > 0 ? readChunkSize
                : READ_CHUNK_SIZE;
        buffer                 = ByteBuffer.allocate(readChunkSize);
        readBufferOutputStream = new ByteArrayOutputStream(readChunkSize << 1);
        this.selector          = Selector.open();
        channel.register(this.selector,
                SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    public boolean send(IMessage message) {
        return msgQueueToServer.offer(message);

    }

    public void listen() throws IOException {
        running = true;
        while (running) {
            selector.select();
            Iterator<SelectionKey> ite = this.selector.selectedKeys()
                                                      .iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                ite.remove();
                if (key.isConnectable()) {
                    connect(key);
                }
                if (key.isReadable()) {
                    read(key);
                }
                if (key.isWritable()) {
                    write(key);
                }
            }
        }
    }

    public void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(this.selector,
                SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        onConnOpen(channel);
    }

    public void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ProtocolMsg box;
        if (delegate != null) {
            box = delegate.createMessage();
        } else {
            box = new ProtocolMsg();
        }
        try {
            boolean succ = read(channel, box);
            if (succ) {
                onRecvMsgFromServer(box);
            } else {
            }
        } catch (Exception e) {
            channel.close();
            onConnClose(channel);
        }
        key.interestOps(key.interestOps() | SelectionKey.OP_READ);
    }

    public void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        IMessage box = null;
        do {
            try {
                box = msgQueueToServer.poll();
                if (box != null) {
                    channel.write(ByteBuffer.wrap(box.pack()));
                }
            } catch (Exception e) {
                if (!(e instanceof InterruptedException)) {
                    log.error("", e);
                }
                if (box != null) {
                    msgQueueToServer.addFirst(box);
                }
                onError(ERROR_SEND, box);
            }
        } while (box != null);
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
    }

    public boolean read(SocketChannel channel, IMessage box)
    throws IOException {
        // 直接只支持
        while (true) {
            byte[] tmpBuffer = readBufferOutputStream.toByteArray();
            if (readBufferMaxSize >= 0
                    && tmpBuffer.length > readBufferMaxSize) {
                // 数据过大，可能错位了。
                return false;
            }

            if (tmpBuffer.length > 0) {
                // 说明还是可以尝试一下的
                int ret = box.unpack(tmpBuffer, 0, tmpBuffer.length);
                if (ret > 0) {
                    // 说明成功
                    readBufferOutputStream.reset();
                    readBufferOutputStream.write(tmpBuffer, ret,
                            tmpBuffer.length - ret);
                    return true;
                } else if (ret < 0) {
                    // 说明数据错乱了
                    readBufferOutputStream.reset();
                }
            }
            buffer.clear();
            int read = channel.read(buffer);
            if (read == -1 || read == 0) {
                return false;
            }
            buffer.flip();
            if (buffer.hasArray()) {
                final byte[] array       = buffer.array();
                final int    arrayOffset = buffer.arrayOffset();
                readChunkBuffer = Arrays.copyOfRange(array,
                        arrayOffset + buffer.position(),
                        arrayOffset + buffer.limit());
            } else {
                readChunkBuffer = new byte[buffer.remaining()];
                buffer.get(readChunkBuffer);
            }
            int len = readChunkBuffer.length;
            if (len > 0) {
                // 进入下个循环自然会判断
                readBufferOutputStream.write(readChunkBuffer, 0, len);
            } else {
                return false;
            }
        }
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

}
