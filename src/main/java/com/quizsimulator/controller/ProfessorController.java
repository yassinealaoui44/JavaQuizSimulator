package com.quizsimulator.controller;

import com.quizsimulator.dto.QuizData.PublishedQuiz;
import com.quizsimulator.dto.QuizData.QuizDraft;
import com.quizsimulator.model.User;
import com.quizsimulator.service.QuizService;
import com.quizsimulator.view.ProfessorView;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;

public class ProfessorController {
    private final ProfessorView view;
    private final QuizService service;

    public ProfessorController(ProfessorView view, QuizService service) {
        this.view = view;
        this.service = service;
    }

    public void run(User professor) {
        while (view.readMenuChoice() == 1) {
            try {
                createQuiz(professor);
            } catch (InvalidPathException exception) {
                view.showMessage("Enter a valid file path.");
            } catch (IllegalArgumentException exception) {
                view.showMessage(exception.getMessage());
            }
        }
    }

    private void createQuiz(User professor) {
        String title = view.readTitle();
        if (title == null) {
            return;
        }
        String filePath = view.readFilePath();
        if (filePath == null) {
            return;
        }
        int expectedQuestions = view.readExpectedQuestions();
        if (expectedQuestions == 0) {
            return;
        }
        int maxChoices = view.readMaxChoices();
        if (maxChoices == 0) {
            return;
        }

        QuizDraft draft = service.previewQuiz(title, Path.of(filePath), expectedQuestions, maxChoices);
        Map<Integer, String> correctAnswers = view.readCorrectAnswers(draft);
        if (correctAnswers == null || !view.confirmPublication(draft, correctAnswers)) {
            view.showMessage("Publication cancelled.");
            return;
        }

        PublishedQuiz result = service.publishQuiz(professor, draft, correctAnswers);
        view.showPublished(result);
    }
}
