package com.securevault.gui.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securevault.gui.displayable.keys.KeyType;
import com.securevault.gui.displayable.keys.WebsiteIdPair;
import com.securevault.gui.manager.listeners.SecureVaultGUIListener;

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
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String SECRET_KEY_SPEC_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int ITERATION_COUNT = 1_000_000;
    private static final int KEY_LENGTH = 256;
    private static final int TAG_LENGTH = 128;
    private static final int IPC_PASSWORD_LENGTH = 50;
    private static final int IV_LENGTH = 12;
    private static final int SALT_LENGTH = 16;
    private static final String EXECUTABLE_VAULT_PATH = "./SecureVault";
    private static final String DEPENDENCY_MODE_ARGUMENT = "-d";
    private static final String OUTPUT_SEPARATOR = ";";
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Path SKIP_ROOT_PATH = Path.of("root");
    private final ConcurrentMap<Integer, CompletableFuture<Input>> commandMapping = new ConcurrentHashMap<>();
    private final AtomicInteger commandID = new AtomicInteger(1);
    private final Semaphore cipherLock = new Semaphore(1, true);
    private final Semaphore ioLock = new Semaphore(1, true);
    private final ObjectMapper jsonHandler = new ObjectMapper();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();
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
        ProcessBuilder vaultProcessBuilder = new ProcessBuilder(EXECUTABLE_VAULT_PATH, DEPENDENCY_MODE_ARGUMENT);
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
                int lineSeparatorLength = LINE_SEPARATOR.length();
                char lineSeparatorEndingChar = LINE_SEPARATOR.charAt(lineSeparatorLength - 1);
                while (vaultProcess.isAlive()) {
                    int v = vaultInputStream.read();
                    if (v == lineSeparatorEndingChar) {
                        int i = 0;
                        char[] result = new char[output.size()];
                        for (char c : output) {
                            result[i++] = c;
                        }
                        String input = new String(result, 0, i - (lineSeparatorLength - 1));
                        Thread.startVirtualThread(() -> processRawOutput(input));
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
        try {
            int index = data.indexOf(OUTPUT_SEPARATOR);
            byte[] iv = base64Decoder.decode(data.substring(0, index));
            String encryptedData = data.substring(index + 1);
            byte[] dataBytes = base64Decoder.decode(encryptedData);
            initCipher(cipher, Cipher.DECRYPT_MODE, iv);
            return new String(cipher.doFinal(dataBytes));
        } finally {
            unlockCipherLock();
        }
    }

    private String encryptData(String data) throws Exception {
        setCipherLock();
        try {
            initCipher(cipher, Cipher.ENCRYPT_MODE, iv);
            byte[] dataBytes = cipher.doFinal(data.getBytes());
            String encryptedData = base64Encoder.encodeToString(dataBytes);
            String finalData = base64Encoder.encodeToString(iv) + OUTPUT_SEPARATOR + encryptedData;
            incrementIV();
            return finalData;
        } finally {
            unlockCipherLock();
        }
    }

    private void writeNewLineToVaultProcess(String data) throws Exception {
        setIOLock();
        try {
            vaultOutputStream.write((data + "\n").getBytes());
            vaultOutputStream.flush();
        } finally {
            unlockIOLock();
        }
    }

    private void processRawOutput(String outputData) {
        try {
            String decryptedData = decryptData(outputData);
            Input input = jsonHandler.readValue(decryptedData, Input.class);
            handleOutput(input);
        } catch (Exception e) {
            IO.println(e);
        }
    }

    private void handleOutput(Input input) {
        List<String> args = input.args();
        switch (input.type()) {
            case ERROR, RESPONSE -> {
                CompletableFuture<Input> completableFuture = commandMapping.remove(input.commandId());
                if (completableFuture != null) {
                    completableFuture.complete(input);
                }
            }
            case INVALID_COMMAND -> {
                CompletableFuture<Input> completableFuture = commandMapping.remove(input.commandId());
                if (completableFuture != null) {
                    completableFuture.complete(null);
                }
            }
            case QUERY -> {
                String id = args.getFirst();
                String message = args.get(1);
                List<String> options = args.subList(2, args.size());
                String response = secureVaultGUI.askForQuery(message, options);
                try {
                    Command command = new Command(CommandType.RESPONSE, -1, List.of(id, response));
                    sendCommand(command);
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

    private void sendResponselessCommand(CommandType commandType, List<String> args) {
        try {
            sendCommand(new Command(commandType, -1, args));
        } catch (Exception e) {
            IO.println(e);
        }
    }

    private Input sendResponseCommand(CommandType commandType, List<String> args) {
        try {
            Command command = new Command(commandType, getNewCommandID(), args);
            CompletableFuture<Input> completableFuture = new CompletableFuture<>();
            commandMapping.put(command.commandId(), completableFuture);
            sendCommand(command);
            return completableFuture.get();
        } catch (Exception e) {
            IO.println(e);
        }
        return null;
    }

    private void showErrorMessage(String message) {
        secureVaultGUI.showErrorDialog(message);
    }

    private boolean isNormalInput(Input input) {
        return !(input == null || input.type() == InputType.ERROR);
    }

    @Override
    public boolean changeVaultPassword(String oldPassword, String newPassword) {
        try {
            Input input = sendResponseCommand(CommandType.CHANGE_PASSWORD, List.of(oldPassword, newPassword));
            if (isNormalInput(input)) {
                return Boolean.parseBoolean(input.args().getFirst());
            }
            if (input != null) {
                showErrorMessage(input.args().getFirst());
            }
        } catch (Exception e) {
            IO.println(e);
        }
        return false;
    }

    @Override
    public String getLogs() {
        Input input = sendResponseCommand(CommandType.GET_LOG, List.of("10000"));
        if (isNormalInput(input)) {
            return input.args().getFirst();
        }
        return "";
    }

    @Override
    public void clearLogs() {
        sendResponselessCommand(CommandType.CLEAR_LOGS, List.of());
    }

    @Override
    public int getNumberOfPendingFileTransfer() {
        try {
            Input input = sendResponseCommand(CommandType.GET_NUMBER_OF_PENDING_FILE_TRANSFERS, List.of());
            if (isNormalInput(input)) {
                return Integer.parseInt(input.args().getFirst());
            }
        } catch (Exception e) {
            IO.println(e);
        }
        return 0;
    }

    @Override
    public double getFileTransferProgress() {
        try {
            Input input = sendResponseCommand(CommandType.GET_FILE_TRANSFER_PROGRESS, List.of());
            if (isNormalInput(input)) {
                return Double.parseDouble(input.args().getFirst());
            }
        } catch (Exception e) {
            IO.println(e);
        }
        return 0;
    }

    @Override
    public void registerFailedFileTransferConsumer(Consumer<String> consumer) {
        failedFileTransferConsumer = consumer;
    }

    @Override
    public void abortAllFileTransfers() {
        sendResponselessCommand(CommandType.ABORT_ALL_FILE_TRANSFERS, List.of());
    }

    @Override
    public void addFileToVault(Path from, Path to) {
        sendResponselessCommand(CommandType.PUT_FILE, List.of(from.toString(), to.toString()));
    }

    @Override
    public void retrieveFileFromVault(Path from, Path to) {
        sendResponselessCommand(CommandType.GET_FILE, List.of(from.toString(), to.toString()));
    }

    @Override
    public void deleteFileFromVault(Path path) {
        sendResponselessCommand(CommandType.DELETE_FILE, List.of(path.toString()));
    }

    @Override
    public String renameFileFromVault(Path path, String newName) {
        Input input = sendResponseCommand(CommandType.CHANGE_FILE_NAME, List.of(path.toString(), newName));
        try {
            if (isNormalInput(input)) {
                if (Boolean.parseBoolean(input.args().getFirst())) {
                    return input.args().get(1);
                }
            }
        } catch (Exception _) {
        }
        return null;
    }

    @Override
    public void closeVault() {
        Input input = sendResponseCommand(CommandType.CLOSE, List.of());
        if (isNormalInput(input)) {
            secureVaultGUI.showLoginPage();
        }
    }

    @Override
    public void lockdown(long duration) {
        Input input = sendResponseCommand(CommandType.LOCKDOWN, List.of(Long.toString(duration)));
        if (isNormalInput(input)) {
            secureVaultGUI.showLoginPage();
        }
    }

    @Override
    public boolean isSelfDestructEnabled() {
        try {
            Input input = sendResponseCommand(CommandType.IS_SELF_DESTRUCT_ENABLED, List.of());
            if (isNormalInput(input)) {
                return Boolean.parseBoolean(input.args().getFirst());
            }
        } catch (Exception e) {
            IO.println(e);
        }
        return false;
    }

    @Override
    public int getSelfDestructTries() {
        try {
            Input input = sendResponseCommand(CommandType.GET_SELF_DESTRUCT_TRIES, List.of());
            if (isNormalInput(input)) {
                return Integer.parseInt(input.args().getFirst());
            }
        } catch (Exception e) {
            IO.println(e);
        }
        return 0;
    }

    @Override
    public void setSelfDestruct(int tries) {
        sendResponselessCommand(CommandType.SET_SELF_DESTRUCT, List.of(Integer.toString(tries)));
    }

    @Override
    public void disableSelfDestruct() {
        sendResponselessCommand(CommandType.DISABLE_SELF_DESTRUCT, List.of());
    }

    @Override
    public void selfDestructVault(String password) {
        Input input = sendResponseCommand(CommandType.SELF_DESTRUCT, List.of(password));
        if (isNormalInput(input)) {
            secureVaultGUI.showLoginPage();
        } else if (input != null) {
            showErrorMessage(input.args().getFirst());
        }
    }

    @Override
    public void addKey(WebsiteIdPair websiteIdPair, String value, KeyType keyType) {
        try {
            CommandType commandType;
            if (keyType == KeyType.PASSWORD) {
                commandType = CommandType.PUT_PASSWORD;
            } else {
                commandType = CommandType.PUT_API_KEY;
            }
            sendResponselessCommand(commandType, List.of(jsonHandler.writeValueAsString(websiteIdPair), value));
        } catch (Exception e) {
            IO.println(e);
        }
    }

    @Override
    public String getKey(WebsiteIdPair websiteIdPair, KeyType keyType) {
        try {
            CommandType commandType = keyType == KeyType.PASSWORD ? CommandType.GET_PASSWORD : CommandType.GET_API_KEY;
            Input input = sendResponseCommand(commandType, List.of(jsonHandler.writeValueAsString(websiteIdPair)));
            if (isNormalInput(input)) {
                return input.args().getFirst();
            }
        } catch (Exception e) {
            IO.println(e);
        }
        return null;
    }

    @Override
    public void deleteKey(WebsiteIdPair websiteIdPair, KeyType keyType) {
        CommandType commandType;
        if (keyType == KeyType.PASSWORD) {
            commandType = CommandType.DELETE_PASSWORD;
        } else {
            commandType = CommandType.DELETE_API_KEY;
        }
        try {
            sendResponselessCommand(commandType, List.of(jsonHandler.writeValueAsString(websiteIdPair)));
        } catch (Exception e) {
            IO.println(e);
        }
    }

    @Override
    public boolean doLogin(Path path, String password, boolean create) {
        try {
            Input input = sendResponseCommand(CommandType.OPEN, List.of(path.toString(), Boolean.toString(create), password));
            if (isNormalInput(input)) {
                return Boolean.parseBoolean(input.args().getFirst());
            }
            if (input != null) {
                showErrorMessage(input.args.getFirst());
            }
        } catch (Exception e) {
            IO.println(e);
        }
        return false;
    }

    @Override
    public String getVersion() {
        Input input = sendResponseCommand(CommandType.VERSION, List.of());
        if (isNormalInput(input)) {
            return input.args().getFirst();
        }
        return "";
    }

    @Override
    public void shutdown() {
        sendResponseCommand(CommandType.TERMINATE, List.of());
    }

    @Override
    public List<Path> getFilesList() {
        try {
            Input input = sendResponseCommand(CommandType.GET_FILES_LIST, List.of(""));
            if (isNormalInput(input)) {
                return input.args().stream().map(x -> SKIP_ROOT_PATH.relativize(Path.of(x))).toList();
            }
        } catch (Exception e) {
            IO.println(e);
        }
        return List.of();
    }

    @Override
    public List<WebsiteIdPair> getKeysList(KeyType keyType) {
        try {
            CommandType commandType;
            if (keyType == KeyType.PASSWORD) {
                commandType = CommandType.GET_ALL_PASSWORDS;
            } else {
                commandType = CommandType.GET_ALL_API_KEYS;
            }
            Input input = sendResponseCommand(commandType, List.of());
            if (isNormalInput(input)) {
                return input.args().stream().map(x -> {
                    try {
                        return jsonHandler.readValue(x, WebsiteIdPair.class);
                    } catch (JsonProcessingException e) {
                        IO.println(e);
                        return null;
                    }
                }).toList();
            }
        } catch (Exception e) {
            IO.println(e);
        }
        return List.of();
    }

    public enum InputType {
        ERROR,
        INVALID_COMMAND,
        RESPONSE,
        QUERY,
        UPDATE_FILE_ADDED,
        UPDATE_FILE_TRANSFER_FAILED,
    }

    public enum CommandType {
        TERMINATE,
        OPEN,
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
        GET_FILE,
        GET_FILES_LIST,
        CHANGE_FILE_NAME,
        DELETE_FILE,
        ABORT_ALL_FILE_TRANSFERS,
        PUT_PASSWORD,
        GET_PASSWORD,
        DELETE_PASSWORD,
        SEARCH_PASSWORD,
        GET_ALL_PASSWORDS,
        PUT_API_KEY,
        GET_API_KEY,
        DELETE_API_KEY,
        SEARCH_API_KEY,
        GET_ALL_API_KEYS,
        GET_NUMBER_OF_PENDING_FILE_TRANSFERS,
        GET_FILE_TRANSFER_PROGRESS,
        GET_LOG,
        CLEAR_LOGS
    }

    public record Command(CommandType type, int commandId, List<String> args) {
    }

    public record Input(InputType type, int commandId, List<String> args) {
    }
}