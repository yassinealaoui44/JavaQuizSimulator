package com.quizsimulator.repository;

import com.quizsimulator.model.Student;
import com.quizsimulator.model.User;

import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByEmail(
            Session session,
            String normalizedEmail
    ) {

        return session.createQuery(
                        "from User where lower(email) = :email",
                        User.class
                )
                .setParameter("email", normalizedEmail)
                .uniqueResultOptional();
    }

    public List<Student> findAllStudents(Session session) {

        return session.createQuery(
                "from Student order by id",
                Student.class
        ).getResultList();
    }

    public void save(Session session, User user) {
        session.persist(user);
    }
}