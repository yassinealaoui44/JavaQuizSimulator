package com.quizsimulator.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PROFESSOR")
public class Professor extends User {

    public Professor() {
    }

    public Professor(String name, String email, String passwordHash) {
        super(name, email, passwordHash);
    }

    @Override
    public Role getRole() {
        return Role.PROFESSOR;
    }
}