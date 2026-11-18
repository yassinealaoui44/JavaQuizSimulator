package com.quizsimulator.repository;

import com.quizsimulator.model.QuizAssignment;

import jakarta.persistence.LockModeType;

import org.hibernate.Session;

import java.util.List;

public class AssignmentRepository {

    public void save(
            Session session,
            QuizAssignment assignment
    ) {
        session.persist(assignment);
    }

    public void saveSubmission(
            Session session,
            QuizAssignment assignment
    ) {

        assignment.getAnswers().forEach(session::persist);

        // The loaded assignment is managed by Hibernate.
        // Its changed score and submission time are saved on commit.
    }

    public List<QuizAssignment> findByStudent(
            Session session,
            Long studentId
    ) {

        return session.createQuery(
                        """
                        select a
                        from QuizAssignment a
                        join fetch a.quiz
                        where a.student.id = :studentId
                        order by a.id desc
                        """,
                        QuizAssignment.class
                )
                .setParameter("studentId", studentId)
                .getResultList();
    }

    public QuizAssignment findOwned(
            Session session,
            Long assignmentId,
            Long studentId,
            boolean forSubmission
    ) {

        var query = session.createQuery(
                        """
                        from QuizAssignment a
                        where a.id = :id
                          and a.student.id = :studentId
                        """,
                        QuizAssignment.class
                )
                .setParameter("id", assignmentId)
                .setParameter("studentId", studentId);

        if (forSubmission) {
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        }

        return query.uniqueResultOptional()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Assignment not found for this student."
                        )
                );
    }
}