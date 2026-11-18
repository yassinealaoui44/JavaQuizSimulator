package com.quizsimulator.database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Objects;
import java.util.function.Function;

public final class Transactions {

    private final SessionFactory factory;

    public Transactions(SessionFactory factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    public <T> T run(Function<Session, T> work) {

        try (Session session = factory.openSession()) {

            Transaction transaction = session.beginTransaction();

            try {
                T result = work.apply(session);

                transaction.commit();

                return result;

            } catch (RuntimeException exception) {

                try {
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }
                } catch (RuntimeException rollbackFailure) {
                    exception.addSuppressed(rollbackFailure);
                }

                throw exception;
            }
        }
    }
}