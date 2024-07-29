package me.asu.net.util;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
//import sun.nio.ch.DirectBuffer;

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
            byteBuffer.clear();
            //((DirectBuffer)byteBuffer).cleaner().clean();
            // 获取 Cleaner 对象
            Object cleaner = getDirectByteBufferCleaner(byteBuffer);

            // 释放内存
            if (cleaner != null) {
                try {
                    Method cleanMethod = cleaner.getClass().getMethod("clean");
                    cleanMethod.invoke(cleaner);
                    System.out.println("Direct ByteBuffer memory released successfully.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 获取 Direct ByteBuffer 的 Cleaner 对象
    private static Object getDirectByteBufferCleaner(ByteBuffer buffer) {
        try {
            Method cleanerMethod = buffer.getClass().getMethod("cleaner");
            cleanerMethod.setAccessible(true);
            return cleanerMethod.invoke(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}