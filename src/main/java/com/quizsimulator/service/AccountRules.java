package com.quizsimulator.service;

import com.quizsimulator.model.Professor;
import com.quizsimulator.model.Student;
import com.quizsimulator.model.User;

import org.hibernate.Session;

import java.util.Locale;

public final class AccountRules {

    private AccountRules() {
    }

    public static String name(String value) {

        if (value == null
                || value.isBlank()
                || value.strip().length() > 100) {

            throw new IllegalArgumentException(
                    "Name is required (maximum 100 characters)."
            );
        }

        return value.strip();
    }

    public static String email(String value) {

        if (value == null) {
            throw new IllegalArgumentException("Email is required.");
        }

        String normalized = value.strip().toLowerCase(Locale.ROOT);

        if (normalized.length() > 255
                || !normalized.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {

            throw new IllegalArgumentException(
                    "Enter a valid email address."
            );
        }

        return normalized;
    }

    static Student student(Session session, User actor) {

        requireId(actor);

        User user = session.get(User.class, actor.getId());

        if (!(user instanceof Student student)) {
            throw new IllegalArgumentException(
                    "A student account is required."
            );
        }

        return student;
    }

    static Professor professor(Session session, User actor) {

        requireId(actor);

        User user = session.get(User.class, actor.getId());

        if (!(user instanceof Professor professor)) {
            throw new IllegalArgumentException(
                    "A professor account is required."
            );
        }

        return professor;
    }

    private static void requireId(User actor) {

        if (actor == null || actor.getId() == null) {
            throw new IllegalArgumentException("Log in first.");
        }
    }
}