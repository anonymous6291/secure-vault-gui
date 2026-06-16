package com.securevault.gui.manager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.List;

public class SecureVaultManager {
    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String SECRET_KEY_SPEC_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int ITERATION_COUNT = 1_000_000;
    private static final int KEY_LENGTH = 128;
    private static final int TAG_LENGTH = 256;
    private static final int IV_LENGTH = 12;
    private static final int SALT_LENGTH = 16;
    private static final String EXECUTABLE_VAULT_PATH = "/home/anonymous/Desktop";
    private final String DEPENDENCY_MODE_ARGUMENT = "-d";
    private final ObjectMapper jsonHandler = new ObjectMapper();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base65Decoder = Base64.getDecoder();
    private final ProcessBuilder vaultProcessBuilder = new ProcessBuilder(EXECUTABLE_VAULT_PATH, DEPENDENCY_MODE_ARGUMENT);
    private final Process vaultProcess;
    private final BufferedInputStream vaultInputStream;
    private final BufferedOutputStream vaultOutputStream;
    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public SecureVaultManager() throws Exception {
        jsonHandler.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        vaultProcess = vaultProcessBuilder.start();
        vaultInputStream = new BufferedInputStream(vaultProcess.getInputStream());
        vaultOutputStream = new BufferedOutputStream(vaultProcess.getOutputStream());
    }

    private Cipher getCipher(int cipherMode, char[] password, byte[] iv, byte[] salt) throws Exception {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);
        KeySpec keySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), SECRET_KEY_SPEC_ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(cipherMode, secretKeySpec, gcmParameterSpec);
        return cipher;
    }

    public void run() {
    }

    record Command(CommandType type, int commandId, List<String> args) {
    }

    record Output(OutputType type, int commandId, List<String> args) {
    }

    enum OutputType {
        ERROR,
        INVALID_COMMAND,
        RESPONSE,
        QUERY,
        UPDATE_FILE_ADDED,
        UPDATE_FILE_TRANSFER_FAILED,
    }
}
/*


        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec keySpec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
        encryptCipher = Cipher.getInstance("AES/GCM/NoPadding");
        decryptCipher = Cipher.getInstance("AES/GCM/NoPadding");
 */