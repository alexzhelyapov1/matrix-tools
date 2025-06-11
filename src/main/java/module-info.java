module com.azapps.matrixapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    opens com.azapps.matrixapp to javafx.fxml;
    opens com.azapps.matrixapp.controller to javafx.fxml;
    exports com.azapps.matrixapp;
}