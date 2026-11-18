package com.quizsimulator.file;

import com.quizsimulator.dto.QuizData.QuestionDraft;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.regex.Pattern;

public class QuizFileReader {

    private static final Pattern CHOICE =
            Pattern.compile("^([A-Z])\\)\\s*(.+)$");

    public List<QuestionDraft> read(
            Path path,
            int expectedQuestions,
            int maxChoices
    ) {

        validateLimits(expectedQuestions, maxChoices);

        if (path == null
                || !path.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT)
                .endsWith(".txt")) {

            throw new IllegalArgumentException(
                    "Choose a .txt quiz file."
            );
        }

        List<String> lines;

        try {
            lines = Files.readAllLines(
                    path,
                    StandardCharsets.UTF_8
            );

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Cannot read quiz file: " + path,
                    exception
            );
        }

        List<QuestionDraft> questions = new ArrayList<>();

        String text = null;

        Map<String, String> choices = new LinkedHashMap<>();

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);

            // Some UTF-8 files start with a byte-order mark.
            if (i == 0 && line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }

            line = line.strip();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("Q:")) {

                if (text != null) {
                    append(questions, text, choices, maxChoices);
                }

                text = line.substring(2).strip();
                choices = new LinkedHashMap<>();

                if (text.isEmpty()) {
                    throw error(
                            i,
                            "Question text cannot be empty."
                    );
                }

            } else {

                var matcher = CHOICE.matcher(line);

                if (text == null || !matcher.matches()) {
                    throw error(
                            i,
                            "Expected Q: question text or A) choice text."
                    );
                }

                String label = matcher.group(1);
                String choice = matcher.group(2).strip();

                if (choice.isEmpty()) {
                    throw error(
                            i,
                            "Choice text cannot be empty."
                    );
                }

                if (choices.putIfAbsent(label, choice) != null) {
                    throw error(
                            i,
                            "Duplicate choice label: " + label
                    );
                }
            }
        }

        if (text != null) {
            append(questions, text, choices, maxChoices);
        }

        if (questions.size() != expectedQuestions) {
            throw new IllegalArgumentException(
                    "Expected " + expectedQuestions
                            + " questions, but found "
                            + questions.size() + "."
            );
        }

        return List.copyOf(questions);
    }

    public static void validateLimits(
            int expectedQuestions,
            int maxChoices
    ) {

        if (expectedQuestions < 1) {
            throw new IllegalArgumentException(
                    "Question count must be positive."
            );
        }

        if (maxChoices < 2 || maxChoices > 26) {
            throw new IllegalArgumentException(
                    "Maximum choices must be between 2 and 26."
            );
        }
    }

    public static void validateQuestion(
            QuestionDraft question,
            int maxChoices
    ) {

        if (question.text() == null || question.text().isBlank()) {
            throw new IllegalArgumentException(
                    "Question text is required."
            );
        }

        int size = question.choices().size();

        if (size < 2 || size > maxChoices) {
            throw new IllegalArgumentException(
                    "Question " + question.position()
                            + " must have 2 to "
                            + maxChoices + " choices."
            );
        }

        for (int i = 0; i < size; i++) {

            String label = String.valueOf((char) ('A' + i));
            String value = question.choices().get(label);

            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(
                        "Question " + question.position()
                                + ": use consecutive labels A, B, C..."
                                + " with nonempty text."
                );
            }
        }
    }

    private static void append(
            List<QuestionDraft> result,
            String text,
            Map<String, String> choices,
            int maximum
    ) {

        QuestionDraft question = new QuestionDraft(
                result.size() + 1,
                text,
                choices
        );

        validateQuestion(question, maximum);

        result.add(question);
    }

    private static IllegalArgumentException error(
            int line,
            String message
    ) {

        return new IllegalArgumentException(
                "Line " + (line + 1) + ": " + message
        );
    }
}