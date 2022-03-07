package me.asu.net.util;
import java.nio.ByteBuffer;
import sun.nio.ch.DirectBuffer;

/**
 * DirectByteBufferCleaner.
 * 手工释放 ByteBuffer.allocateDirect(int capacity)
 * 申请的内存，不等gc回收。
 *
 * @version 1.0.0
 * @since 2017-10-31 13:35
 */
public class DirectByteBufferCleaner {

    public static void clean(final ByteBuffer byteBuffer) {
        if (byteBuffer.isDirect()) {
            ((DirectBuffer)byteBuffer).cleaner().clean();
        }
    }
}