package com.quizsimulator.view;

import com.quizsimulator.dto.QuizData.PublishedQuiz;
import com.quizsimulator.dto.QuizData.QuestionDraft;
import com.quizsimulator.dto.QuizData.QuizDraft;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class ProfessorView {
    private final Scanner scanner;

    public ProfessorView(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readMenuChoice() {
        showMessage("1. Create and publish a quiz");
        showMessage("0. Logout");
        return readNumber("Choose an option: ", 0, 1);
    }

    public String readTitle() {
        return readRequired("Quiz title (:back to cancel): ");
    }

    public String readFilePath() {
        return readRequired("Path to the .txt file (:back to cancel): ");
    }

    public int readExpectedQuestions() {
        return readNumber("Expected number of questions (0 to cancel): ", 1, Integer.MAX_VALUE);
    }

    public int readMaxChoices() {
        return readNumber("Maximum choices per question (0 to cancel): ", 2, 26);
    }

    public Map<Integer, String> readCorrectAnswers(QuizDraft draft) {
        Map<Integer, String> answers = new LinkedHashMap<>();

        for (QuestionDraft question : draft.questions()) {
            showMessage(question.position() + ". " + question.text());
            question.choices().forEach((letter, text) -> showMessage(letter + ". " + text));

            while (true) {
                String answer = readLine("Correct answer (0 to cancel): ");
                if (answer == null || answer.equals("0")) {
                    return null;
                }
                answer = answer.toUpperCase(Locale.ROOT);
                if (question.choices().containsKey(answer)) {
                    answers.put(question.position(), answer);
                    break;
                }
                showMessage("Enter one of the displayed letters.");
            }
        }
        return answers;
    }

    public boolean confirmPublication(QuizDraft draft, Map<Integer, String> answers) {
        showMessage("Quiz: " + draft.title());
        showMessage("Questions: " + draft.questions().size());
        answers.forEach((position, letter) -> showMessage(position + " → " + letter));

        while (true) {
            String answer = readLine("Publish this quiz? (y/n): ");
            if (answer == null || answer.equalsIgnoreCase("n")) {
                return false;
            }
            if (answer.equalsIgnoreCase("y")) {
                return true;
            }
            showMessage("Please enter y or n.");
        }
    }

    public void showPublished(PublishedQuiz quiz) {
        showMessage("Published " + quiz.title() + " and assigned it to "
                + quiz.assignedStudents() + " student(s).");
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    private String readRequired(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (input == null || input.equalsIgnoreCase(":back")) {
                return null;
            }
            if (!input.isEmpty()) {
                return input;
            }
            showMessage("This field is required.");
        }
    }

    private int readNumber(String prompt, int min, int max) {
        while (true) {
            String input = readLine(prompt);
            if (input == null) {
                return 0;
            }
            try {
                int number = Integer.parseInt(input);
                if (number == 0 || (number >= min && number <= max)) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
                // The message below covers invalid numbers and out-of-range values.
            }
            if (max == Integer.MAX_VALUE) {
                showMessage("Enter a positive whole number.");
            } else {
                showMessage("Enter a whole number from " + min + " to " + max + ".");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }
}