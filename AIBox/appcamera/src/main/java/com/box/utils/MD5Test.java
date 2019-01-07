package com.box.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Test {

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F' };

    public static String getMD5(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            try {
                byte[] bytes = digest.digest(content.getBytes("UTF-8"));
                return getHashString(bytes);
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getHashString(byte[] bytes) {

        return new String(encodeHex(bytes, DIGITS_LOWER));
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    // d00c2fa9443f9d47ae0b0a35b8998522
    public static void main(String[] args) {
        String ss = "WeEuHW/EBn12nlE3JZecdqFQFiJC1rgrmBOLeMArcJk";

        System.out.println(getMD5(ss));

        System.out.println(DigestUtils.md5Hex(ss));

    }
}
