import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

class VaultSecurity {
    private static final String META_FILE = "vault.meta";
    private static final String VAULT_FILE = "passwords.vault";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static SecretKey initializeAndAuthenticate(Scanner sc) {
        File meta = new File(META_FILE);
        if (!meta.exists()) {
            return createMasterPassword(sc);
        }
        return unlockVault(sc);
    }

    private static SecretKey createMasterPassword(Scanner sc) {
        System.out.println("No vault found. Create a master password.");
        while (true) {
            System.out.print("Master password: ");
            String password = sc.nextLine();
            System.out.print("Confirm master password: ");
            String confirm = sc.nextLine();

            if (!password.equals(confirm)) {
                System.out.println("Passwords do not match. Try again.");
                continue;
            }
            if (password.trim().isEmpty()) {
                System.out.println("Master password cannot be empty.");
                continue;
            }

            try {
                byte[] salt = new byte[16];
                RANDOM.nextBytes(salt);
                SecretKey key = deriveKey(password, salt);
                String hash = hashHex(key.getEncoded());
                String content = Base64.getEncoder().encodeToString(salt) + ":" + hash;
                Files.writeString(Path.of(META_FILE), content, StandardCharsets.UTF_8);
                saveEncryptedContent(key, "");
                System.out.println("Vault created.");
                return key;
            } catch (Exception e) {
                System.out.println("Error creating vault: " + e.getMessage());
                return null;
            }
        }
    }

    private static SecretKey unlockVault(Scanner sc) {
        try {
            String[] parts = Files.readString(Path.of(META_FILE), StandardCharsets.UTF_8).trim().split(":");
            if (parts.length != 2) {
                System.out.println("Vault metadata is corrupted.");
                return null;
            }
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            String expectedHash = parts[1];

            for (int attempt = 0; attempt < 3; attempt++) {
                System.out.print("Enter master password: ");
                String password = sc.nextLine();
                SecretKey key = deriveKey(password, salt);
                String candidate = hashHex(key.getEncoded());
                if (candidate.equals(expectedHash)) {
                    return key;
                }
                System.out.println("Incorrect master password.");
            }
            System.out.println("Too many failed attempts.");
        } catch (Exception e) {
            System.out.println("Error unlocking vault: " + e.getMessage());
        }
        return null;
    }

    public static String loadDecryptedContent(SecretKey key) {
        File file = new File(VAULT_FILE);
        if (!file.exists()) {
            saveEncryptedContent(key, "");
            return "";
        }

        try {
            String data = Files.readString(Path.of(VAULT_FILE), StandardCharsets.UTF_8).trim();
            if (data.isEmpty()) {
                return "";
            }
            String[] parts = data.split(":", 2);
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherText = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Error decrypting vault: " + e.getMessage());
            return "";
        }
    }

    public static void saveEncryptedContent(SecretKey key, String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            String payload = Base64.getEncoder().encodeToString(iv) + ":" +
                    Base64.getEncoder().encodeToString(encrypted);
            Files.writeString(Path.of(VAULT_FILE), payload, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Error saving encrypted vault: " + e.getMessage());
        }
    }

    public static void backupVault(String backupPrefix) throws IOException {
        Path sourceVault = Path.of(VAULT_FILE);
        Path sourceMeta = Path.of(META_FILE);
        Files.copy(sourceVault, Path.of(backupPrefix + ".vault"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceMeta, Path.of(backupPrefix + ".meta"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    public static void restoreVault(String backupPrefix) throws IOException {
        Path backupVault = Path.of(backupPrefix + ".vault");
        Path backupMeta = Path.of(backupPrefix + ".meta");
        Files.copy(backupVault, Path.of(VAULT_FILE), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.copy(backupMeta, Path.of(META_FILE), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static String hashHex(byte[] value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
