
package org.nutz.lang.inject;

import org.nutz.lang.util.Lang;
import org.nutz.castor.Castors;
import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class InjectByField implements Injecting {

    private Field field;

    public InjectByField(Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    @Override
    public void inject(Object obj, Object value) {
        Object v = null;
        try {
            v = Castors.me().castTo(value, field.getType());
            field.set(obj, v);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("Fail to set value by field", e);
            }
            throw Lang.makeThrow("Fail to set '%s'[ %s ] to field %s.'%s' because [%s]: %s",
                                 value,
                                 v,
                                 field.getDeclaringClass().getName(),
                                 field.getName(),
                                 Lang.unwrapThrow(e),
                                 Lang.unwrapThrow(e).getMessage());
        }
    }
}
