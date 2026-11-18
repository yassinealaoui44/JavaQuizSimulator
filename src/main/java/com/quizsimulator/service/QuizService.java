package com.quizsimulator.service;

import com.quizsimulator.database.HibernateUtil;
import com.quizsimulator.database.Transactions;

import com.quizsimulator.dto.QuizData.*;

import com.quizsimulator.file.QuizFileReader;

import com.quizsimulator.model.*;

import com.quizsimulator.repository.AssignmentRepository;
import com.quizsimulator.repository.QuizRepository;
import com.quizsimulator.repository.UserRepository;

import org.hibernate.SessionFactory;

import java.nio.file.Path;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class QuizService {

    private final Transactions transactions;

    private final QuizFileReader reader = new QuizFileReader();

    private final UserRepository users = new UserRepository();

    private final QuizRepository quizzes = new QuizRepository();

    private final AssignmentRepository assignments =
            new AssignmentRepository();

    public QuizService() {
        this(HibernateUtil.getSessionFactory());
    }

    public QuizService(SessionFactory factory) {
        transactions = new Transactions(factory);
    }

    public QuizDraft previewQuiz(
            String title,
            Path file,
            int expectedQuestions,
            int maxChoices
    ) {

        return new QuizDraft(
                validTitle(title),
                expectedQuestions,
                maxChoices,
                reader.read(file, expectedQuestions, maxChoices)
        );
    }

    public PublishedQuiz publishQuiz(
            User actor,
            QuizDraft draft,
            Map<Integer, String> correctAnswers
    ) {

        if (draft == null || correctAnswers == null) {
            throw new IllegalArgumentException(
                    "A quiz draft and answer key are required."
            );
        }

        String title = validTitle(draft.title());

        QuizFileReader.validateLimits(
                draft.expectedQuestions(),
                draft.maxChoices()
        );

        if (draft.questions().size() != draft.expectedQuestions()
                || correctAnswers.size() != draft.questions().size()) {

            throw new IllegalArgumentException(
                    "Provide the expected questions and one key per question."
            );
        }

        Map<Integer, String> keys = new LinkedHashMap<>();

        for (int i = 0; i < draft.questions().size(); i++) {

            QuestionDraft question = draft.questions().get(i);

            if (question.position() != i + 1) {
                throw new IllegalArgumentException(
                        "Question positions must be 1, 2, 3..."
                );
            }

            QuizFileReader.validateQuestion(
                    question,
                    draft.maxChoices()
            );

            String key = normalizeChoice(
                    correctAnswers.get(question.position())
            );

            if (!question.choices().containsKey(key)) {
                throw new IllegalArgumentException(
                        "Invalid correct choice for question "
                                + question.position()
                );
            }

            keys.put(question.position(), key);
        }

        return transactions.run(session -> {

            Professor professor = AccountRules.professor(
                    session,
                    actor
            );

            Quiz quiz = new Quiz(title, professor);

            for (QuestionDraft question : draft.questions()) {

                quiz.addQuestion(
                        new Question(
                                question.position(),
                                question.text(),
                                question.choices(),
                                keys.get(question.position())
                        )
                );
            }

            quizzes.save(session, quiz);

            var students = users.findAllStudents(session);

            for (Student student : students) {

                QuizAssignment assignment =
                        new QuizAssignment(quiz, student);

                assignments.save(session, assignment);
            }

            return new PublishedQuiz(
                    quiz.getId(),
                    quiz.getTitle(),
                    quiz.getQuestions().size(),
                    students.size()
            );
        });
    }

    static String normalizeChoice(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "A choice letter is required."
            );
        }

        return value.strip().toUpperCase(Locale.ROOT);
    }

    private static String validTitle(String title) {

        if (title == null
                || title.isBlank()
                || title.strip().length() > 255) {

            throw new IllegalArgumentException(
                    "Quiz title is required (maximum 255 characters)."
            );
        }

        return title.strip();
    }
}