package cnblog.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 实现简单的加解密，加密时首先用DES加密，然后
 * 应用base64将byte数组转化为字符串
 */

public class CipherUtil {
final static String KEY = "wyf";//用于DES加密
final static String KEY_DES = "DES";
static SecretKeySpec secretKey;

static {
    try {
        SecureRandom secure = new SecureRandom(KEY.getBytes());
        KeyGenerator generator = KeyGenerator.getInstance(KEY_DES);
        generator.init(secure);
        byte[] key = generator.generateKey().getEncoded();
        secretKey = new SecretKeySpec(key, KEY_DES);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public static String encrypt(String s) {
    try {
        byte[] data = s.getBytes();
        Cipher cipher = Cipher.getInstance(KEY_DES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypt = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encrypt);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

public static String decrypt(String s) {
    try {
        byte[] data = Base64.getDecoder().decode(s);
        Cipher cipher = Cipher.getInstance(KEY_DES);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypt = cipher.doFinal(data);
        return new String(decrypt);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
}
