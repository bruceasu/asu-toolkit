package org.nutz.castor.castor;

import org.nutz.lang.util.Mirror;
import org.nutz.castor.Castor;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Class2Mirror extends Castor<Class, Mirror> {

    @Override
    public Mirror<?> cast(Class src, Class toType, String... args) {
        return Mirror.me(src);
    }

}
