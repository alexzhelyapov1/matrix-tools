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
            // Загружаем FXML файл из папки resources/com/azapps/matrixapp/view/
            // Обратите внимание на путь к FXML файлу.
            // getClass().getResource() ищет ресурс относительно расположения класса MainApp.
            // "/com/azapps/matrixapp/view/MainView.fxml" - абсолютный путь от корня classpath.
            // Если FXML лежит в той же папке, что и MainApp (или в подпапке view относительно MainApp),
            // можно использовать относительный путь: "view/MainView.fxml"
            URL fxmlLocation = getClass().getResource("/com/azapps/matrixapp/view/MainView.fxml");
            if (fxmlLocation == null) {
                System.err.println("Не удалось найти FXML файл. Проверьте путь: /com/azapps/matrixapp/view/MainView.fxml");
                // Можно выбросить исключение или показать диалоговое окно с ошибкой
                return;
            }
            Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlLocation));

            // Устанавливаем заголовок окна
            primaryStage.setTitle("Калькулятор Матриц");

            // Создаем сцену
            Scene scene = new Scene(root, 800, 600); // Начальные размеры окна

            // Устанавливаем сцену для Stage
            primaryStage.setScene(scene);

            // Отображаем Stage (окно)
            primaryStage.show();

        } catch (IOException e) {
            // Обработка исключения, если FXML файл не может быть загружен
            e.printStackTrace();
            // Здесь можно показать пользователю сообщение об ошибке
            // например, через Alert
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}