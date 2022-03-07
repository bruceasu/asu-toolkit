package org.nutz.lang.eject;

import org.nutz.lang.util.Lang;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class EjectByGetter implements Ejecting {

    private Method getter;

    public EjectByGetter(Method getter) {
        this.getter = getter;
    }

    @Override
    public Object eject(Object obj) {
        try {
            return null == obj ? null : getter.invoke(obj);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("Fail to value by getter", e);
            }
            throw Lang.makeThrow("Fail to invoke getter %s.'%s()' because [%s]: %s",
                                 getter.getDeclaringClass().getName(),
                                 getter.getName(),
                                 Lang.unwrapThrow(e),
                                 Lang.unwrapThrow(e).getMessage());
        }
    }

}
