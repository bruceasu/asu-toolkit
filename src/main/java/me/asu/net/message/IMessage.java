package me.asu.net.message;

import java.io.IOException;
import java.nio.charset.Charset;


public interface IMessage {

    byte[]  EMPTY_BODY = new byte[0];
    Charset CS_UTF8    = Charset.forName("utf-8");

    /**
     * pack.
     *
     * @return 字节数组(byte[])
     * @throws IOException 异常
     */
    byte[] pack() throws IOException;

    /**
     * unpack.
     *
     * @param bytes 字节数组(byte[])
     * @return 包长度，0 表示失败。
     */
    int unpack(byte[] bytes);

    /**
     * unpack.
     *
     * @param bytes  字节数组(byte[])
     * @param offset 开始位置
     * @param length 数据长度
     * @return 包长度，0 表示失败。
     */
    int unpack(byte[] bytes, int offset, int length);

    int getPackageLength();
}

