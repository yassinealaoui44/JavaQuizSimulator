package com.quizsimulator;

import com.quizsimulator.database.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class DatabaseTest {

    public static void main(String[] args) {

        SessionFactory factory = HibernateUtil.getSessionFactory();

        try (Session session = factory.openSession()) {

            Transaction transaction = session.beginTransaction();

            try {
                String databaseName = session.createNativeQuery(
                        "SELECT current_database()",
                        String.class
                ).getSingleResult();

                transaction.commit();

                System.out.println("Connection successful!");
                System.out.println("Database: " + databaseName);

            } catch (RuntimeException exception) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                throw exception;
            }

        } finally {
            HibernateUtil.shutdown();
        }
    }
}