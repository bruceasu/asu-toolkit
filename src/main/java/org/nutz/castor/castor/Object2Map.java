package org.nutz.castor.castor;

import java.util.Map;

import org.nutz.lang.util.Lang;
import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Object2Map extends Castor<Object, Map> {

    @Override
    public Map<String, Object> cast(Object src, Class<?> toType, String... args) throws
                                                                                 FailToCastObjectException {
        return Lang.obj2map(src, (Class<? extends Map<String, Object>>)toType);
    }

}
