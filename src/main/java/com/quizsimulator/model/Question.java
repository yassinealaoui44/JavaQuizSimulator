package com.quizsimulator.model;

import jakarta.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(
        name = "questions",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"quiz_id", "position"}
        )
)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ElementCollection
    @CollectionTable(
            name = "choices",
            joinColumns = @JoinColumn(name = "question_id"),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"question_id", "label"}
            )
    )
    @MapKeyColumn(name = "label", length = 1)
    @Column(name = "text", nullable = false, columnDefinition = "text")
    private Map<String, String> choices = new LinkedHashMap<>();

    @Column(name = "correct_choice", nullable = false, length = 1)
    private String correctChoice;

    public Question() {
    }

    public Question(
            int position,
            String text,
            Map<String, String> choices,
            String correctChoice
    ) {
        this.position = position;
        this.text = text;
        this.choices = new LinkedHashMap<>(choices);
        this.correctChoice = correctChoice;
    }

    void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Long getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public Map<String, String> getChoices() {
        return choices;
    }

    public String getCorrectChoice() {
        return correctChoice;
    }
}