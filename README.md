# 🔐 Password Manager

Command-line password manager in Java with encrypted local vault storage.

## ✨ Features

- Master password onboarding and vault unlock
- AES-GCM encrypted storage at rest (`passwords.vault`)
- Add/edit/delete entries with label, username, password, URL, notes, and tags
- Built-in password generator (length and character set options)
- Password strength checks and reuse warnings
- Hide/reveal password display flow
- Basic and advanced search (label/username/tag) + sorting
- CSV/JSON import and export
- Vault backup/restore
- Clipboard copy for username/password with auto-clear timeout

## 🚀 Run

From `/home/runner/work/PasswordManager/PasswordManager/src`:

```bash
javac Main.java PasswordManager.java PasswordEntry.java VaultSecurity.java PasswordGenerator.java ClipboardUtil.java
java Main
```

## 📁 Storage Files

- `vault.meta` – salt + verifier for master-password-based key derivation
- `passwords.vault` – encrypted vault payload

Optional data exchange files:
- CSV/JSON files created by export/import features
- `*.vault` and `*.meta` backup copies from backup command
