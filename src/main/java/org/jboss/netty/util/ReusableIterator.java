package org.jboss.netty.util;

import java.util.Iterator;

public interface ReusableIterator<E> extends Iterator<E> {
    void rewind();
}