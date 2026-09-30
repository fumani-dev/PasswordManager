import java.util.Scanner;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

class PasswordManager {
    private final Scanner sc = new Scanner(System.in);

    public void viewEntries() {
        if (PasswordEntry.database.isEmpty()) {
            System.out.println("No entries found.");
            return;
        }
        boolean reveal = promptReveal();
        printEntries(PasswordEntry.database, reveal);
    }

    private void printEntries(List<PasswordEntry> entries, boolean reveal) {
        System.out.println("");
        System.out.println("Saved entries (" + entries.size() + ")");
        System.out.println("=".repeat(50));

        int count = 1;
        for (PasswordEntry entry : entries) {
            System.out.println("Entry #" + count);
            System.out.println(entry.toDisplayString(reveal));
            System.out.println("-".repeat(50));
            count++;
        }
    }

    public void editLabelValue() {
        if (PasswordEntry.database.isEmpty()) {
            System.out.println("No entries to edit.");
            return;
        }
        viewEntries();
        System.out.print("\nEdit label at index (1-" + PasswordEntry.database.size() + "): ");

        try {
            int index = sc.nextInt();
            if (index < 1 || index > PasswordEntry.database.size()) {
                System.out.println("Invalid index. Valid range: 1.." + PasswordEntry.database.size());
                return;
            }

            String oldLabel = PasswordEntry.database.get(index - 1).getLabel();
            System.out.print("New label: ");
            String newLabel = sc.nextLine();

            System.out.print("\nChange label from '" + oldLabel + "' to '" + newLabel + "'? (Y/N): ");
            String confirmation = sc.nextLine().trim().toUpperCase();

            if (confirmation.equals("Y")) {
                PasswordEntry.database.get(index - 1).setLabel(newLabel);
                PasswordEntry.saveAllToFile(); // Save changes to file
                System.out.println("Label updated successfully!");
            } else {
                System.out.println("Changes cancelled!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input!");
            sc.nextLine(); // Clear buffer
        }
    }

    public void editUsernameValue() {
        if (PasswordEntry.database.isEmpty()) {
            System.out.println("No entries to edit.");
            return;
        }
        viewEntries();
        System.out.print("\nEdit username at index (1-" + PasswordEntry.database.size() + "): ");

        try {
            int index = sc.nextInt();
            if (index < 1 || index > PasswordEntry.database.size()) {
                System.out.println("Invalid index. Valid range: 1.." + PasswordEntry.database.size());
                return;
            }

            String oldUsername = PasswordEntry.database.get(index - 1).getUsername();
            System.out.print("New username: ");
            String newUsername = sc.nextLine();

            System.out.print("\nChange username from '" + oldUsername + "' to '" + newUsername + "'? (Y/N): ");

            String confirmation = sc.nextLine().trim().toUpperCase();
            if (confirmation.equals("Y")) {
                PasswordEntry.database.get(index - 1).setUsername(newUsername);
                PasswordEntry.saveAllToFile(); // Save changes to file
                System.out.println("Username updated successfully!");
            } else {
                System.out.println("Changes cancelled!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input!");
            sc.nextLine(); // Clear buffer
        }
    }

    public void editPasswordValue() {
        if (PasswordEntry.database.isEmpty()) {
            System.out.println("No entries to edit.");
            return;
        }
        viewEntries();
        System.out.print("\nEdit password at index (1-" + PasswordEntry.database.size() + "): ");

        try {
            int index = sc.nextInt();
            if (index < 1 || index > PasswordEntry.database.size()) {
                System.out.println("Invalid index. Valid range: 1.." + PasswordEntry.database.size());
                return;
            }

            String newPassword = capturePasswordInput(index - 1);
            if (newPassword.isEmpty()) {
                System.out.println("Password cannot be empty.");
                return;
            }

            System.out.print("\nChange password? (Y/N): ");
            String confirmation = sc.nextLine().trim().toUpperCase();
            if (confirmation.equals("Y")) {
                PasswordEntry.database.get(index - 1).setPassword(newPassword);
                PasswordEntry.saveAllToFile(); // Save changes to file
                System.out.println("Password updated successfully!");
            } else {
                System.out.println("Changes cancelled!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input!");
            sc.nextLine(); // Clear buffer
        }
    }

    public void searchByLabel() {
        if (PasswordEntry.database.isEmpty()) {
            System.out.println("No entries to search.");
            return;
        }

        System.out.println("\nSearch entry by label (e.g., 'Facebook'):");
        System.out.print("Search for: ");
        String search = sc.nextLine().toLowerCase();

        boolean reveal = promptReveal();

        boolean found = false;
        for (int i = 0; i < PasswordEntry.database.size(); i++) {
            PasswordEntry entry = PasswordEntry.database.get(i);
            if (entry.getLabel().toLowerCase().contains(search)) {
                System.out.println("\nEntry #" + (i + 1));
                System.out.println(entry.toDisplayString(reveal));
                System.out.println("-".repeat(50));
                found = true;
            }
        }

        if (!found) {
            System.out.println("No entry found containing '" + search + "'");
        }
    }

    public void deleteEntry() {
        if (PasswordEntry.database.isEmpty()) {
            System.out.println("No entries to delete.");
            return;
        }
        viewEntries();
        System.out.print("\nDelete entry at index (1-" + PasswordEntry.database.size() + "): ");

        try {
            int index = sc.nextInt();
            if (index < 1 || index > PasswordEntry.database.size()) {
                System.out.println("Invalid index!");
                return;
            }

            PasswordEntry entry = PasswordEntry.database.get(index - 1);
            System.out.print("Delete entry '" + entry.getLabel() + "'? (Y/N): ");
            String confirmation = sc.nextLine().trim().toUpperCase();

            if (confirmation.equals("Y")) {
                PasswordEntry.database.remove(index - 1);
                PasswordEntry.saveAllToFile();
                System.out.println("Entry deleted successfully!");
            } else {
                System.out.println("Deletion cancelled!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input!");
            sc.nextLine();
        }
    }

    public String capturePasswordInput(int ignoreIndex) {
        System.out.print("Generate password? (Y/N): ");
        String generate = sc.nextLine().trim().toUpperCase();
        String password;
        if (generate.equals("Y")) {
            try {
                System.out.print("Length (min 4): ");
                int length = Integer.parseInt(sc.nextLine());
                System.out.print("Include uppercase? (Y/N): ");
                boolean upper = sc.nextLine().trim().equalsIgnoreCase("Y");
                System.out.print("Include lowercase? (Y/N): ");
                boolean lower = sc.nextLine().trim().equalsIgnoreCase("Y");
                System.out.print("Include numbers? (Y/N): ");
                boolean digits = sc.nextLine().trim().equalsIgnoreCase("Y");
                System.out.print("Include symbols? (Y/N): ");
                boolean symbols = sc.nextLine().trim().equalsIgnoreCase("Y");
                password = PasswordGenerator.generate(length, upper, lower, digits, symbols);
                System.out.println("Generated password: " + password);
            } catch (Exception e) {
                System.out.println("Failed to generate password: " + e.getMessage());
                return "";
            }
        } else {
            System.out.print("Password: ");
            password = sc.nextLine();
        }

        String strength = PasswordEntry.evaluatePasswordStrength(password);
        System.out.println("Password strength: " + strength);
        if (strength.equals("Weak")) {
            System.out.println("Warning: this password is weak.");
        }

        if (PasswordEntry.isPasswordReused(password, ignoreIndex)) {
            System.out.println("Warning: this password is already used in another entry.");
        }

        return password;
    }

    private boolean promptReveal() {
        System.out.print("Reveal passwords? (Y/N): ");
        return sc.nextLine().trim().equalsIgnoreCase("Y");
    }

    public void editMetadata() {
        if (PasswordEntry.database.isEmpty()) {
            System.out.println("No entries to edit.");
            return;
        }
        viewEntries();
        System.out.print("\nEdit metadata at index (1-" + PasswordEntry.database.size() + "): ");
        try {
            int index = sc.nextInt();
            sc.nextLine();
            if (index < 1 || index > PasswordEntry.database.size()) {
                System.out.println("Invalid index.");
                return;
            }
            PasswordEntry entry = PasswordEntry.database.get(index - 1);
            System.out.print("New URL (blank to keep current): ");
            String url = sc.nextLine();
            System.out.print("New notes (blank to keep current): ");
            String notes = sc.nextLine();
            System.out.print("New tags (comma-separated, blank to keep current): ");
            String tagsInput = sc.nextLine();

            if (!url.isBlank()) entry.setUrl(url);
            if (!notes.isBlank()) entry.setNotes(notes);
            if (!tagsInput.isBlank()) entry.setTags(PasswordEntry.parseTags(tagsInput));
            PasswordEntry.saveAllToFile();
            System.out.println("Metadata updated.");
        } catch (Exception e) {
            System.out.println("Invalid input.");
            sc.nextLine();
        }
    }

    public void advancedSearchAndSort() {
        if (PasswordEntry.database.isEmpty()) {
            System.out.println("No entries to search.");
            return;
        }
        System.out.println("\nSearch by:");
        System.out.println("1. Label");
        System.out.println("2. Username");
        System.out.println("3. Tag");
        System.out.print("Option: ");
        String mode = sc.nextLine();
        System.out.print("Query: ");
        String query = sc.nextLine().toLowerCase();

        List<PasswordEntry> results = new ArrayList<>();
        for (PasswordEntry entry : PasswordEntry.database) {
            boolean match = switch (mode) {
                case "1" -> entry.getLabel().toLowerCase().contains(query);
                case "2" -> entry.getUsername().toLowerCase().contains(query);
                case "3" -> entry.getTags().stream().anyMatch(t -> t.toLowerCase().contains(query));
                default -> false;
            };
            if (match) results.add(entry);
        }

        if (results.isEmpty()) {
            System.out.println("No matching entries.");
            return;
        }

        System.out.println("Sort by: 1.Label 2.Username");
        String sort = sc.nextLine();
        if (sort.equals("1")) {
            results = results.stream().sorted(Comparator.comparing(PasswordEntry::getLabel, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
        } else if (sort.equals("2")) {
            results = results.stream().sorted(Comparator.comparing(PasswordEntry::getUsername, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
        }
        printEntries(results, promptReveal());
    }

    public void exportData() {
        if (PasswordEntry.database.isEmpty()) {
            System.out.println("No entries to export.");
            return;
        }
        try {
            System.out.println("Export format: 1.CSV 2.JSON");
            String format = sc.nextLine();
            System.out.print("Output file path: ");
            String path = sc.nextLine();
            if (format.equals("1")) {
                exportCsv(path);
            } else if (format.equals("2")) {
                exportJson(path);
            } else {
                System.out.println("Invalid format.");
                return;
            }
            System.out.println("Export complete.");
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    public void importData() {
        try {
            System.out.println("Import format: 1.CSV 2.JSON");
            String format = sc.nextLine();
            System.out.print("Input file path: ");
            String path = sc.nextLine();
            int before = PasswordEntry.database.size();
            if (format.equals("1")) {
                importCsv(path);
            } else if (format.equals("2")) {
                importJson(path);
            } else {
                System.out.println("Invalid format.");
                return;
            }
            PasswordEntry.saveAllToFile();
            System.out.println("Imported " + (PasswordEntry.database.size() - before) + " entries.");
        } catch (Exception e) {
            System.out.println("Import failed: " + e.getMessage());
        }
    }

    public void backupVault() {
        try {
            System.out.print("Backup file prefix (e.g., backup/myvault): ");
            String prefix = sc.nextLine();
            VaultSecurity.backupVault(prefix);
            System.out.println("Backup created.");
        } catch (Exception e) {
            System.out.println("Backup failed: " + e.getMessage());
        }
    }

    public void restoreVault() {
        try {
            System.out.print("Backup file prefix to restore: ");
            String prefix = sc.nextLine();
            VaultSecurity.restoreVault(prefix);
            PasswordEntry.loadAllPasswords();
            System.out.println("Vault restored.");
        } catch (Exception e) {
            System.out.println("Restore failed: " + e.getMessage());
        }
    }

    public void copyCredentialToClipboard() {
        if (PasswordEntry.database.isEmpty()) {
            System.out.println("No entries available.");
            return;
        }
        viewEntries();
        try {
            System.out.print("\nSelect entry index: ");
            int index = sc.nextInt();
            sc.nextLine();
            if (index < 1 || index > PasswordEntry.database.size()) {
                System.out.println("Invalid index.");
                return;
            }
            PasswordEntry entry = PasswordEntry.database.get(index - 1);
            System.out.print("Copy 1.Username or 2.Password: ");
            String type = sc.nextLine();
            System.out.print("Auto-clear timeout seconds: ");
            int seconds = Integer.parseInt(sc.nextLine());
            String value = type.equals("1") ? entry.getUsername() : entry.getPassword();
            boolean ok = ClipboardUtil.copyWithAutoClear(value, seconds);
            if (ok) {
                System.out.println("Copied to clipboard and will auto-clear.");
            } else {
                System.out.println("Clipboard unavailable in this environment.");
            }
        } catch (Exception e) {
            System.out.println("Copy failed: " + e.getMessage());
            sc.nextLine();
        }
    }

    private void exportCsv(String path) throws Exception {
        StringBuilder sb = new StringBuilder("label,username,password,url,notes,tags\n");
        for (PasswordEntry entry : PasswordEntry.database) {
            sb.append(csvEscape(entry.getLabel())).append(",")
                    .append(csvEscape(entry.getUsername())).append(",")
                    .append(csvEscape(entry.getPassword())).append(",")
                    .append(csvEscape(entry.getUrl())).append(",")
                    .append(csvEscape(entry.getNotes())).append(",")
                    .append(csvEscape(String.join("|", entry.getTags())))
                    .append("\n");
        }
        Files.writeString(Path.of(path), sb.toString(), StandardCharsets.UTF_8);
    }

    private void importCsv(String path) throws Exception {
        List<String> lines = Files.readAllLines(Path.of(path), StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            List<String> parts = parseCsvLine(lines.get(i));
            if (parts.size() < 6) continue;
            PasswordEntry.database.add(new PasswordEntry(
                    parts.get(0), parts.get(1), parts.get(2), parts.get(3), parts.get(4),
                    PasswordEntry.parseTags(parts.get(5).replace("|", ","))
            ));
        }
    }

    private void exportJson(String path) throws Exception {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < PasswordEntry.database.size(); i++) {
            PasswordEntry entry = PasswordEntry.database.get(i);
            sb.append("  {")
                    .append("\"label\":\"").append(jsonEscape(entry.getLabel())).append("\",")
                    .append("\"username\":\"").append(jsonEscape(entry.getUsername())).append("\",")
                    .append("\"password\":\"").append(jsonEscape(entry.getPassword())).append("\",")
                    .append("\"url\":\"").append(jsonEscape(entry.getUrl())).append("\",")
                    .append("\"notes\":\"").append(jsonEscape(entry.getNotes())).append("\",")
                    .append("\"tags\":\"").append(jsonEscape(String.join(",", entry.getTags()))).append("\"")
                    .append("}");
            if (i < PasswordEntry.database.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        Files.writeString(Path.of(path), sb.toString(), StandardCharsets.UTF_8);
    }

    private void importJson(String path) throws Exception {
        String content = Files.readString(Path.of(path), StandardCharsets.UTF_8).trim();
        if (content.length() < 2) return;
        String body = content.substring(1, content.length() - 1).trim();
        if (body.isEmpty()) return;
        String[] objects = body.split("\\},\\s*\\{");
        for (String object : objects) {
            String normalized = object.replace("{", "").replace("}", "");
            String label = extractJsonValue(normalized, "label");
            String username = extractJsonValue(normalized, "username");
            String password = extractJsonValue(normalized, "password");
            String url = extractJsonValue(normalized, "url");
            String notes = extractJsonValue(normalized, "notes");
            String tags = extractJsonValue(normalized, "tags");
            PasswordEntry.database.add(new PasswordEntry(label, username, password, url, notes, PasswordEntry.parseTags(tags)));
        }
    }

    private String extractJsonValue(String raw, String key) {
        String marker = "\"" + key + "\":\"";
        int start = raw.indexOf(marker);
        if (start == -1) return "";
        start += marker.length();
        int end = raw.indexOf("\"", start);
        if (end == -1) return "";
        return raw.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String csvEscape(String input) {
        String safe = input == null ? "" : input;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        columns.add(current.toString());
        return columns;
    }

    private String jsonEscape(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public void closeScanner() {
        sc.close();
    }
}