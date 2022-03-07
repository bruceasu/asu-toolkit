package org.nutz.castor.castor;

import org.nutz.lang.util.Mirror;
import org.nutz.castor.Castor;

@SuppressWarnings({"rawtypes"})
public class Mirror2String extends Castor<Mirror, String> {

    @Override
    public String cast(Mirror src, Class<?> toType, String... args) {
        return src.getType().getName();
    }

}
