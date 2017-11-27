package net.seanamos.pbkdf2;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class PasswordPrefs {

    private static final int ITERATION_COUNT = 256000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = KEY_LENGTH / 8;

    private final SharedPreferences prefs;

    public PasswordPrefs(Context context) {
        prefs = context.getSharedPreferences("Crypto", Context.MODE_PRIVATE);
    }

    private SecretKey deriveKey(byte[] salt, String password) throws GeneralSecurityException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] bytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(bytes, "AES");
    }

    public void encrypt(String prefsKey, String secret, String password) throws GeneralSecurityException, UnsupportedEncodingException {
        SecureRandom random = new SecureRandom();
        // rando salt
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        SecretKey key = deriveKey(salt, password);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // rando iv
        byte[] iv = new byte[cipher.getBlockSize()];
        random.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        // crypt it
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
        byte[] encrypted = cipher.doFinal(secret.getBytes("UTF-8"));
        storeWithParams(prefsKey, encrypted, salt, iv);
    }

    public String decrypt(String prefsKey, String password) throws GeneralSecurityException, UnsupportedEncodingException {
        CryptoBlob blob = fetchWithParams(prefsKey);
        if (blob == null)
            return null;
        // recreate the key used to encrypt (hopefully lol)
        SecretKey key = deriveKey(blob.salt, password);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParams = new IvParameterSpec(blob.iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
        byte[] plainText = cipher.doFinal(blob.encrypted);
        return new String(plainText, "UTF-8");
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    private void storeWithParams(String prefsKey, byte[] encrypted, byte[] salt, byte[] iv) {
        String toSave = Base64.encodeToString(encrypted, Base64.DEFAULT) + "]" +
                Base64.encodeToString(salt, Base64.DEFAULT) + "]" +
                Base64.encodeToString(iv, Base64.DEFAULT);
        prefs.edit().putString(prefsKey, toSave).apply();
    }

    private CryptoBlob fetchWithParams(String prefsKey) {
        String stored = prefs.getString(prefsKey, null);
        if (stored == null)
            return null;
        String[] fields = stored.split("]");
        byte[] encrypted = Base64.decode(fields[0], Base64.DEFAULT);
        byte[] salt = Base64.decode(fields[1], Base64.DEFAULT);
        byte[] iv = Base64.decode(fields[2], Base64.DEFAULT);
        return new CryptoBlob(encrypted, salt, iv);
    }

    private class CryptoBlob {
        public final byte[] encrypted;
        public final byte[] salt;
        public final byte[] iv;
        public CryptoBlob(byte[] encrypted, byte[] salt, byte[] iv) {
            this.encrypted = encrypted;
            this.salt = salt;
            this.iv = iv;
        }
    }
}
