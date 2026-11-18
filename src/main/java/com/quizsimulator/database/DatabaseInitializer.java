package com.quizsimulator.database;

import com.quizsimulator.model.Professor;

import com.quizsimulator.repository.UserRepository;

import com.quizsimulator.security.PasswordHasher;

import com.quizsimulator.service.AccountRules;

import org.hibernate.SessionFactory;

public class DatabaseInitializer {

    public static Professor createProfessor(
            SessionFactory factory,
            String name,
            String email,
            String password
    ) {

        String validName = AccountRules.name(name);
        String validEmail = AccountRules.email(email);
        String hash = PasswordHasher.hash(password);

        UserRepository users = new UserRepository();

        return new Transactions(factory).run(session -> {

            if (users.findByEmail(session, validEmail).isPresent()) {
                throw new IllegalArgumentException(
                        "Email already exists; no account was changed."
                );
            }

            Professor professor = new Professor(
                    validName,
                    validEmail,
                    hash
            );

            users.save(session, professor);

            return professor;
        });
    }

    public static void main(String[] args) {

        String name = requiredEnvironment("PROFESSOR_NAME");
        String email = requiredEnvironment("PROFESSOR_EMAIL");
        String password = requiredEnvironment("PROFESSOR_PASSWORD");

        SessionFactory factory = HibernateUtil.getSessionFactory();

        try {
            Professor professor = createProfessor(
                    factory,
                    name,
                    email,
                    password
            );

            System.out.println(
                    "Professor created: " + professor.getEmail()
            );

        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static String requiredEnvironment(String key) {

        String value = System.getenv(key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Set " + key + " in this run configuration."
            );
        }

        return value;
    }
}