import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

class PasswordEntry {
    private String label;
    private String username;
    private String password;
    private String url;
    private String notes;
    private List<String> tags;

    static ArrayList<PasswordEntry> database = new ArrayList<>();
    private final static Scanner sc = new Scanner(System.in);
    private static SecretKey vaultKey;

    public PasswordEntry(String label, String username, String password, String url, String notes, List<String> tags) {
        this.label = label;
        this.username = username;
        this.password = password;
        this.url = url;
        this.notes = notes;
        this.tags = tags;
    }

    public static void setVaultKey(SecretKey key) {
        vaultKey = key;
    }

    public static void recordEntry(PasswordManager manager) {
        System.out.print("\nLabel: ");
        String label = sc.nextLine();
        System.out.print("Username: ");
        String username = sc.nextLine();
        String password = manager.capturePasswordInput(-1);
        System.out.print("URL (optional): ");
        String url = sc.nextLine();
        System.out.print("Notes (optional): ");
        String notes = sc.nextLine();
        System.out.print("Tags (comma-separated, optional): ");
        List<String> tags = parseTags(sc.nextLine());

        if (!password.isEmpty()) {
            PasswordEntry entry = new PasswordEntry(label, username, password, url, notes, tags);
            database.add(entry);
            saveAllToFile();
            System.out.println("\nNew entry recorded.");
        } else {
            System.out.println("Password field must be filled");
        }
    }

    public static void loadAllPasswords() {
        database.clear();
        if (vaultKey == null) {
            return;
        }
        String plain = VaultSecurity.loadDecryptedContent(vaultKey);
        if (plain.isEmpty()) return;

        String[] lines = plain.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split("\t", -1);
            if (parts.length < 6) continue;

            String label = decode(parts[0]);
            String username = decode(parts[1]);
            String password = decode(parts[2]);
            String url = decode(parts[3]);
            String notes = decode(parts[4]);
            String tagField = decode(parts[5]);
            List<String> tags = parseTags(tagField);
            PasswordEntry entry = new PasswordEntry(label, username, password, url, notes, tags);
            database.add(entry);
        }
    }

    public static void saveAllToFile() {
        if (vaultKey == null) return;
        StringBuilder builder = new StringBuilder();
        for (PasswordEntry entry : database) {
            builder.append(encode(entry.label)).append("\t")
                    .append(encode(entry.username)).append("\t")
                    .append(encode(entry.password)).append("\t")
                    .append(encode(entry.url)).append("\t")
                    .append(encode(entry.notes)).append("\t")
                    .append(encode(String.join(",", entry.tags)))
                    .append("\n");
        }
        VaultSecurity.saveEncryptedContent(vaultKey, builder.toString());
    }

    public static String evaluatePasswordStrength(String password) {
        int score = 0;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[^A-Za-z0-9].*")) score++;

        if (score <= 2) return "Weak";
        if (score <= 4) return "Medium";
        return "Strong";
    }

    public static boolean isPasswordReused(String password, int ignoreIndex) {
        for (int i = 0; i < database.size(); i++) {
            if (i == ignoreIndex) continue;
            if (database.get(i).password.equals(password)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> parseTags(String input) {
        if (input == null || input.trim().isEmpty()) return new ArrayList<>();
        String[] raw = input.split(",");
        List<String> tags = new ArrayList<>();
        for (String tag : raw) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) {
                tags.add(trimmed);
            }
        }
        return tags;
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    public static void closeScanner() {
        sc.close();
    }

    public String getLabel() {
        return label;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getNotes() {
        return notes;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public void setLabel(String newLabel) {
        this.label = newLabel;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String maskedPassword() {
        if (password.isEmpty()) return "";
        return "*".repeat(Math.min(password.length(), 12));
    }

    public String toDisplayString(boolean revealPassword) {
        String displayPassword = revealPassword ? password : maskedPassword();
        return "\n\tLabel: " + label +
                "\n\tUsername: " + username +
                "\n\tPassword: " + displayPassword +
                "\n\tURL: " + (url.isEmpty() ? "-" : url) +
                "\n\tNotes: " + (notes.isEmpty() ? "-" : notes) +
                "\n\tTags: " + (tags.isEmpty() ? "-" : String.join(", ", tags)) +
                "\n";
    }

    @Override
    public String toString() {
        return toDisplayString(true);
    }
}