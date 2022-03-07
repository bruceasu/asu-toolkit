package me.asu.net.socket.server;

import java.net.InetSocketAddress;
import lombok.Data;
import me.asu.net.message.IMessage;

/**
 * .
 * @since 2018/11/13
 */
@Data
public class ChannelContext {

    Worker            worker;
    InetSocketAddress address;
    String            host;
    int               port;

    public ChannelContext(Worker worker) {
        this.worker = worker;
        address = (InetSocketAddress) worker.getSocket().getRemoteSocketAddress();
        host = address.getAddress().getHostAddress();
        port = address.getPort();
    }

    public void send(IMessage message) {
        worker.addSendData(message);
    }

    public void send(IMessage message, SendCallBack callBack) {
        worker.addSendData(message, callBack);
    }

    public void send(byte[] message) {
        worker.addSendData(message);
    }

    public void send(byte[] message, SendCallBack callBack) {
        worker.addSendData(message, callBack);
    }

    public void close() {
        worker.shutdown();
    }

}
