package com.quizsimulator.view;

import com.quizsimulator.dto.QuizData.AssignmentSummary;
import com.quizsimulator.dto.QuizData.GradeResult;
import com.quizsimulator.dto.QuizData.StudentQuestion;
import com.quizsimulator.dto.QuizData.StudentQuiz;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class StudentView {
    private final Scanner scanner;

    public StudentView(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readMenuChoice() {
        showMessage("1. View assigned quizzes");
        showMessage("0. Logout");
        return readOption("Choose an option: ", 1);
    }

    public AssignmentSummary selectAssignment(List<AssignmentSummary> assignments) {
        if (assignments.isEmpty()) {
            showMessage("My quizzes is empty.");
            return null;
        }

        showMessage("My quizzes");
        for (int index = 0; index < assignments.size(); index++) {
            AssignmentSummary assignment = assignments.get(index);
            String status = assignment.submitted()
                    ? "Submitted — " + assignment.score() + "/" + assignment.totalQuestions()
                    : "Pending";
            showMessage((index + 1) + ". " + assignment.quizTitle() + " — " + status);
        }
        showMessage("0. Back");

        while (true) {
            int choice = readOption("Choose a pending quiz (0 to go back): ", assignments.size());
            if (choice == 0) {
                return null;
            }

            AssignmentSummary assignment = assignments.get(choice - 1);
            if (!assignment.submitted()) {
                return assignment;
            }
            showMessage("That quiz has already been submitted.");
        }
    }

    public Map<Long, String> readAnswers(StudentQuiz quiz) {
        Map<Long, String> answers = new LinkedHashMap<>();
        showMessage(quiz.title());
        for (StudentQuestion question : quiz.questions()) {
            showMessage(question.position() + ". " + question.text());
            question.choices().forEach((letter, text) -> showMessage(letter + ". " + text));

            while (true) {
                String answer = readLine("Answer (Enter to skip, 0 to cancel): ");
                if (answer == null || answer.equals("0")) {
                    return null;
                }
                if (answer.isEmpty()) {
                    break;
                }

                answer = answer.toUpperCase(Locale.ROOT);
                if (question.choices().containsKey(answer)) {
                    answers.put(question.questionId(), answer);
                    break;
                }
                showMessage("Enter one of the displayed letters.");
            }
        }
        return answers;
    }

    public boolean confirmSubmission(int answered, int totalQuestions) {
        showMessage("Answered: " + answered + " | Skipped: " + (totalQuestions - answered));
        while (true) {
            String answer = readLine("Submit your answers? (y/n): ");
            if (answer == null || answer.equalsIgnoreCase("n")) {
                return false;
            }
            if (answer.equalsIgnoreCase("y")) {
                return true;
            }
            showMessage("Please enter y or n.");
        }
    }

    public void showGrade(GradeResult result) {
        showMessage("Grade: " + result.score() + "/" + result.totalQuestions());
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    private int readOption(String prompt, int max) {
        while (true) {
            String input = readLine(prompt);
            if (input == null) {
                return 0;
            }

            try {
                int option = Integer.parseInt(input);
                if (option >= 0 && option <= max) {
                    return option;
                }
            } catch (NumberFormatException ignored) {
                // Use the same message for invalid and out-of-range input.
            }
            showMessage("Enter a whole number from 0 to " + max + ".");
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }
}