# 🔐 Password Manager

A comprehensive command-line password management application built in Java. Store, view, search, edit, and manage your passwords with persistent file-based storage.

## ✨ Features

- **Add New Entries** – Store passwords with labels and usernames
- **View All Entries** – Display all saved passwords with numbered entries
- **Search by Label** – Quickly find entries using partial or full label matching
- **Edit Entries** – Modify passwords, labels, or usernames with confirmation prompts
- **Delete Entries** – Remove unwanted entries with safety confirmation
- **Persistent Storage** – Automatically saves to `passwords.txt` on every change
- **Input Validation** – Handles invalid inputs gracefully without crashing
- **Case-Insensitive Search** – Search for labels regardless of case

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 14 or higher
- Any terminal/command prompt

### Build and Run

From the repository root, compile the Java sources:

```bash
javac -d out src/*.java
```

Run the application:

```bash
java -cp out Main
```

The application loads and saves password records in `passwords.txt` in the
current working directory. This file is ignored by Git because it contains
plain-text credential data.

## 🗂️ Project Structure

- `src/Main.java` - Starts the application and displays the menu
- `src/PasswordManager.java` - Handles viewing, searching, editing, and deleting entries
- `src/PasswordEntry.java` - Represents entries and manages file persistence
