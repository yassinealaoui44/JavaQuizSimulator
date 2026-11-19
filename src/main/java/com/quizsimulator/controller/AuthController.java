package com.quizsimulator.controller;

import com.quizsimulator.model.Role;
import com.quizsimulator.model.User;
import com.quizsimulator.service.AuthService;
import com.quizsimulator.view.AuthView;

public class AuthController {
    private final AuthView view;
    private final AuthService service;
    private final ProfessorController professorController;
    private final StudentController studentController;

    public AuthController(AuthView view, AuthService service,
                          ProfessorController professorController, StudentController studentController) {
        this.view = view;
        this.service = service;
        this.professorController = professorController;
        this.studentController = studentController;
    }

    public void run() {
        while (true) {
            int choice = view.readMenuChoice();
            try {
                switch (choice) {
                    case 1 -> login();
                    case 2 -> register(Role.PROFESSOR);
                    case 3 -> register(Role.STUDENT);
                    case 4 -> {
                        view.showMessage("Goodbye.");
                        return;
                    }
                }
            } catch (IllegalArgumentException exception) {
                view.showMessage(exception.getMessage());
            }
        }
    }

    private void login() {
        String email = view.readEmail();
        if (email == null) {
            return;
        }
        String password = view.readPassword();
        if (password == null) {
            return;
        }

        User user = service.login(email, password);
        view.showMessage("Welcome, " + user.getName() + ".");
        switch (user.getRole()) {
            case PROFESSOR -> professorController.run(user);
            case STUDENT -> studentController.run(user);
        }
    }

    private void register(Role role) {
        String name = view.readName();
        if (name == null) {
            return;
        }
        String email = view.readEmail();
        if (email == null) {
            return;
        }
        String password = view.readPassword();
        if (password == null) {
            return;
        }
        String confirmation = view.readPasswordConfirmation();
        if (confirmation == null) {
            return;
        }
        if (!password.equals(confirmation)) {
            view.showMessage("Passwords do not match.");
            return;
        }

        switch (role) {
            case PROFESSOR -> service.registerProfessor(name, email, password);
            case STUDENT -> service.registerStudent(name, email, password);
        }
        view.showMessage("Registration successful. You can now log in.");
    }
}
