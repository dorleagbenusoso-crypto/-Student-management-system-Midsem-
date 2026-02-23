package com.smssimple;

import com.smssimple.util.AppLogger;
import com.smssimple.util.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Student Management System - Simple Version
 * Main class: com.smssimple.MainApp
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        AppLogger.info("Application starting");
        
        // Initialize database
        try {
            DatabaseManager.getConnection();
        } catch (Exception e) {
            AppLogger.error("Database initialization failed: " + e.getMessage());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smssimple/fxml/MainView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 700);

        stage.setTitle("Student Management System");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        AppLogger.info("Application closing");
        DatabaseManager.close();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
