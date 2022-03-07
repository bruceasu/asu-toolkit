package org.nutz.castor.castor;

import org.nutz.lang.util.Times;
import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;

public abstract class DateTimeCastor<FROM, TO> extends Castor<FROM, TO> {

    protected java.util.Date toDate(String src) {
        try {
            return Times.D(src);
        }
        catch (Throwable e) {
            throw new FailToCastObjectException(e,
                                                "'%s' to %s",
                                                src,
                                                java.util.Date.class.getName());
        }
    }

}
