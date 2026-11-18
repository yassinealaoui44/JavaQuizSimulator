package com.quizsimulator.repository;

import com.quizsimulator.model.Quiz;

import org.hibernate.Session;

public class QuizRepository {

    public void save(Session session, Quiz quiz) {
        session.persist(quiz);
    }
}