package org.nutz.castor.castor;


import org.nutz.lang.util.Lang;
import org.nutz.lang.util.Strings;
import org.nutz.castor.Castor;

public class String2Boolean extends Castor<String, Boolean> {

    @Override
    public Boolean cast(String src, Class<?> toType, String... args) {
        if (Strings.isBlank(src))
            return false;
        return Lang.parseBoolean(src);
    }

}
