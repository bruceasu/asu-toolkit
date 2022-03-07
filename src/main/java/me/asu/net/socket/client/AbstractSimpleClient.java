package me.asu.net.socket.client;

import static me.asu.net.NetConstants.ERROR_OPEN;

import java.net.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.asu.net.message.IMessage;
import me.asu.net.util.Stream;

/**
 * AbstractSimpleClient.
 * @version 1.0.0
 * @since 2017-10-27 9:24
 */
@Data
@Slf4j
public abstract class AbstractSimpleClient {

    /** 连接重试间隔(秒). */
    protected static final int MSG_QUEUE_TO_SERVER_MAX_SIZE = 100;
    /** 连接重试间隔(毫秒). */
    protected static final int TRY_CONNECT_INTERVAL = 1000;
    /** 连接超时(秒). */
    protected static final int CONNECT_TIMEOUT = 5;

    /** 最后一次活跃时间，包括onOpen和onRecv。onClose时要清零. */
    protected long lastActiveTimeMills = 0;
    protected Delegate delegate;
    protected String host = "127.0.0.1";
    protected int port;
    protected boolean shouldConnect = false;
    protected Stream stream;
    protected int tryConnectInterval = TRY_CONNECT_INTERVAL;
    protected int connectTimeout = CONNECT_TIMEOUT;


    public void closeConn() {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }


    public void disconnect() {
        stream.shutdown(2);
    }

    public boolean isConnected() {
        return stream!= null && !stream.isClosed();
    }

    protected void onError(int code, IMessage ibox) {
        if (delegate != null) {
            delegate.onError(code, ibox);
        }
    }

    protected void onTimeout() {
        if (delegate != null) {
            delegate.onTimeout(host, port);
        }
    }

    protected void onConnClose() {
        lastActiveTimeMills = 0;
        shouldConnect = false;
        if (delegate != null) {
            delegate.onClose(host, port);
        }
    }

    protected void onConnOpen() {
        lastActiveTimeMills = System.currentTimeMillis();
        if (delegate != null) {
            delegate.onOpen(host, port);
        }
    }

    protected void onSendMsgToServer(IMessage ibox) {
        if (delegate != null) {
            delegate.onSend(ibox);
        }
    }

    protected void onRecvMsgFromServer(IMessage ibox) {
        if (delegate != null) {
            delegate.onRecv(ibox);
        }
        lastActiveTimeMills = System.currentTimeMillis();
    }

    synchronized protected void connectToServer() {
        closeConn();
        Socket socket;
        try {
            socket = new Socket();
            InetSocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, connectTimeout * 1000);
            socket.setReuseAddress(true);
            socket.setTcpNoDelay(true);
            // false 默认，尝试发送剩余数据;
            // true,0 立即;
            // true, timeout 等待timeout时间，发送剩余数据
            socket.setSoLinger(true, 0);
        } catch (SocketTimeoutException e) {
            log.error("", e);
            onTimeout();
            return;
        } catch (Exception e) {
            log.error("", e);
            onError(ERROR_OPEN, null);
            return;
        }
        stream.setSocket(socket);
        onConnOpen();
    }
}
