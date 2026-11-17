package com.quizsimulator.database;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY =
            buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "DB_PASSWORD is missing. Set it in the run configuration."
            );
        }

        Configuration configuration = new Configuration();

        // Read src/main/resources/hibernate.cfg.xml
        configuration.configure("hibernate.cfg.xml");

        // Supply the password without putting it in the repository
        configuration.setProperty(
                "hibernate.connection.password",
                password
        );

        return configuration.buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        SESSION_FACTORY.close();
    }
}