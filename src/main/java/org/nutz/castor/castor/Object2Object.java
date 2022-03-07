package org.nutz.castor.castor;


import org.nutz.lang.util.Mirror;
import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;

public class Object2Object extends Castor<Object, Object> {

    @Override
    public Object cast(Object src, Class<?> toType, String... args)
    throws FailToCastObjectException {
        return Mirror.me(toType).born(src);
    }

}
