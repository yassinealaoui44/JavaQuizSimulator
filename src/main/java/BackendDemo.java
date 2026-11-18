package com.quizsimulator;

import com.quizsimulator.database.DatabaseInitializer;
import com.quizsimulator.database.HibernateUtil;

import com.quizsimulator.service.AuthService;
import com.quizsimulator.service.GradingService;
import com.quizsimulator.service.QuizService;

import java.nio.file.Path;

import java.util.Map;
import java.util.UUID;

public class BackendDemo {

    public static void main(String[] args) {

        var factory = HibernateUtil.getSessionFactory();

        try {
            var auth = new AuthService(factory);
            var quizzes = new QuizService(factory);
            var grading = new GradingService(factory);

            // Different emails on every run.
            String suffix = UUID.randomUUID().toString();
            String password = UUID.randomUUID().toString();

            var professor = DatabaseInitializer.createProfessor(
                    factory,
                    "Demo Professor",
                    "prof-" + suffix + "@example.test",
                    password
            );

            var student = auth.registerStudent(
                    "Demo Student",
                    "student-" + suffix + "@example.test",
                    password
            );

            var loggedInStudent = auth.login(
                    student.getEmail(),
                    password
            );

            var draft = quizzes.previewQuiz(
                    "Java basics (demo)",
                    Path.of("examples", "java-basics.txt"),
                    3,
                    4
            );

            var published = quizzes.publishQuiz(
                    professor,
                    draft,
                    Map.of(
                            1, "B",
                            2, "A",
                            3, "C"
                    )
            );

            var inbox = grading.getAssignments(loggedInStudent);

            var assignment = inbox.stream()
                    .filter(item ->
                            item.quizTitle().equals(published.title())
                    )
                    .findFirst()
                    .orElseThrow();

            var quiz = grading.openAssignment(
                    loggedInStudent,
                    assignment.assignmentId()
            );

            // Question 1: correct.
            // Question 2: incorrect.
            // Question 3: skipped.
            var result = grading.submit(
                    loggedInStudent,
                    quiz.assignmentId(),
                    Map.of(
                            quiz.questions().get(0).questionId(), "B",
                            quiz.questions().get(1).questionId(), "B"
                    )
            );

            System.out.println(
                    "Published quiz ID: " + published.quizId()
            );

            System.out.println(
                    "Assigned students: " + published.assignedStudents()
            );

            System.out.println(
                    "Score: " + result.score()
                            + "/" + result.totalQuestions()
            );

            try {
                grading.submit(
                        loggedInStudent,
                        quiz.assignmentId(),
                        Map.of()
                );

                throw new AssertionError(
                        "Repeated submission was accepted!"
                );

            } catch (IllegalStateException expected) {

                System.out.println(
                        "Repeat blocked: " + expected.getMessage()
                );
            }

        } finally {
            HibernateUtil.shutdown();
        }
    }
}