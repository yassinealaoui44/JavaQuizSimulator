package com.quizsimulator.service;

import com.quizsimulator.database.HibernateUtil;
import com.quizsimulator.database.Transactions;

import com.quizsimulator.model.Professor;
import com.quizsimulator.model.Student;
import com.quizsimulator.model.User;

import com.quizsimulator.repository.UserRepository;
import com.quizsimulator.security.PasswordHasher;

import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;

public class AuthService {

    private final Transactions transactions;

    private final UserRepository users = new UserRepository();

    public AuthService() {
        this(HibernateUtil.getSessionFactory());
    }

    public AuthService(SessionFactory factory) {
        transactions = new Transactions(factory);
    }

    public Student registerStudent(
            String name,
            String email,
            String password
    ) {
        return register(name, email, password, Student::new);
    }

    public Professor registerProfessor(
            String name,
            String email,
            String password
    ) {
        return register(name, email, password, Professor::new);
    }

    private <T extends User> T register(
            String name,
            String email,
            String password,
            AccountFactory<T> createAccount
    ) {

        String validName = AccountRules.name(name);
        String validEmail = AccountRules.email(email);
        String hash = PasswordHasher.hash(password);

        try {
            return transactions.run(session -> {

                if (users.findByEmail(session, validEmail).isPresent()) {
                    throw new IllegalArgumentException(
                            "Email is already registered."
                    );
                }

                T account = createAccount.create(validName, validEmail, hash);
                users.save(session, account);
                return account;
            });

        } catch (ConstraintViolationException exception) {

            // Handles duplicate emails during simultaneous registrations.
            if ("23505".equals(exception.getSQLState())) {
                throw new IllegalArgumentException(
                        "Email is already registered.",
                        exception
                );
            }

            throw exception;
        }
    }

    @FunctionalInterface
    private interface AccountFactory<T extends User> {
        T create(String name, String email, String hash);
    }

    public User login(String email, String password) {

        String validEmail = AccountRules.email(email);

        User user = transactions.run(session ->
                users.findByEmail(session, validEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid email or password."
                                )
                        )
        );

        boolean correctPassword = PasswordHasher.verify(
                password,
                user.getPasswordHash()
        );

        if (!correctPassword) {
            throw new IllegalArgumentException(
                    "Invalid email or password."
            );
        }

        return user;
    }
}
