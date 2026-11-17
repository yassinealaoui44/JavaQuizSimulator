package com.quizsimulator.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @OneToMany(
            mappedBy = "quiz",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @OrderBy("position ASC")
    private List<Question> questions = new ArrayList<>();

    public Quiz() {
    }

    public Quiz(String title, Professor professor) {
        this.title = title;
        this.professor = professor;
    }

    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Professor getProfessor() {
        return professor;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}