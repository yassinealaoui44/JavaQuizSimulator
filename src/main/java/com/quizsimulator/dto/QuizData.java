package com.quizsimulator.dto;

import java.time.LocalDateTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class QuizData {

    private QuizData() {
    }

    public record QuestionDraft(
            int position,
            String text,
            Map<String, String> choices
    ) {

        public QuestionDraft {
            choices = Collections.unmodifiableMap(
                    new TreeMap<>(choices)
            );
        }
    }

    public record QuizDraft(
            String title,
            int expectedQuestions,
            int maxChoices,
            List<QuestionDraft> questions
    ) {

        public QuizDraft {
            questions = List.copyOf(questions);
        }
    }

    public record PublishedQuiz(
            Long quizId,
            String title,
            int questionCount,
            int assignedStudents
    ) {
    }

    public record AssignmentSummary(
            Long assignmentId,
            String quizTitle,
            boolean submitted,
            Integer score,
            int totalQuestions,
            LocalDateTime submittedAt
    ) {
    }

    public record StudentQuestion(
            Long questionId,
            int position,
            String text,
            Map<String, String> choices
    ) {

        public StudentQuestion {
            choices = Collections.unmodifiableMap(
                    new TreeMap<>(choices)
            );
        }
    }

    public record StudentQuiz(
            Long assignmentId,
            String title,
            List<StudentQuestion> questions
    ) {

        public StudentQuiz {
            questions = List.copyOf(questions);
        }
    }

    public record GradeResult(
            Long assignmentId,
            int score,
            int totalQuestions
    ) {
    }
}