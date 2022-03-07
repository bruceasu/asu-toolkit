package org.nutz.lang.born;

import org.nutz.lang.util.Lang;

import java.lang.reflect.Constructor;


public class ConstructorCastingBorning<T> implements Borning<T> {

    private Constructor<T> c;
    private Class<?>[] pts;

    public ConstructorCastingBorning(Constructor<T> c) {
        this.c = c;
        this.c.setAccessible(true);
        this.pts = c.getParameterTypes();
    }

    public T born(Object... args) {
        try {
            args = Lang.array2ObjectArray(args, pts);
            return c.newInstance(args);
        }
        catch (Exception e) {
            throw new BorningException(e, c.getDeclaringClass(), args);
        }
    }

}
