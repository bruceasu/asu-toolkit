package me.asu.net.socket.uitl;

import java.io.*;
import java.util.concurrent.TimeoutException;
import me.asu.net.message.*;
import java.net.Socket;

/**
 * Stream
 *
 * @version 1.0.0
 * @since 2017-10-11 17:40
 */
public class Stream implements Closeable {

    private final static int READ_CHUNK_SIZE = 4 * 1024;
    private final static int READ_BUFFER_MAX_SIZE = -1;

    private Socket socket = null;
    private int readTimeout = 0;
    private byte[] readChunkBuffer = null;
    private int readBufferMaxSize;
    private ByteArrayOutputStream readBufferOutputStream;

    public Stream() {
        this(null, READ_CHUNK_SIZE, READ_BUFFER_MAX_SIZE);
    }


    public Stream(Socket socket) {
        this(socket, READ_CHUNK_SIZE, READ_BUFFER_MAX_SIZE);
    }

    public Stream(Socket socket, int readChunkSize) {
        this(socket, readChunkSize, READ_BUFFER_MAX_SIZE);
    }

    public Stream(Socket socket, int readChunkSize, int readBufferMaxSize) {
        this.readBufferMaxSize = readBufferMaxSize;
        readChunkSize = readChunkSize > 0 ? readChunkSize : READ_CHUNK_SIZE;
        // readChunkSize 一次性读取的大小
        readChunkBuffer = new byte[readChunkSize];
        readBufferOutputStream = new ByteArrayOutputStream(readChunkSize << 1);
        this.socket = socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public boolean read(IMessage box) throws IOException, TimeoutException {
        if (this.isClosed()) {
           throw new IllegalStateException("socket is closed.");
        }
        long startTime = System.currentTimeMillis();

        // 直接只支持
        while (true) {
            if (readTimeout > 0) {
                if (startTime + readTimeout < System.currentTimeMillis()) {
                    throw new TimeoutException();
                }
            }
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
                }
            }

            int len = this.socket.getInputStream()
                    .read(readChunkBuffer, 0, readChunkBuffer.length);
            // 如果读取超时会抛出异常：java.net.SocketTimeoutException，不会进入下面的逻辑
            // -1：当server关闭的时候会报这个错误
            if (len <= 0) {
                // 说明报错了，或者连接失败了
                try {
                    this.close();
                } catch (Exception ignore) {
                }
                return false;
            }
            // 进入下个循环自然会判断
            readBufferOutputStream.write(readChunkBuffer, 0, len);
        }
    }

    public void write(IMessage box) throws IOException {
        if (this.isClosed()) {
            throw new IllegalStateException("socket is closed.");
        }
        this.socket.getOutputStream().write(box.pack());
        this.socket.getOutputStream().flush();
    }

    public void write(byte[] box) throws IOException {
        if (this.isClosed()) {
            throw new IOException("closed");
        }
        this.socket.getOutputStream().write(box);
        this.socket.getOutputStream().flush();
    }

    @Override
    public void close() throws IOException {
        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }
    }

    public boolean isClosed() {
        // 之所以删掉socket.isClosed() 判断是因为不准
        return this.socket == null;
    }

    public void shutdown(int how) {
        // 要到用的时候才判断是否空指针
        // 比如在ferry中，disconnect是通过shutdown实现的，就会触发其另一个线程的close函数
        if (how == 0 || how == 2) {
            try {
                if (this.socket != null) {
                    this.socket.shutdownInput();
                }
            } catch (IOException e) {
            } catch (Exception e) {
                // 由于跨线程，可能报空指针错误
            }
        }

        if (how == 1 || how == 2) {
            try {
                if (this.socket != null) {
                    this.socket.shutdownOutput();
                }
            } catch (IOException e) {
            } catch (Exception e) {
                // 由于跨线程，可能报空指针错误
            }
        }
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
