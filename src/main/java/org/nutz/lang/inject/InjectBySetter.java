
package org.nutz.lang.inject;

import org.nutz.lang.util.Lang;
import org.nutz.castor.Castors;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InjectBySetter implements Injecting {


    private Method   setter;
    private Class<?> valueType;

    public InjectBySetter(Method setter) {
        this.setter = setter;
        valueType = setter.getParameterTypes()[0];
    }

    @Override
    public void inject(Object obj, Object value) {
        Object v = null;
        try {
            v = Castors.me().castTo(value, valueType);
            setter.invoke(obj, v);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("Fail to value by setter", e);
            }
            throw Lang.makeThrow("Fail to set '%s'[ %s ] by setter %s.'%s()' because [%s]: %s",
                                 value,
                                 v,
                                 setter.getDeclaringClass().getName(),
                                 setter.getName(),
                                 Lang.unwrapThrow(e),
                                 Lang.unwrapThrow(e).getMessage());
        }
    }

}