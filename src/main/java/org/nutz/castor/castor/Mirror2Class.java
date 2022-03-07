package org.nutz.castor.castor;

import org.nutz.lang.util.Mirror;
import org.nutz.castor.Castor;

@SuppressWarnings({"rawtypes"})
public class Mirror2Class extends Castor<Mirror, Class> {

    @Override
    public Class cast(Mirror src, Class toType, String... args) {
        return src.getType();
    }

}
