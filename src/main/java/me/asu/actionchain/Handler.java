package me.asu.actionchain;

public interface Handler<Context> {
   default void handle(Context ctx, ActionChain actionChain){
        if (actionChain != null) {
            actionChain.fireNext(ctx);
        }
    }
}