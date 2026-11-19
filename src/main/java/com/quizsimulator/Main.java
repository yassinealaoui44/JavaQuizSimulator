package com.quizsimulator;

import com.quizsimulator.controller.AuthController;
import com.quizsimulator.controller.ProfessorController;
import com.quizsimulator.controller.StudentController;
import com.quizsimulator.database.HibernateUtil;
import com.quizsimulator.service.AuthService;
import com.quizsimulator.service.GradingService;
import com.quizsimulator.service.QuizService;
import com.quizsimulator.view.AuthView;
import com.quizsimulator.view.ProfessorView;
import com.quizsimulator.view.StudentView;
import org.hibernate.SessionFactory;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SessionFactory factory;
        try {
            factory = HibernateUtil.getSessionFactory();
        } catch (ExceptionInInitializerError exception) {
            System.err.println("Could not start the database connection. "
                    + "Check that PostgreSQL is running, quiz_simulator exists, and DB_PASSWORD is set.");
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            ProfessorController professorController =
                    new ProfessorController(new ProfessorView(scanner), new QuizService(factory));
            StudentController studentController =
                    new StudentController(new StudentView(scanner), new GradingService(factory));
            AuthController authController = new AuthController(
                    new AuthView(scanner), new AuthService(factory), professorController, studentController
            );
            authController.run();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
