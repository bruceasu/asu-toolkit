package me.asu.net.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import me.asu.util.Bytes;

@lombok.Data
public class ProtocolMsgHeader {
    public static final byte BODY_TYPE_JSON = 0;
    public static final byte BODY_TYPE_RAW = 1;
    public static final byte BODY_TYPE_XML = 2;
    public static final byte BODY_TYPE_STR_UTF8 = 3;
    public static final byte BODY_TYPE_BASE64_STR = 4;
    public static final byte BODY_TYPE_HEX_STR = 5;

    public static final byte CMD_TYPE_REQUEST = 0;
    public static final byte CMD_TYPE_RESPONSE = 1;
    public static final byte CMD_TYPE_NOTIFICATION = 2;

    /**
     * java 不支持 uint32
     */
    public int  cmdId;
    /**
     * 消息系列号，主要用于Request和Response，Response的值必须和Request相同，使得发送端可以进行事务匹配处理
     */
    public int  seqId;
    /**
     * body length
     */
    public int  bodyLen;
    /**
     * java 不支持 uint8_t. REQUEST:0,  RESPONSE:1, NOTIFICATION:2
     */
    public byte cmdType = CMD_TYPE_REQUEST;
    /**
     * json: 0
     */
    public byte bodyType = BODY_TYPE_JSON;
    /**
     * 请求时为0, 返回时为对应的消息错误码
     */
    public byte code;
    /**
     * time to live
     */
    public byte ttl;

    public int getLength() {
        // 4 *3 + 1 * 4
        return 16;
    }

    public void clear() {
        cmdId = 0;
        seqId = 0;
        bodyLen = 0;
        cmdType = 0;
        bodyType = 0;
        code = 0;
        ttl = 0;
    }

    public void readFrom(ProtocolMsgHeader h) {
        this.cmdId = h.cmdId;
        this.seqId = h.seqId;
        this.bodyLen = h.bodyLen;
        this.cmdType = h.cmdType;
        this.bodyType = h.bodyType;
        this.code = h.code;
        this.ttl = h.ttl;
    }

    public byte[] pack() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(Bytes.toBytes(cmdId));
        stream.write(Bytes.toBytes(seqId));
        stream.write(Bytes.toBytes(bodyLen));
        stream.write(cmdType);
        stream.write(bodyType);
        stream.write(code);
        stream.write(ttl);
        return stream.toByteArray();
    }

    public boolean canReadHeader(ByteBuffer byteBuffer) {
        return byteBuffer.remaining() >= getLength();
    }

    public boolean readHeader(ByteBuffer byteBuffer) {
        if (!canReadHeader(byteBuffer)) {
            return false;
        }
        cmdId = byteBuffer.getInt();
        seqId = byteBuffer.getInt();
        bodyLen = byteBuffer.getInt();
        cmdType = byteBuffer.get();
        bodyType = byteBuffer.get();
        code = byteBuffer.get();
        ttl = byteBuffer.get();
        return true;
    }

}
