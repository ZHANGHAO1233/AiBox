package com.box.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {

    public static String encrypt(String content, String password) {

        try {
            byte[] encryptedBytes = aes_128_cbc_pkcs5_encrypt(content.getBytes("UTF-8"), password);
            return android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.NO_WRAP);
//            return new String(Base64.encodeBase64Chunked(encryptedBytes));
//            return Base64.encodeBase64String(encryptedBytes);
            // new Base64().getEncoder().encodeToString(encryptedBytes);
        } catch (NullPointerException | UnsupportedEncodingException e) {
            ILog.d("encrypt throws " + e.getClass().getName() + ":::" + e.getMessage());
        }
        ILog.d("encrypt failed.");
        return null;
    }


    final public static int SECURITY_CRYPTO_BITS = 128;

    public static byte[] sha1Hash(byte[] inputBytes) throws NoSuchAlgorithmException {
        MessageDigest mdInstance = MessageDigest.getInstance("SHA-1");
        mdInstance.update(inputBytes);
        byte[] resultBytes = mdInstance.digest();
        return resultBytes;
    }

    private static SecretKeySpec getSecretKeySpec(String password)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] keyBytes = new byte[SECURITY_CRYPTO_BITS / 8];
        Arrays.fill(keyBytes, (byte) 0x00);

        byte[] passwordBytes = sha1Hash(password.getBytes("UTF-8"));

        int length = passwordBytes.length < keyBytes.length ? passwordBytes.length : keyBytes.length;
        System.arraycopy(passwordBytes, 0, keyBytes, 0, length);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        return keySpec;
    }

    private static IvParameterSpec getIvParameterSpec(String password)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] iv = new byte[16];

        Arrays.fill(iv, (byte) 0x00);

        byte[] passwordBytes = sha1Hash(password.getBytes("UTF-8"));

        int length = passwordBytes.length < iv.length ? passwordBytes.length : iv.length;
        System.arraycopy(passwordBytes, 0, iv, 0, length);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        return ivParameterSpec;
    }

    public static byte[] aes_128_cbc_pkcs5_encrypt(byte[] plainBytes, String password) throws NullPointerException {
        if (plainBytes.length == 0 || plainBytes == null) {
            throw new NullPointerException("Invalid parameter - content");
        }

        if (password.length() == 0 || password == null) {
            throw new NullPointerException("Invalid parameter - password");
        }

        try {
            SecretKeySpec secKeySpec = getSecretKeySpec(password);
            IvParameterSpec ivParamSpec = getIvParameterSpec(password);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secKeySpec, ivParamSpec);

            return cipher.doFinal(plainBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}
