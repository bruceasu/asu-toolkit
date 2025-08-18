package me.asu.util;

public class Base62 {

    // Base62 字符表
    private static final char[] CHAR_SET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int BASE = CHAR_SET.length;

    private static final int[] INDEXES = new int[128];

    static {
        for (int i = 0; i < INDEXES.length; i++) INDEXES[i] = -1;
        for (int i = 0; i < CHAR_SET.length; i++) {
            INDEXES[CHAR_SET[i]] = i;
        }
    }

    /** 编码数字为 Base62 字符串 */
    public static String encode(long num) {
        if (num == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int rem = (int)(num % BASE);
            sb.append(CHAR_SET[rem]);
            num /= BASE;
        }
        return sb.reverse().toString();
    }

    /** 解码 Base62 字符串为数字 */
    public static long decode(String str) {
        long num = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int val = (c < 128) ? INDEXES[c] : -1;
            if (val == -1) throw new IllegalArgumentException("Invalid Base62 character: " + c);
            num = num * BASE + val;
        }
        return num;
    }

    /** 编码字节数组为 Base62 */
    public static String encode(byte[] data) {
        // 简单做法：先转为正数 BigInteger，再用数字编码
        java.math.BigInteger bi = new java.math.BigInteger(1, data);
        return encode(bi);
    }

    private static String encode(java.math.BigInteger bi) {
        if (bi.equals(java.math.BigInteger.ZERO)) return "0";
        StringBuilder sb = new StringBuilder();
        java.math.BigInteger base = java.math.BigInteger.valueOf(BASE);
        while (bi.compareTo(java.math.BigInteger.ZERO) > 0) {
            java.math.BigInteger[] divmod = bi.divideAndRemainder(base);
            sb.append(CHAR_SET[divmod[1].intValue()]);
            bi = divmod[0];
        }
        return sb.reverse().toString();
    }

    /** 解码 Base62 为字节数组 */
    public static byte[] decodeToBytes(String str) {
        java.math.BigInteger bi = java.math.BigInteger.ZERO;
        java.math.BigInteger base = java.math.BigInteger.valueOf(BASE);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int val = (c < 128) ? INDEXES[c] : -1;
            if (val == -1) throw new IllegalArgumentException("Invalid Base62 character: " + c);
            bi = bi.multiply(base).add(java.math.BigInteger.valueOf(val));
        }
        return bi.toByteArray();
    }

    public static void main(String[] args) {
        long number = 123456789;
        String encoded = Base62.encode(number);
        long decoded = Base62.decode(encoded);
        System.out.println(number + " -> " + encoded + " -> " + decoded);

        byte[] data = "Hello Base62!".getBytes();
        String encodedBytes = Base62.encode(data);
        byte[] decodedBytes = Base62.decodeToBytes(encodedBytes);
        System.out.println(new String(decodedBytes));
    }
}
