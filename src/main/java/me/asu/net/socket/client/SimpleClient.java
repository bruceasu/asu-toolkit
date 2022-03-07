package me.asu.net.socket.client;

import static me.asu.net.NetConstants.ERROR_RECV;
import static me.asu.net.NetConstants.ERROR_SEND;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.asu.net.message.IMessage;
import me.asu.net.message.ProtocolMsg;
import me.asu.net.util.Stream;

/**
 * SimpleClient.
 *
 * @version 1.0.0
 * @since 2017-10-11 17:51
 */
@Data
@Slf4j
public class SimpleClient extends AbstractSimpleClient {


    public SimpleClient() {
        stream = new Stream();
    }

    public SimpleClient(String host, int port) {
        this();
        this.host = host;
        this.port = port;
    }

    public SimpleClient(Delegate delegate, String host, int port) {
        this();
        this.delegate = delegate;
        this.host = host;
        this.port = port;
    }


    public void connect() {
        shouldConnect = true;
        while (!isConnected()) {
            if (shouldConnect) {
                connectToServer();
            }
            if (!isConnected()) {
                // 毫秒
                try {
                    Thread.sleep(tryConnectInterval);
                } catch (Exception e) {
                    log.error("e: " + e);
                }
                continue;
            }
        }
    }

    /**
     * 阻塞发送报文。
     *
     * @param message 报文
     */
    public void send(IMessage message) {
        try {
            if (isConnected()) {
                stream.write(message);
                onSendMsgToServer(message);
            } else {
                // try reconnect.
                connect();
                if (isConnected()) {
                    stream.write(message);
                    onSendMsgToServer(message);
                } else {
                    throw new IllegalStateException("Not connect yet.");
                }
            }
        } catch (Exception e) {
            onError(ERROR_SEND, message);
            throw new RuntimeException(e);
        }
    }

    /**
     * 阻塞发送报文。
     *
     * @param message 报文
     */
    public void send(byte[] message) {
        try {
            if (isConnected()) {
                stream.write(message);
                onSendMsgToServer(null);
            } else {
                // try reconnect.
                connect();
                if (isConnected()) {
                    stream.write(message);
                    onSendMsgToServer(null);
                } else {
                    throw new IllegalStateException("Not connect yet.");
                }
            }
        } catch (Exception e) {
            onError(ERROR_SEND, null);
            throw new RuntimeException(e);
        }
    }

    /**
     * 阻塞获取报文。
     *
     * @param box 报文容器
     * @return {@link IMessage} 报文容器
     */
    public IMessage receive(IMessage box) {
        if (box == null && delegate != null) {
            box = delegate.createMessage();
        } else {
            box = new ProtocolMsg();
        }
        if (doRead(box)) {
            return box;
        }
        return null;
    }

    /**
     * 阻塞获取报文。
     *
     * @return {@link IMessage} 报文容器
     */
    public IMessage receive() {
        IMessage box;
        if (delegate != null) {
            box = delegate.createMessage();
        } else {
            box = new ProtocolMsg();
        }
        if (doRead(box)) {
            return box;
        }
        return null;
    }

    private boolean doRead(IMessage box) {
        try {
            boolean succ = stream.read(box);
            if (succ) {
                onRecvMsgFromServer(box);
                return true;
            } else {
                // 先自己也关闭掉
                closeConn();
                onError(ERROR_RECV, box);
            }
        } catch (Exception e) {
            log.error("e: " + e);
            closeConn();
            onError(ERROR_RECV, box);
        }
        return false;
    }
}
