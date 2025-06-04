module com.azapps.matrixapp {
    // Зависимости от модулей JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // Явно добавим зависимость от javafx.graphics, так как он упоминается в ошибке

    // Открываем наши пакеты для JavaFX FXML
    opens com.azapps.matrixapp to javafx.fxml;
    opens com.azapps.matrixapp.controller to javafx.fxml;
    // Если FXML будет напрямую ссылаться на классы модели,
    // то и пакет модели нужно будет открыть:
    // opens com.azapps.matrixapp.model to javafx.fxml;

    // ЭКСПОРТИРУЕМ пакет, содержащий MainApp
    exports com.azapps.matrixapp; // Эта строка необходима
    // Если другие модули (кроме javafx.graphics) должны использовать ваши контроллеры или модели,
    // их тоже можно экспортировать, но для текущей ошибки важен именно пакет с MainApp.
    // exports com.azapps.matrixapp.controller;
    // exports com.azapps.matrixapp.model;
}