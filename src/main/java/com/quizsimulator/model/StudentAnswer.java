package com.quizsimulator.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "student_answers",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"assignment_id", "question_id"}
        )
)
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private QuizAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "selected_choice", nullable = false, length = 1)
    private String selectedChoice;

    public StudentAnswer() {
    }

    public StudentAnswer(
            QuizAssignment assignment,
            Question question,
            String selectedChoice
    ) {
        this.assignment = assignment;
        this.question = question;
        this.selectedChoice = selectedChoice;
    }

    public Long getId() {
        return id;
    }

    public QuizAssignment getAssignment() {
        return assignment;
    }

    public Question getQuestion() {
        return question;
    }

    public String getSelectedChoice() {
        return selectedChoice;
    }
}