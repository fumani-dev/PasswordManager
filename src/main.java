import javax.crypto.SecretKey;
import java.util.Scanner;

class Main {

    public static void main(String[] args) {
        PasswordManager manager = new PasswordManager();
        Scanner sc = new Scanner(System.in);

        SecretKey key = VaultSecurity.initializeAndAuthenticate(sc);
        if (key == null) {
            sc.close();
            return;
        }
        PasswordEntry.setVaultKey(key);

        // Load existing passwords from file at startup
        PasswordEntry.loadAllPasswords();

        while (true) {
            mainMenu();
            System.out.print("\nOption: ");

            try {
                int userOption = sc.nextInt();
                sc.nextLine();

                switch (userOption) {
                    case 0 -> {
                        System.out.println("Goodbye!");
                        manager.closeScanner();
                        PasswordEntry.closeScanner();
                        sc.close();
                        System.exit(0);
                    }
                    case 1 -> PasswordEntry.recordEntry(manager);
                    case 2 -> manager.viewEntries();
                    case 3 -> manager.searchByLabel();
                    case 4 -> manager.editPasswordValue();
                    case 5 -> manager.editLabelValue();
                    case 6 -> manager.editUsernameValue();
                    case 7 -> manager.deleteEntry();
                    case 8 -> manager.editMetadata();
                    case 9 -> manager.advancedSearchAndSort();
                    case 10 -> manager.exportData();
                    case 11 -> manager.importData();
                    case 12 -> manager.backupVault();
                    case 13 -> manager.restoreVault();
                    case 14 -> manager.copyCredentialToClipboard();
                    default -> System.out.println("Invalid option! Please enter 0-14.");
                }
            } catch (Exception e) {
                System.out.println("\nEnter a valid number (0-14).\n");
                sc.nextLine(); // Clear invalid input
            }
        }
    }

    static void mainMenu() {
        System.out.println("\n=== PASSWORD MANAGER ===");
        String[] options = {
                "Exit",
                "Add new entry",
                "View all entries",
                "Search by label",
                "Edit password",
                "Edit label",
                "Edit username",
                "Delete entry",
                "Edit URL/notes/tags",
                "Advanced search/sort",
                "Export data (CSV/JSON)",
                "Import data (CSV/JSON)",
                "Backup vault",
                "Restore vault",
                "Copy username/password to clipboard"
        };

        for (int i = 0; i < options.length; i++) {
            System.out.println(i + ". " + options[i]);
        }
    }
}