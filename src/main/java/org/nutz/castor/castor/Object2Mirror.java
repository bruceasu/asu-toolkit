package org.nutz.castor.castor;

import org.nutz.lang.util.Mirror;
import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;

@SuppressWarnings({"rawtypes"})
public class Object2Mirror extends Castor<Object, Mirror> {

    @Override
    public Mirror cast(Object src, Class<?> toType, String... args)
    throws FailToCastObjectException {
        return Mirror.me(src.getClass());
    }

}
