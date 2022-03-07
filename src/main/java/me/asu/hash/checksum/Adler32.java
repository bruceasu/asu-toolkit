package me.asu.hash.checksum;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Checksum;

/**
 * @author suhanjun
 */
public class Adler32 implements Checksum {

    /**
     * largest prime smaller than 65536
     */
    private static final int BASE = 65521;

    private int checksum; // we do all in int.

    // Note that java doesn't have unsigned integers,
    // so we have to be careful with what arithmetic
    // we do. We return the checksum as a long to
    // avoid sign confusion.

    /**
     * Creates a new instance of the <code>Adler32</code> class. The checksum
     * starts off with a value of 1.
     */
    public Adler32() {
        reset();
    }

    /**
     * Resets the Adler32 checksum to the initial value.
     */
    @Override
    public void reset() {
        checksum = 1; // Initialize to 1
    }

    /**
     * Updates the checksum with the byte b.
     *
     * @param bval the data value to add. The high byte of the int is ignored.
     */
    @Override
    public void update(int bval) {
        // We could make a length 1 byte array and call update again, but I
        // would rather not have that overhead
        int s1 = checksum & 0xffff;
        int s2 = checksum >>> 16;

        s1 = (s1 + (bval & 0xFF)) % BASE;
        s2 = (s1 + s2) % BASE;

        checksum = (s2 << 16) + s1;
    }

    /**
     * Updates the checksum with the bytes taken from the array.
     *
     * @param buffer an array of bytes
     */
    public void update(byte[] buffer) {
        update(buffer, 0, buffer.length);
    }

    /**
     * Updates the checksum with the bytes taken from the array.
     *
     * @param buf an array of bytes
     * @param off the start of the data used for this update
     * @param len the number of bytes to use for this update
     */
    @Override
    public void update(byte[] buf, int off, int len) {
        // (By Per Bothner)
        int s1 = checksum & 0xffff;
        int s2 = checksum >>> 16;

        while (len > 0) {
            // We can defer the modulo operation:
            // s1 maximally grows from 65521 to 65521 + 255 * 3800
            // s2 maximally grows by 3800 * median(s1) = 2090079800 < 2^31
            int n = 3800;
            if (n > len) {
                n = len;
            }
            len -= n;
            while (--n >= 0) {
                s1 = s1 + (buf[off++] & 0xFF);
                s2 = s2 + s1;
            }
            s1 %= BASE;
            s2 %= BASE;
        }

        checksum = (s2 << 16) | s1;
    }

    /**
     * Returns the Adler32 data checksum computed so far.
     */
    @Override
    public long getValue() {
        return (long) checksum & 0xffffffffL;
    }

    public void update(InputStream is) throws IOException {
        byte[] buff = new byte[4096];
        while (is.read(buff) > 0) {
            this.update(buff);
        }

    }
}
