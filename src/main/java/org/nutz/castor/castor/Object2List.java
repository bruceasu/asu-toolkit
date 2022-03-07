package org.nutz.castor.castor;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.Lang;
import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;

@SuppressWarnings({"rawtypes"})
public class Object2List extends Castor<Object, List> {

    @Override
    @SuppressWarnings("unchecked")
    public List cast(Object src, Class<?> toType, String... args) throws FailToCastObjectException {
        try {
            List<Object> list = (List<Object>) (toType == List.class ? new ArrayList<Object>(1)
                                                                    : toType.newInstance());
            list.add(src);
            return list;
        }
        catch (Exception e) {
            throw Lang.wrapThrow(e);
        }
    }

}
