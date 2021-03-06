package org.nutz.lang.eject;

import org.nutz.lang.util.Lang;
import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EjectByField implements Ejecting {

    private Field field;

    public EjectByField(Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    @Override
    public Object eject(Object obj) {
        try {
            return null == obj ? null : field.get(obj);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("Fail to get value by field", e);
            }
            throw Lang.makeThrow("Fail to get field %s.'%s' because [%s]: %s",
                                 field.getDeclaringClass().getName(),
                                 field.getName(),
                                 Lang.unwrapThrow(e),
                                 Lang.unwrapThrow(e).getMessage());
        }
    }

}
