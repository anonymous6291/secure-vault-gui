package com.securevault.gui.manager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securevault.gui.displayable.keys.KeyType;
import com.securevault.gui.displayable.keys.Pair;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SecureVaultManager implements SecureVaultGUIListener {
    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String SECRET_KEY_SPEC_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int ITERATION_COUNT = 1_000_000;
    private static final int KEY_LENGTH = 256;
    private static final int TAG_LENGTH = 128;
    private static final int IPC_PASSWORD_LENGTH = 50;
    private static final int IV_LENGTH = 12;
    private static final int SALT_LENGTH = 16;
    private static final String EXECUTABLE_VAULT_PATH = "/home/anonymous/IdeaProjects/SecureVault/SecureVaultInstaller/bin/SecureVaultInstaller";
    private static final String OUTPUT_SEPARATOR = ";";
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Path SKIP_ROOT_PATH = Path.of("root");
    private final ConcurrentMap<Integer, CompletableFuture<Output>> commandMapping = new ConcurrentHashMap<>();
    private final AtomicInteger commandID = new AtomicInteger(1);
    private final Semaphore cipherLock = new Semaphore(1, true);
    private final Semaphore ioLock = new Semaphore(1, true);
    private final String DEPENDENCY_MODE_ARGUMENT = "-d";
    private final ObjectMapper jsonHandler = new ObjectMapper();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();
    private final ProcessBuilder vaultProcessBuilder = new ProcessBuilder(EXECUTABLE_VAULT_PATH, DEPENDENCY_MODE_ARGUMENT);
    private final Process vaultProcess;
    private final BufferedInputStream vaultInputStream;
    private final BufferedOutputStream vaultOutputStream;
    private final byte[] iv;
    private final byte[] salt;
    private final char[] ipcPassword;
    private final SecretKeySpec secretKeySpec;
    private final Cipher cipher;
    private volatile SecureVaultGUI secureVaultGUI;
    private volatile Consumer<String> failedFileTransferConsumer;

    public SecureVaultManager() throws Exception {
        jsonHandler.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        vaultProcess = vaultProcessBuilder.start();
        vaultInputStream = new BufferedInputStream(vaultProcess.getInputStream());
        vaultOutputStream = new BufferedOutputStream(vaultProcess.getOutputStream());
        iv = new byte[IV_LENGTH];
        salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        byte[] pass = new byte[IPC_PASSWORD_LENGTH];
        secureRandom.nextBytes(pass);
        ipcPassword = new String(pass).toCharArray();
        secretKeySpec = getSecretKeySpec(ipcPassword, salt);
        cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
    }

    private SecretKeySpec getSecretKeySpec(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);
        KeySpec keySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return new SecretKeySpec(secretKey.getEncoded(), SECRET_KEY_SPEC_ALGORITHM);
    }

    public void start() throws Exception {
        IPCSpec ipcSpec = new IPCSpec(ITERATION_COUNT, KEY_LENGTH, TAG_LENGTH, IV_LENGTH, SECRET_KEY_FACTORY_ALGORITHM, SECRET_KEY_SPEC_ALGORITHM, CIPHER_TRANSFORMATION, base64Encoder.encodeToString(new String(ipcPassword).getBytes()), base64Encoder.encodeToString(salt));
        writeNewLineToVaultProcess(jsonHandler.writeValueAsString(ipcSpec));
        Thread.startVirtualThread(() -> {
            List<Character> output = new LinkedList<>();
            try {
                while (vaultProcess.isAlive()) {
                    int v = vaultInputStream.read();
                    if (v == '\n') {
                        int i = 0;
                        char[] result = new char[output.size()];
                        for (char c : output) {
                            result[i++] = c;
                        }
                        processRawOutput(new String(result));
                        output.clear();
                    } else if (v != -1) {
                        output.add((char) v);
                    }
                }
            } catch (Exception e) {
                IO.println(e);
            }
        });
        SwingUtilities.invokeAndWait(() -> secureVaultGUI = new SecureVaultGUI(this));
        findVersion();
    }

    private void setCipherLock() {
        try {
            cipherLock.acquire();
        } catch (Exception _) {
        }
    }

    private void unlockCipherLock() {
        cipherLock.release();
    }

    private void setIOLock() {
        try {
            ioLock.acquire();
        } catch (Exception _) {
        }
    }

    private void unlockIOLock() {
        ioLock.release();
    }

    private void initCipher(Cipher cipher, int mode, byte[] iv) throws Exception {
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(mode, secretKeySpec, gcmParameterSpec);
    }

    private void incrementIV() {
        int n = iv.length - 1;
        while (iv[n] == 127) {
            iv[n] = 0;
            n--;
        }
        iv[n]++;
    }

    private String decryptData(String data) throws Exception {
        setCipherLock();
        int index = data.indexOf(OUTPUT_SEPARATOR);
        byte[] iv = base64Decoder.decode(data.substring(0, index));
        String encryptedData = data.substring(index + 1);
        byte[] dataBytes = base64Decoder.decode(encryptedData);
        initCipher(cipher, Cipher.DECRYPT_MODE, iv);
        String finalData = new String(cipher.doFinal(dataBytes));
        unlockCipherLock();
        return finalData;
    }

    private String encryptData(String data) throws Exception {
        setCipherLock();
        initCipher(cipher, Cipher.ENCRYPT_MODE, iv);
        byte[] dataBytes = cipher.doFinal(data.getBytes());
        String encryptedData = base64Encoder.encodeToString(dataBytes);
        String finalData = base64Encoder.encodeToString(iv) + OUTPUT_SEPARATOR + encryptedData;
        incrementIV();
        unlockCipherLock();
        return finalData;
    }

    private void writeNewLineToVaultProcess(String data) throws Exception {
        setIOLock();
        vaultOutputStream.write((data + "\n").getBytes());
        vaultOutputStream.flush();
        unlockIOLock();
    }

    private void processRawOutput(String outputData) {
        try {
            String decryptedData = decryptData(outputData);
            Output output = jsonHandler.readValue(decryptedData, Output.class);
            handleOutput(output);
        } catch (Exception e) {
            IO.println(e);
        }
    }

    private void handleOutput(Output output) {
        List<String> args = output.args();
        switch (output.type()) {
            case ERROR -> {
                IO.println(output);
                secureVaultGUI.showErrorDialog(args.getFirst());
                CompletableFuture<Output> completableFuture = commandMapping.remove(output.commandId());
                if (completableFuture != null) {
                    completableFuture.complete(output);
                }
            }
            case INVALID_COMMAND -> {
                IO.println("Error: " + output);
                CompletableFuture<Output> completableFuture = commandMapping.remove(output.commandId());
                if (completableFuture != null) {
                    completableFuture.complete(output);
                }
            }
            case RESPONSE -> {
                CompletableFuture<Output> completableFuture = commandMapping.remove(output.commandId());
                if (completableFuture != null) {
                    completableFuture.complete(output);
                }
            }
            case QUERY -> {
                String id = args.getFirst();
                String message = args.get(1);
                List<String> options = args.subList(2, args.size());
                String response = secureVaultGUI.askForQuery(message, options);
                try {
                    sendCommand(new Command(CommandType.RESPONSE, -1, List.of(id, response)));
                } catch (Exception e) {
                    IO.println(e);
                }
            }
            case UPDATE_FILE_ADDED -> {
                if (secureVaultGUI != null) {
                    args.forEach(x -> secureVaultGUI.addFile(SKIP_ROOT_PATH.relativize(Path.of(x))));
                }
            }
            case UPDATE_FILE_TRANSFER_FAILED -> {
                if (args != null) {
                    args.forEach(failedFileTransferConsumer);
                }
            }
        }
    }

    private void sendCommand(Command command) throws Exception {
        String data = jsonHandler.writeValueAsString(command);
        String encryptedData = encryptData(data);
        writeNewLineToVaultProcess(encryptedData);
    }

    private int getNewCommandID() {
        return commandID.getAndIncrement();
    }

    private Output sendCommandAndGetResponse(Command command) {
        try {
            sendCommand(command);
            CompletableFuture<Output> completableFuture = new CompletableFuture<>();
            commandMapping.put(command.commandId(), completableFuture);
            return completableFuture.get();
        } catch (Exception e) {
            IO.println(e);
        }
        return null;
    }

    private Command getNewCommand(CommandType commandType, List<String> args) {
        return new Command(commandType, getNewCommandID(), args);
    }

    private void findVersion() {
        Command command = getNewCommand(CommandType.VERSION, List.of());
        Output output = sendCommandAndGetResponse(command);
    }

    @Override
    public int getNumberOfPendingFileTransfer() {
        return 0;
    }

    @Override
    public double getFileTransferProgress() {
        return 0;
    }

    @Override
    public void registerFailedFileTransferConsumer(Consumer<String> consumer) {
        failedFileTransferConsumer = consumer;
    }

    @Override
    public void addFileToVault(Path from, Path to) {
    }

    @Override
    public void retrieveFileFromVault(Path path) {
    }

    @Override
    public void deleteFileFromVault(Path path) {
    }

    @Override
    public void renameFileFromVault(Path path, String newName) {
    }

    @Override
    public void closeVault() {
        try {
            sendCommand(new Command(CommandType.CLOSE, -1, List.of()));
        } catch (Exception _) {
        }
    }

    @Override
    public void lockdown(long duration) {
    }

    @Override
    public boolean isSelfDestructEnabled() {
        return false;
    }

    @Override
    public int getSelfDestructTries() {
        return 0;
    }

    @Override
    public void setSelfDestruct(int tries) {
    }

    @Override
    public void disableSelfDestruct() {
    }

    @Override
    public void selfDestructVault(String password) {
    }

    @Override
    public void addKey(Pair pair, String value, KeyType keyType) {
    }

    @Override
    public String getKey(Pair pair, KeyType keyType) {
        return null;
    }

    @Override
    public void deleteKey(Pair pair, KeyType keyType) {
    }

    @Override
    public boolean doLogin(Path path, String password, boolean create) {
        return true;
    }

    @Override
    public void shutdown() {
        try {
            IO.println("Shutdown");
            sendCommand(new Command(CommandType.TERMINATE, -1, List.of()));
        } catch (Exception e) {
            try {
                vaultProcess.destroyForcibly();
            } catch (Exception _) {
            }
        }
    }

    @Override
    public List<Path> getFilesList() {
        return null;
    }

    @Override
    public List<Pair> getKeysList(KeyType keyType) {
        return null;
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

    enum CommandType {
        TERMINATE,
        OPEN,
        IS_OPEN,
        CLOSE,
        RESPONSE,
        VERSION,
        CHANGE_PASSWORD,
        SELF_DESTRUCT,
        LOCKDOWN,
        SET_SELF_DESTRUCT,
        GET_SELF_DESTRUCT_TRIES,
        DISABLE_SELF_DESTRUCT,
        IS_SELF_DESTRUCT_ENABLED,
        PUT_FILE,
        PUT_DIRECTORY,
        GET_FILE,
        GET_DIRECTORY,
        GET_FILES_LIST,
        CHANGE_FILE_NAME,
        DELETE_FILE,
        MAKE_DIRECTORY,
        DELETE_DIRECTORY,
        ABORT_ALL_FILE_TRANSFERS,
        PUT_PASSWORD,
        GET_PASSWORD,
        DELETE_PASSWORD,
        SEARCH_PASSWORD,
        DELETE_ALL_PASSWORDS,
        GET_ALL_PASSWORDS,
        PUT_API_KEY,
        GET_API_KEY,
        DELETE_API_KEY,
        SEARCH_API_KEY,
        DELETE_ALL_API_KEYS,
        GET_ALL_API_KEYS,
        GET_NUMBER_OF_PENDING_FILE_TRANSFERS,
        GET_NUMBER_OF_FAILED_FILE_TRANSFERS,
        GET_FAILED_FILE_TRANSFERS_LIST,
        GET_FILE_TRANSFER_PROGRESS,
        GET_LOG,
        CLEAR_LOGS
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