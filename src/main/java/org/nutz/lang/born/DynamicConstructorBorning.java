package org.nutz.lang.born;

import org.nutz.lang.util.Mirror;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class DynamicConstructorBorning<T> implements Borning<T> {

    private Constructor<T> c;

    public DynamicConstructorBorning(Constructor<T> c) {
        this.c = c;
        this.c.setAccessible(true);
    }

    public T born(Object... args) {
        try {
            return c.newInstance(Mirror.evalArgToRealArray(args));
        } catch (InvocationTargetException e1) {
			throw new BorningException(e1.getTargetException(), c.getDeclaringClass(), args);
		} catch (Exception e) {
			if (e instanceof BorningException)
				throw (BorningException)e;
			throw new BorningException(e, c.getDeclaringClass(), args);
		}
    }

}
