package me.asu.net.message;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import me.asu.util.Hex;
import me.asu.util.Bytes;

/**
 * ProtoMessage.
 * <code><pre>
 * format:
 *      |---------+---------+---------+---------+----------+--------+--------+-----------|
 *      | 4 bytes | 4 bytes | 4 bytes | 1 byte  | 1 byte   | 1 byte | 1 byte | N byte(s) |
 *      |---------+---------+---------+---------+----------+--------+--------+-----------|
 *      | cmdId   | seqId   | bodyLen | cmdType | bodyType | code   | ttl    | body data |
 *      |---------+---------+---------+---------+----------+--------+--------+-----------|
 * header:
 *      lengthFieldOffset   = 8 pre header
 *      lengthFieldLength   = 4 only data length
 *      lengthAdjustment    = 4 after header
 *      initialBytesToStrip = 0
 * body:
 *      byte array.
 * </pre></code>
 */
@lombok.Data
public class ProtocolMsg implements IMessage, Cloneable {

    public ProtocolMsgHeader header = new ProtocolMsgHeader();
    /**
     * pb 字节序 或者json字符串.
     */
    public byte[]            body   = EMPTY_BODY;

    public static ProtocolMsg create(ProtocolMsg message) {
        ProtocolMsg newMsg = new ProtocolMsg();
        newMsg.getHeader().readFrom(message.getHeader());

        byte[] newBody;
        byte[] body = message.getBody();
        if (body == null || body.length == 0) {
            newBody = EMPTY_BODY;
        } else {
            newBody = new byte[body.length];
            System.arraycopy(body, 0, newBody, 0, body.length);
        }
        newMsg.setBody(newBody);
        return newMsg;
    }

    public int cmdId() {
        return header.getCmdId();
    }

    @Override
    public byte[] pack() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // adjust
        header.bodyLen = body.length;

        stream.write(header.pack());
        stream.write(body);
        return stream.toByteArray();
    }

    @Override
    public int unpack(byte[] bytes) {
        if (bytes == null) {
            return 0;
        }
        return unpack(bytes, 0, bytes.length);
    }

    @Override
    public int unpack(byte[] bytes, int offset, int length) {
        if (bytes == null) {
            return 0;
        }
        if (readPackage(ByteBuffer.wrap(bytes, offset, length))) {
            return getPackageLength();
        } else {
            return 0;
        }
    }

    @Override
    public int getPackageLength() {
        return getHeaderLength() + getBodyLength();
    }

    public int seqId() {
        return header.getSeqId();
    }

    public ProtocolMsgHeader header() {
        return header;
    }

    public byte[] body() {
        return body;
    }

    public void setBody(byte[] body) {
        if (body == null) {
            this.body = EMPTY_BODY;
        } else {
            this.body = body;
        }
        header.setBodyLen(this.body.length);
    }

    @Override
    public String toString() {
        return "{header: " + header.toString() + ", body: \"" + encodeBody()
                + "\"}";
    }

    private String encodeBody() {
        return header().getBodyType() == 1 ? Bytes.toString(body)
                : Hex.encodeHexString(body);
    }

    public void resetBody() {
        body = EMPTY_BODY;
    }

    private boolean readPackage(ByteBuffer byteBuffer) {
        if (!header.readHeader(byteBuffer)) {
            return false;
        }
        if (!readBody(byteBuffer)) {
            clear();
            return false;
        }
        return true;
    }

    private boolean canReadBody(ByteBuffer byteBuffer) {
        int bodyLength = header.bodyLen;
        return byteBuffer.remaining() >= bodyLength;
    }

    private boolean readBody(ByteBuffer byteBuffer) {
        if (!canReadBody(byteBuffer)) {
            return false;
        }
        body = new byte[header.bodyLen];
        byteBuffer.get(body);
        return true;
    }

    private void clear() {
        resetHeader();
        resetBody();
    }

    private void resetHeader() {
        getHeader().clear();
    }

    private int getHeaderLength() {
        return getHeader().getLength();
    }

    private int getBodyLength() {
        if (body != null && body.length > 0) {
            return body.length;
        } else {
            return getHeader().getBodyLen();
        }
    }

}

