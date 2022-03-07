package me.asu.actionchain;

public interface ActionChain<Context> {
    void handle(Context ctx);

    void fireNext(Context ctx);
}