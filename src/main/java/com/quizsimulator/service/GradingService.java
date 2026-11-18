package com.quizsimulator.service;

import com.quizsimulator.database.HibernateUtil;
import com.quizsimulator.database.Transactions;

import com.quizsimulator.dto.QuizData.*;

import com.quizsimulator.model.Question;
import com.quizsimulator.model.QuizAssignment;
import com.quizsimulator.model.Student;
import com.quizsimulator.model.User;

import com.quizsimulator.repository.AssignmentRepository;

import org.hibernate.SessionFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.stream.Collectors;

public class GradingService {

    private final Transactions transactions;

    private final AssignmentRepository assignments =
            new AssignmentRepository();

    public GradingService() {
        this(HibernateUtil.getSessionFactory());
    }

    public GradingService(SessionFactory factory) {
        transactions = new Transactions(factory);
    }

    public List<AssignmentSummary> getAssignments(User actor) {

        return transactions.run(session -> {

            Student student = AccountRules.student(session, actor);

            return assignments
                    .findByStudent(session, student.getId())
                    .stream()
                    .map(assignment ->
                            new AssignmentSummary(
                                    assignment.getId(),
                                    assignment.getQuiz().getTitle(),
                                    assignment.isSubmitted(),
                                    assignment.getScore(),
                                    assignment.getQuiz()
                                            .getQuestions()
                                            .size(),
                                    assignment.getSubmittedAt()
                            )
                    )
                    .toList();
        });
    }

    public StudentQuiz openAssignment(
            User actor,
            Long assignmentId
    ) {

        requireAssignmentId(assignmentId);

        return transactions.run(session -> {

            Student student = AccountRules.student(session, actor);

            QuizAssignment assignment = assignments.findOwned(
                    session,
                    assignmentId,
                    student.getId(),
                    false
            );

            ensurePending(assignment);

            var questions = assignment
                    .getQuiz()
                    .getQuestions()
                    .stream()
                    .map(question ->
                            new StudentQuestion(
                                    question.getId(),
                                    question.getPosition(),
                                    question.getText(),
                                    question.getChoices()
                            )
                    )
                    .toList();

            return new StudentQuiz(
                    assignment.getId(),
                    assignment.getQuiz().getTitle(),
                    questions
            );
        });
    }

    public GradeResult submit(
            User actor,
            Long assignmentId,
            Map<Long, String> selectedAnswers
    ) {

        requireAssignmentId(assignmentId);

        if (selectedAnswers == null) {
            throw new IllegalArgumentException(
                    "Answers are required; use an empty map to skip all."
            );
        }

        Map<Long, String> submitted =
                new LinkedHashMap<>(selectedAnswers);

        return transactions.run(session -> {

            Student student = AccountRules.student(session, actor);

            QuizAssignment assignment = assignments.findOwned(
                    session,
                    assignmentId,
                    student.getId(),
                    true
            );

            ensurePending(assignment);

            List<Question> questions =
                    assignment.getQuiz().getQuestions();

            Set<Long> questionIds = questions.stream()
                    .map(Question::getId)
                    .collect(Collectors.toSet());

            if (!questionIds.containsAll(submitted.keySet())) {
                throw new IllegalArgumentException(
                        "An answer references a question outside this quiz."
                );
            }

            Map<Long, String> normalized = new LinkedHashMap<>();

            // Validate every supplied answer before saving.
            for (Question question : questions) {

                if (!submitted.containsKey(question.getId())) {
                    continue;
                }

                String choice = QuizService.normalizeChoice(
                        submitted.get(question.getId())
                );

                if (!question.getChoices().containsKey(choice)) {
                    throw new IllegalArgumentException(
                            "Invalid choice for question "
                                    + question.getPosition()
                    );
                }

                normalized.put(question.getId(), choice);
            }

            int score = (int) questions.stream()
                    .filter(question ->
                            question.getCorrectChoice().equals(
                                    normalized.get(question.getId())
                            )
                    )
                    .count();

            for (Question question : questions) {

                if (normalized.containsKey(question.getId())) {

                    assignment.addAnswer(
                            question,
                            normalized.get(question.getId())
                    );
                }
            }

            assignment.recordResult(score);

            assignments.saveSubmission(session, assignment);

            return new GradeResult(
                    assignment.getId(),
                    score,
                    questions.size()
            );
        });
    }

    private static void ensurePending(QuizAssignment assignment) {

        if (assignment.isSubmitted()) {
            throw new IllegalStateException(
                    "Quiz already submitted."
            );
        }
    }

    private static void requireAssignmentId(Long id) {

        if (id == null || id < 1) {
            throw new IllegalArgumentException(
                    "Select a valid assignment."
            );
        }
    }
}