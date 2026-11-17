package com.quizsimulator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "assignments",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"quiz_id", "student_id"}
        )
)
public class QuizAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private Integer score;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @OneToMany(
            mappedBy = "assignment",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private List<StudentAnswer> answers = new ArrayList<>();

    public QuizAssignment() {
    }

    public QuizAssignment(Quiz quiz, Student student) {
        this.quiz = quiz;
        this.student = student;
    }

    public void addAnswer(Question question, String selectedChoice) {
        answers.add(new StudentAnswer(this, question, selectedChoice));
    }

    public void recordResult(int score) {
        if (submittedAt != null) {
            throw new IllegalStateException("Quiz already submitted.");
        }

        if (score < 0 || score > quiz.getQuestions().size()) {
            throw new IllegalArgumentException("Invalid score.");
        }

        this.score = score;
        this.submittedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public Student getStudent() {
        return student;
    }

    public Integer getScore() {
        return score;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public List<StudentAnswer> getAnswers() {
        return answers;
    }

    public boolean isSubmitted() {
        return submittedAt != null;
    }
}