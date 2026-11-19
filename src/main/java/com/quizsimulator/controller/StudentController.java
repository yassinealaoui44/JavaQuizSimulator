package com.quizsimulator.controller;

import com.quizsimulator.dto.QuizData.AssignmentSummary;
import com.quizsimulator.dto.QuizData.GradeResult;
import com.quizsimulator.dto.QuizData.StudentQuiz;
import com.quizsimulator.model.User;
import com.quizsimulator.service.GradingService;
import com.quizsimulator.view.StudentView;

import java.util.Map;

public class StudentController {
    private final StudentView view;
    private final GradingService service;

    public StudentController(StudentView view, GradingService service) {
        this.view = view;
        this.service = service;
    }

    public void run(User student) {
        while (view.readMenuChoice() == 1) {
            try {
                openInbox(student);
            } catch (IllegalArgumentException | IllegalStateException exception) {
                view.showMessage(exception.getMessage());
            }
        }
    }

    private void openInbox(User student) {
        AssignmentSummary assignment = view.selectAssignment(service.getAssignments(student));
        if (assignment == null) {
            return;
        }

        Long assignmentId = assignment.assignmentId();
        StudentQuiz quiz = service.openAssignment(student, assignmentId);
        Map<Long, String> answers = view.readAnswers(quiz);
        if (answers == null || !view.confirmSubmission(answers.size(), quiz.questions().size())) {
            view.showMessage("Submission cancelled.");
            return;
        }

        GradeResult result = service.submit(student, assignmentId, answers);
        view.showGrade(result);
    }
}
