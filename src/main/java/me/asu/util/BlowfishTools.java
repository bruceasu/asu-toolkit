
package me.asu.util;

import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import me.asu.util.Bytes;
import me.asu.util.Strings;

/**
 * BlowfishTools.
 * @version 1.0.0
 * @since 2017-10-09 14:32
 */
@Getter
public class BlowfishTools {

    /**
     * 密钥算法名称
     */
    public static final String BLOWFISH               = "Blowfish";
    public static       String DEFAULT_TRANSFORMATION = "Blowfish/CBC/PKCS5Padding";
    SecretKeySpec          skSpec;
    AlgorithmParameterSpec iv;
    /**
     * 密钥
     */
    private String encryptKey           = "qazwsxedcrfv";
    /**
     * 初始化向量
     */
    private String initializationVector = "12345678";
    /**
     * 转换模式 (default)
     */
    private String transformation       = DEFAULT_TRANSFORMATION;

    public BlowfishTools() {
        init();
    }

    public BlowfishTools(String key, String iv) {
        this(key, iv, DEFAULT_TRANSFORMATION);
        init();
    }

    public BlowfishTools(String encryptKey, String initializationVector, String transformation) {
        if (Strings.isBlank(encryptKey)) {
            throw new IllegalArgumentException("密钥不能为空。");
        }
        if (Strings.isBlank(initializationVector)) {
            throw new IllegalArgumentException("初始化向量不能为空。");
        }
        this.encryptKey = encryptKey;
        this.initializationVector = initializationVector;
        if (Strings.isNotBlank(transformation)) {
            this.transformation = transformation;
        }
        init();
    }

    private void init() {
        // 根据给定的字节数组构造一个密钥  Blowfish-与给定的密钥内容相关联的密钥算法的名称
        this.skSpec = new SecretKeySpec(Bytes.toBytes(this.encryptKey), BLOWFISH);
        // 使用 initializationVector 中的字节作为 IV 来构造一个 IvParameterSpec 对象
        this.iv = new IvParameterSpec(Bytes.toBytes(this.initializationVector));
    }

    /**
     * 加密.
     *
     * @param text 明文
     * @return 密文
     */
    public byte[] encrypt(byte[] text)
            throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException,
                   InvalidAlgorithmParameterException, InvalidKeyException,
                   IllegalBlockSizeException {
        // 返回实现指定转换的 Cipher 对象
        Cipher cipher = Cipher.getInstance(transformation);
        // 用密钥和随机源初始化此 Cipher
        cipher.init(Cipher.ENCRYPT_MODE, skSpec, iv);
        // 加密文本
        return cipher.doFinal(text);
    }

    /**
     * 解密.
     *
     * @param encrypted 密文
     * @return 明文
     */
    public byte[] decrypt(byte[] encrypted)
            throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
                   IllegalBlockSizeException, BadPaddingException,
                   InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(DEFAULT_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, skSpec, iv);
        return cipher.doFinal(encrypted);

    }

    /**
     * 加密.
     * 用utf-8编码成byte[]。
     *
     * @param text 明文
     * @return 密文
     */
    public byte[] encrypt(String text)
            throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException,
                   InvalidAlgorithmParameterException, InvalidKeyException,
                   IllegalBlockSizeException {
        return encrypt(Bytes.toBytes(text));
    }

    /**
     * 解密.
     * 直接用utf-8编码解析成字符串。
     *
     * @param encrypted 密文
     * @return 明文
     */
    public String decryptToString(byte[] encrypted)
            throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
                   IllegalBlockSizeException, BadPaddingException,
                   InvalidAlgorithmParameterException {
        return Bytes.toString(decrypt(encrypted));
    }


}
