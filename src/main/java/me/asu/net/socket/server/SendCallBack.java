package me.asu.net.socket.server;

public interface SendCallBack {

    void onComplete(boolean suc);

    void onError(Throwable throwable);
}
