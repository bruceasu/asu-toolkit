package me.asu.net.socket.server;


import lombok.extern.slf4j.Slf4j;
import me.asu.net.message.*;

/**
 * 服务处理器
 *
 * 
 */
public interface Handler {

    /**
     * 打开连接通知.
     *
     * @return IMessage
     */
    void onOpen(ChannelContext ctx);

    /**
     * 接收数据通知.
     *
     * @return IMessage
     */
    void onRecv(IMessage message, ChannelContext ctx);

    /**
     * 关闭通知.
     */
    void onClose(ChannelContext ctx);

    /**
     * 错误通知.
     *
     */
    void onError(int code, Throwable cause, ChannelContext ctx);

    /**
     * 超时通知.
     */
    void onTimeout(ChannelContext ctx);

    /**
     * 创建一个数据对象.
     *
     * @return IMessage
     */
    ProtocolMsg createMessage();

    @Slf4j
    class HandlerAdapter implements Handler {

        @Override
        public void onOpen(ChannelContext ctx) {
            log.debug("{}: open.", ctx.getAddress());
        }

        @Override
        public void onRecv(IMessage message, ChannelContext ctx)  {
            log.debug("{}: received a message.", ctx.getAddress());
        }

        @Override
        public void onClose(ChannelContext ctx) {
            log.debug("{}: close.", ctx.getAddress());
        }

        @Override
        public void onError(int code, Throwable cause, ChannelContext ctx) {
            log.error("{}: error code: {}, reason: {}.", ctx.getAddress(), code, cause == null ? "" : cause.getMessage());
        }

        @Override
        public void onTimeout(ChannelContext ctx) {
            log.error("{}: timeout.", ctx.getAddress());
        }

        @Override
        public ProtocolMsg createMessage() {
            return new ProtocolMsg();
        }
    }
}
