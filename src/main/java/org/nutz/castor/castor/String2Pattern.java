package org.nutz.castor.castor;

import org.nutz.lang.util.Lang;

import java.util.regex.Pattern;
import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;


public class String2Pattern extends Castor<String, Pattern> {

    @Override
    public Pattern cast(String src, Class<?> toType, String... args)
    throws FailToCastObjectException {
        try {
            return Pattern.compile(src);
        }
        catch (Exception e) {
            throw new FailToCastObjectException("Error regex: " + src, Lang.unwrapThrow(e));
        }
    }

}
