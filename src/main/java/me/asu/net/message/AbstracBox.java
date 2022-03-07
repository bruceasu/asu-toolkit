
package me.asu.net.message;

public abstract class AbstracBox implements IMessage {

    // 如果解析完毕会被设置为true
    protected boolean _unpack_done = false;

    // 包体
    public byte[] body = null;

    // 检查
    public int check(byte[] buf) {
        return check(buf, 0, buf == null ? 0 : buf.length);
    }

    // 检查
    public int check(byte[] buf, int offset, int length) {
        return unpack(buf, offset, length, false);
    }

    public abstract int unpack(byte[] buf,
            int offset,
            int length,
            boolean save);

    // 解包
    @Override
    public int unpack(byte[] buf) {
        return unpack(buf, 0, buf == null ? 0 : buf.length);
    }


    @Override
    public int unpack(byte[] buf, int offset, int length) {
        return unpack(buf, offset, length, true);
    }

    public boolean unpackDone() {
        return _unpack_done;
    }

    public int getMagic() {
        return 0;
    }
}
