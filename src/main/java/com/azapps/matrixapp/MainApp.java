package com.azapps.matrixapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            URL fxmlLocation = getClass().getResource("/com/azapps/matrixapp/view/MainView.fxml");
            if (fxmlLocation == null) {
                System.err.println("Не удалось найти FXML файл. Проверьте путь: /com/azapps/matrixapp/view/MainView.fxml");
                return;
            }
            Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlLocation));
            primaryStage.setTitle("Калькулятор Матриц");
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}