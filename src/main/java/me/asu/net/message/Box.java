package me.asu.net.message;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

// 与python的netkit不同
// java版只认网络字节序，因为java的所有存储都是以网络字节序
// 只支持packet_len

public class Box extends AbstracBox {

    // 魔法数字
    public final static int MAGIC = 142857;

    // 默认就用这个值
    public int magic = MAGIC;
    public short version;
    public short flag;
    // 这个只用来在网络上传输，放在这里是怕忘记了
    private int _transfer_packet_len;

    // 压包
    @Override
    public byte[] pack() {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(this.getPackageLength());
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

        try{
            outputStream.writeInt(magic);
            outputStream.writeShort(version);
            outputStream.writeShort(flag);
            outputStream.writeInt(getPackageLength());
            if (body != null) {
                outputStream.write(body);
            }

            outputStream.flush();
        }
        catch (Exception e) {
            return null;
        }

        return byteArrayOutputStream.toByteArray();
    }

    //>0: 成功生成obj，返回了使用的长度，即剩余的部分buf要存起来
    //<0: 报错
    //0: 继续收
    public int unpack(byte[] buf, int offset, int length, boolean save) {

        if (buf == null) {
            return -100;
        }

        length = buf.length > length ? length : buf.length;

        if (length < this.headerLen()) {
            return 0;
        }

        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(buf, offset, length));

        int _magic;
        short _version;
        short _flag;
        int _packet_len;
        int _body_len;
        byte[] _body = null;

        try {
            _magic = inputStream.readInt();
            _version = inputStream.readShort();
            _flag = inputStream.readShort();
            _packet_len = inputStream.readInt();
        }
        catch (Exception e) {
            return -1;
        }

        if (_magic != MAGIC) {
            // 包错误
            return -2;
        }

        if (length < _packet_len) {
            // 继续收
            return 0;
        }

        _body_len = _packet_len - this.headerLen();

        try{
            if (_body_len > 0) {
                _body = new byte[_body_len];
                int len = inputStream.read(_body);

                // 说明解出来的body长度不对
                if (len != _body_len) {
                    System.out.println(len);
                    return -3;
                }
            }
        }
        catch (Exception e) {
            // 解body异常
            return -4;
        }

        if (!save) {
            return _packet_len;
        }

        this.magic = _magic;
        this.version = _version;
        this.flag = _flag;
        this._transfer_packet_len = _packet_len;
        this.body = _body;

        this._unpack_done = true;

        return _packet_len;
    }

    public int headerLen() {
        return 4 * 2 + 2 * 2;
    }

    public int bodyLen() {
        if (body != null) {
            return body.length;
        }

        return 0;
    }

    @Override
    public int getPackageLength() {
        return headerLen() + bodyLen();
    }

    @Override
    public String toString() {
        String out = "";
        out += "magic: " + magic;
        out += ", version: " + version;
        out += ", flag: " + flag;
        out += ", packetLen: " + getPackageLength();
        out += ", bodyLen: " + bodyLen();

        return out;
    }
}
