package me.asu.actionchain;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

public class DefaultActionChain<Context> implements ActionChain<Context>
{

    private static final Logger log = getLogger(DefaultActionChain.class);
    private ActionChain<Context> next;
    private Handler<Context>     handler;

    public DefaultActionChain(ActionChain next, Handler handler) {
        this.next = next;
        this.handler = handler;
    }


    @Override
    public void handle(Context ctx) {
        log.debug("handle {}", handler.getClass());
        handler.handle(ctx,this);
    }

    @Override
    public void fireNext(Context ctx){
        ActionChain next_ = this.next;
        if(next_ != null){
            next_ .handle(ctx);
        }

    }
}