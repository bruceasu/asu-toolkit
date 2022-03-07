package org.nutz.castor.castor;

import org.nutz.lang.util.Lang;

import java.util.Map;
import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;

@SuppressWarnings({"rawtypes"})
public class Map2Object extends Castor<Map, Object> {

    @Override
    public Object cast(Map src, Class<?> toType, String... args) throws FailToCastObjectException {
        return Lang.map2Object(src, toType);
    }

}
