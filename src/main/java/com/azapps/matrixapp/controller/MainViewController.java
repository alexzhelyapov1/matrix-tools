package com.azapps.matrixapp.controller;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.geometry.HPos;

import java.util.ArrayList;
import java.util.List;

import com.azapps.matrixapp.model.Matrix;
import com.azapps.matrixapp.model.MatrixOperationException;


public class MainViewController {
    @FXML private ScrollPane matrixScrollPane;
    @FXML private GridPane matrixInputGrid;
    @FXML private HBox sizeControlBox;
    @FXML private Spinner<Integer> rowsSpinner;
    @FXML private Spinner<Integer> colsSpinner;
    @FXML private HBox buttonsContainer;
    @FXML private Button transposeButton;
    @FXML private Button inverseButton;

    private TranslateTransition transposeButtonAnimator;

    @FXML private ScrollPane resultMatrixScrollPane;
    @FXML private GridPane resultMatrixGrid;

    private List<List<TextField>> matrixTextFields;

    private static final int MAX_DIMENSION = 10;
    private static final int MIN_DIMENSION = 1;
    private static final double BUTTON_SPACING = 10.0;

    @FXML
    public void initialize() {
        System.out.println("MainViewController initialized.");

        setupSpinner(rowsSpinner, 2, MIN_DIMENSION, MAX_DIMENSION);
        setupSpinner(colsSpinner, 2, MIN_DIMENSION, MAX_DIMENSION);

        rowsSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) updateMatrixGridAndButtons();
        });
        colsSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) updateMatrixGridAndButtons();
        });

        matrixTextFields = new ArrayList<>();
        transposeButtonAnimator = new TranslateTransition(Duration.millis(300), transposeButton);
        transposeButtonAnimator.setInterpolator(Interpolator.EASE_BOTH);

        Platform.runLater(this::updateMatrixGridAndButtons);
    }

    private void updateMatrixGridAndButtons() {
        updateMatrixGrid();
        Platform.runLater(this::updateButtonStates);
    }

    private void setupSpinner(Spinner<Integer> spinner, int initialValue, int min, int max) {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue);
        spinner.setValueFactory(valueFactory);
        spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                spinner.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (!newValue.isEmpty()) {
                try {
                    int val = Integer.parseInt(newValue);
                    if (val > max) spinner.getEditor().setText(String.valueOf(max));
                } catch (NumberFormatException e) { /* игнор */ }
            }
        });
        spinner.getEditor().focusedProperty().addListener((observable, oldValue, lostFocus) -> {
            if (lostFocus) commitSpinnerValue(spinner);
        });
        spinner.getEditor().setOnAction(event -> commitSpinnerValue(spinner));
    }

    private void commitSpinnerValue(Spinner<Integer> spinner) {
        try {
            String text = spinner.getEditor().getText();
            SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = null;
            if (spinner.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory) {
                valueFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();
            } else {
                if (!text.isEmpty()) spinner.getValueFactory().setValue(Integer.parseInt(text));
                else spinner.getValueFactory().setValue(spinner.getValue());
                return;
            }

            if (text == null || text.isEmpty()) {
                spinner.getValueFactory().setValue(valueFactory.getMin());
                return;
            }

            int value = Integer.parseInt(text);

            if (value < valueFactory.getMin()) value = valueFactory.getMin();
            else if (value > valueFactory.getMax()) value = valueFactory.getMax();
            spinner.getValueFactory().setValue(value);
        } catch (NumberFormatException e) {
            spinner.getValueFactory().setValue(spinner.getValue());
        } catch (ClassCastException e) {
            System.err.println("Неожиданная ошибка приведения типа SpinnerValueFactory: " + e.getMessage());
            spinner.getValueFactory().setValue(spinner.getValue());
        }
    }

    private void updateMatrixGrid() {
        matrixInputGrid.getChildren().clear();
        if (matrixTextFields != null) matrixTextFields.clear();
        else matrixTextFields = new ArrayList<>();

        int currentRows = rowsSpinner.getValue();
        int currentCols = colsSpinner.getValue();

        for (int i = 0; i < currentRows; i++) {
            List<TextField> rowFields = new ArrayList<>();
            for (int j = 0; j < currentCols; j++) {
                TextField textField = new TextField();
                textField.setPromptText("0");
                textField.setPrefWidth(60);
                textField.setPrefHeight(35);
                textField.setAlignment(Pos.CENTER);
                textField.textProperty().addListener((obs, oldValue, newValue) -> {
                    if (!newValue.matches("-?((\\d*\\.?\\d*)|(\\d+\\.?))")) {
                        textField.setText(oldValue);
                    }
                });
                matrixInputGrid.add(textField, j, i);
                rowFields.add(textField);
            }
            matrixTextFields.add(rowFields);
        }
        initializeOrUpdateResultMatrixGrid(currentRows, currentCols);
    }

    private void initializeOrUpdateResultMatrixGrid(int rows, int cols) {
        if (resultMatrixGrid == null) return;
        resultMatrixGrid.getChildren().clear();
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                TextField resultField = new TextField();
                resultField.setPromptText("-");
                resultField.setEditable(false);
                resultField.setFocusTraversable(false);
                resultField.getStyleClass().add("result-matrix-cell");
                resultField.setPrefWidth(60);
                resultField.setPrefHeight(35);
                resultField.setAlignment(Pos.CENTER);
                resultMatrixGrid.add(resultField, j, i);
            }
        }
    }

    private void updateButtonStates() {
        if (buttonsContainer.getWidth() == 0 && buttonsContainer.isVisible()) {

            Platform.runLater(this::updateButtonStates);
            return;
        }
        if (buttonsContainer.getWidth() == 0 && !buttonsContainer.isVisible()) {
            return;
        }

        final int rows = rowsSpinner.getValue();
        final int cols = colsSpinner.getValue();
        final boolean isSquare = (rows == cols);

        if (!isSquare) {
            inverseButton.setVisible(false);
            inverseButton.setManaged(false);
        }

        HBox.setHgrow(transposeButton, Priority.NEVER);
        HBox.setHgrow(inverseButton, Priority.NEVER);

        final double containerWidth = buttonsContainer.getWidth() - buttonsContainer.getPadding().getLeft() - buttonsContainer.getPadding().getRight();

        if (isSquare) {
            final double availableWidthForTwoButtons = containerWidth - BUTTON_SPACING;
            final double buttonWidth = Math.max(50, availableWidthForTwoButtons / 2.0);
            transposeButton.setPrefWidth(buttonWidth);

            transposeButtonAnimator.setOnFinished(null);

            if (transposeButton.getTranslateX() != 0 || buttonsContainer.getAlignment() == Pos.CENTER_LEFT) {
                buttonsContainer.setAlignment(Pos.CENTER_LEFT);
                transposeButtonAnimator.setToX(0);
                transposeButtonAnimator.setOnFinished(event -> {
                    buttonsContainer.setAlignment(Pos.CENTER_RIGHT);
                    inverseButton.setPrefWidth(buttonWidth);
                    inverseButton.setVisible(true);
                    inverseButton.setManaged(true);
                    buttonsContainer.requestLayout();
                    transposeButton.requestLayout();
                    inverseButton.requestLayout();
                    transposeButtonAnimator.setOnFinished(null);
                });
                transposeButtonAnimator.play();
            } else {
                buttonsContainer.setAlignment(Pos.CENTER_RIGHT);

                inverseButton.setPrefWidth(buttonWidth);
                inverseButton.setVisible(true);
                inverseButton.setManaged(true);
                buttonsContainer.requestLayout();
                transposeButton.requestLayout();
                inverseButton.requestLayout();
            }
        } else {
            double singleButtonPrefWidth = Math.max(150, (containerWidth - BUTTON_SPACING) / 2.0);
            transposeButton.setPrefWidth(singleButtonPrefWidth);
            buttonsContainer.setAlignment(Pos.CENTER_LEFT);

            double buttonActualWidth = transposeButton.getBoundsInParent().getWidth();
            if (buttonActualWidth == 0) buttonActualWidth = transposeButton.getPrefWidth();
            double targetX = (containerWidth / 2.0) - (buttonActualWidth / 2.0);

            if (Math.abs(transposeButton.getTranslateX() - targetX) > 0.1) {
                transposeButtonAnimator.setOnFinished(null);
                transposeButtonAnimator.setToX(targetX);
                transposeButtonAnimator.play();
            } else {
                 transposeButton.setTranslateX(targetX);
            }
            buttonsContainer.requestLayout();
            transposeButton.requestLayout();
        }
    }

    @FXML
    private void handleTransposeAction() {
        System.out.println("Transpose button clicked");
        try {
            double[][] inputData = getMatrixFromInput();
            if (inputData == null) return;
            Matrix matrix = new Matrix(inputData);
            Matrix transposedMatrix = matrix.transpose();
            displayMatrixResult(transposedMatrix.getData(), "Транспонированная матрица:");
        } catch (IllegalArgumentException e) {
            showErrorInResultArea("Ошибка создания матрицы: " + e.getMessage());
        } catch (Exception e) {
            showErrorInResultArea("Произошла ошибка при транспонировании: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleInverseAction() {
        System.out.println("Inverse button clicked");
        try {
            double[][] inputData = getMatrixFromInput();
            if (inputData == null) return;
            if (rowsSpinner.getValue() != colsSpinner.getValue()) {
                showErrorInResultArea("Ошибка: Матрица должна быть квадратной для обращения.");
                return;
            }
            Matrix matrix = new Matrix(inputData);
            Matrix invertedMatrix = matrix.inverse();
            displayMatrixResult(invertedMatrix.getData(), "Обратная матрица:");
        } catch (IllegalArgumentException e) {
            showErrorInResultArea("Ошибка создания матрицы: " + e.getMessage());
        } catch (MatrixOperationException e) {
            showErrorInResultArea("Ошибка обращения матрицы: " + e.getMessage());
        } catch (Exception e) {
            showErrorInResultArea("Произошла ошибка при обращении: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double[][] getMatrixFromInput() {
        int currentRows = rowsSpinner.getValue();
        int currentCols = colsSpinner.getValue();
        double[][] matrix = new double[currentRows][currentCols];

        if (matrixTextFields == null || matrixTextFields.size() != currentRows) {
             showErrorInResultArea("Ошибка: Несоответствие данных для ввода матрицы. Пожалуйста, обновите размер.");
             return null;
        }

        for (int i = 0; i < currentRows; i++) {
            if (matrixTextFields.get(i) == null || matrixTextFields.get(i).size() != currentCols) {
                showErrorInResultArea("Ошибка: Несоответствие данных для ввода матрицы (строка " + (i+1) + "). Пожалуйста, обновите размер.");
                return null;
            }
            for (int j = 0; j < currentCols; j++) {
                TextField textField = matrixTextFields.get(i).get(j);
                String text = textField.getText().trim();
                if (text.isEmpty() || text.equals("-") || text.equals(".")) {
                    matrix[i][j] = 0.0;
                } else {
                    try {
                        matrix[i][j] = Double.parseDouble(text);
                    } catch (NumberFormatException e) {
                        showErrorInResultArea("Ошибка: Некорректное значение в ячейке ввода [" + (i + 1) + "," + (j + 1) + "]: '" + textField.getText() + "'");
                        textField.requestFocus();
                        textField.selectAll();
                        return null;
                    }
                }
            }
        }
        return matrix;
    }

    private void displayMatrixResult(double[][] matrixData, String title) {
        if (resultMatrixGrid == null) return;
        resultMatrixGrid.getChildren().clear();

        if (matrixData == null || matrixData.length == 0 || (matrixData.length > 0 && matrixData[0].length == 0) ) {
            Label emptyMsg = new Label(title + ( (matrixData != null && matrixData.length > 0 && matrixData[0].length == 0) ? ": результат - матрица 0 столбцов." : ": результат - пустая матрица." ));
            emptyMsg.getStyleClass().add("info-label");
            resultMatrixGrid.add(emptyMsg, 0, 0);
            GridPane.setColumnSpan(emptyMsg, Math.max(1, colsSpinner.getValue()));
            GridPane.setHalignment(emptyMsg, HPos.CENTER);
            return;
        }

        int resultRows = matrixData.length;
        int resultCols = matrixData[0].length;

        for (int i = 0; i < resultRows; i++) {
            for (int j = 0; j < resultCols; j++) {
                TextField resultField = new TextField();
                resultField.setText(String.format("%.3f", matrixData[i][j]));
                resultField.setEditable(false);
                resultField.setFocusTraversable(false);
                resultField.getStyleClass().add("result-matrix-cell");
                resultField.setPrefWidth(60);
                resultField.setPrefHeight(35);
                resultField.setAlignment(Pos.CENTER);
                resultMatrixGrid.add(resultField, j, i);
            }
        }
    }

    private void showErrorInResultArea(String errorMessage) {
        if (resultMatrixGrid == null) return;
        resultMatrixGrid.getChildren().clear();
        Label errorLabel = new Label(errorMessage);
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        resultMatrixGrid.add(errorLabel, 0, 0);
        int colspan = Math.max(1, colsSpinner.getValue());
        GridPane.setColumnSpan(errorLabel, colspan);
        GridPane.setHalignment(errorLabel, HPos.CENTER);
    }
}