package com.quizsimulator.view;

import java.util.Scanner;

public class AuthView {
    private final Scanner scanner;

    public AuthView(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readMenuChoice() {
        while (true) {
            System.out.println("1. Login");
            System.out.println("2. Register as professor");
            System.out.println("3. Register as student");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            if (!scanner.hasNextLine()) {
                return 4;
            }

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": return 1;
                case "2": return 2;
                case "3": return 3;
                case "4": return 4;
                default: showMessage("Please enter 1, 2, 3, or 4.");
            }
        }
    }

    public String readName() {
        return readRequired("Name: ", true);
    }

    public String readEmail() {
        return readRequired("Email: ", true);
    }

    public String readPassword() {
        return readRequired("Password: ", false);
    }

    public String readPasswordConfirmation() {
        return readRequired("Confirm password: ", false);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    private String readRequired(String prompt, boolean trim) {
        while (true) {
            System.out.print(prompt + "(:back to cancel) ");
            if (!scanner.hasNextLine()) {
                return null;
            }

            String input = scanner.nextLine();
            if (":back".equals(trim ? input.trim() : input)) {
                return null;
            }
            if (!input.isBlank()) {
                return trim ? input.trim() : input;
            }
            showMessage("This field is required.");
        }
    }
}
