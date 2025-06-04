package com.azapps.matrixapp.controller;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import com.azapps.matrixapp.model.Matrix;
import com.azapps.matrixapp.model.MatrixOperationException;


public class MainViewController {

    // --- Элементы для ввода матрицы ---
    @FXML private ScrollPane matrixScrollPane;
    @FXML private GridPane matrixInputGrid;

    // --- Элементы для задания размера ---
    @FXML private HBox sizeControlBox; // Контейнер для элементов управления размером
    @FXML private Spinner<Integer> rowsSpinner;
    @FXML private Spinner<Integer> colsSpinner;

    // --- Кнопки операций ---
    // @FXML private AnchorPane buttonsPane; // Больше не нужен AnchorPane как прямой родитель кнопок HBox
    @FXML private HBox buttonsContainer;  // HBox, содержащий кнопки
    @FXML private Button transposeButton;
    @FXML private Button inverseButton;

    // --- Элементы для вывода результата ---
    @FXML private ScrollPane resultScrollPane;
    @FXML private Label resultLabel;

    private List<List<TextField>> matrixTextFields;

    private static final int MAX_DIMENSION = 10;
    private static final int MIN_DIMENSION = 1;
    private static final double BUTTON_SPACING = 10.0; // Расстояние между кнопками в HBox

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

        // Чтобы buttonsContainer был корректно измерен перед первым вызовом updateButtonStates
        buttonsContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0 && oldVal.doubleValue() == 0) { // Запускаем один раз после инициализации размера
                 updateButtonStates();
            }
        });
        
        updateMatrixGridAndButtons(); // Первоначальное создание и настройка
    }

    private void updateMatrixGridAndButtons() {
        updateMatrixGrid();
        updateButtonStates();
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
                    // Не форсируем min сразу, чтобы пользователь мог ввести, например, "0" для "10"
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
            
            // Получаем конкретную фабрику значений
            SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = null;
            if (spinner.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory) {
                valueFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();
            } else {
                // Если это не IntegerSpinnerValueFactory, то наша логика min/max не сработает.
                // В этом случае можно просто попытаться установить значение, если оно парсится.
                // Или выбросить ошибку/залогировать.
                // Для нашего приложения мы ожидаем IntegerSpinnerValueFactory.
                if (!text.isEmpty()) spinner.getValueFactory().setValue(Integer.parseInt(text));
                else spinner.getValueFactory().setValue(spinner.getValue()); // Восстановить, если пусто
                return;
            }


            if (text == null || text.isEmpty()) {
                // Если поле пустое, устанавливаем минимальное значение
                spinner.getValueFactory().setValue(valueFactory.getMin()); 
                return;
            }
            
            int value = Integer.parseInt(text);
            
            if (value < valueFactory.getMin()) {
                value = valueFactory.getMin();
            } else if (value > valueFactory.getMax()) {
                value = valueFactory.getMax();
            }
            spinner.getValueFactory().setValue(value);
        } catch (NumberFormatException e) {
            // Если значение невалидно (например, не число), восстанавливаем предыдущее валидное значение
            spinner.getValueFactory().setValue(spinner.getValue());
        } catch (ClassCastException e) {
            // Этого не должно случиться, если мы используем IntegerSpinnerValueFactory,
            // но проверка instanceof выше должна это предотвратить.
            System.err.println("Неожиданная ошибка приведения типа SpinnerValueFactory: " + e.getMessage());
            spinner.getValueFactory().setValue(spinner.getValue()); // Восстановить
        }
    }

    private void updateMatrixGrid() {
        matrixInputGrid.getChildren().clear();
        matrixTextFields.clear();

        int rows = rowsSpinner.getValue();
        int cols = colsSpinner.getValue();

        for (int i = 0; i < rows; i++) {
            List<TextField> rowFields = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                TextField textField = new TextField();
                textField.setPromptText("0");
                textField.setPrefWidth(60); // Немного увеличим
                textField.setPrefHeight(35); // Явно зададим высоту, можно и через CSS
                textField.setAlignment(Pos.CENTER);
                textField.textProperty().addListener((obs, oldValue, newValue) -> {
                    if (!newValue.matches("-?((\\d*\\.?\\d*)|(\\d+\\.?))")) { // Улучшенная регулярка для чисел
                        textField.setText(oldValue);
                    }
                });
                matrixInputGrid.add(textField, j, i);
                rowFields.add(textField);
            }
            matrixTextFields.add(rowFields);
        }
        resultLabel.setText("");
    }

    private void updateButtonStates() {
        if (buttonsContainer.getWidth() == 0) {
            // Если контейнер еще не отрисован и его ширина 0, откладываем обновление.
            // Это может случиться при первом запуске, слушатель на widthProperty должен это покрыть.
            return;
        }

        int rows = rowsSpinner.getValue();
        int cols = colsSpinner.getValue();
        boolean isSquare = (rows == cols);

        inverseButton.setVisible(isSquare);
        inverseButton.setManaged(isSquare);

        double containerWidth = buttonsContainer.getWidth() - buttonsContainer.getPadding().getLeft() - buttonsContainer.getPadding().getRight();
        double targetButtonWidth;

        if (isSquare) {
            // Обе кнопки видны
            targetButtonWidth = (containerWidth - BUTTON_SPACING) / 2.0;
            transposeButton.setPrefWidth(targetButtonWidth);
            inverseButton.setPrefWidth(targetButtonWidth);
            buttonsContainer.setAlignment(Pos.CENTER_RIGHT); // Выравниваем пару кнопок вправо

            // Убираем смещение для кнопки транспонирования, если оно было
            transposeButton.setTranslateX(0);

        } else {
            // Только кнопка транспонирования видна
            targetButtonWidth = containerWidth; // Занимает всю ширину контейнера (если она одна)
                                                // или можно задать ей чуть меньшую ширину для красоты
            // targetButtonWidth = Math.min(containerWidth, 200); // Например, максимальная ширина для одной кнопки
            transposeButton.setPrefWidth(targetButtonWidth);
            
            // Плавное перемещение кнопки "Транспонировать" в центр buttonsContainer
            // buttonsContainer остается выровненным по правому краю в родительском HBox,
            // а мы двигаем кнопку внутри него.
            // buttonsContainer.setAlignment(Pos.CENTER_LEFT); // Чтобы translateX считался от левого края HBox
            // double buttonCurrentWidth = transposeButton.getBoundsInParent().getWidth(); // Используем актуальную ширину
            // double targetX = (containerWidth / 2.0) - (buttonCurrentWidth / 2.0) - transposeButton.getLayoutX();
            
            // Упрощенный вариант: центрируем HBox, если там только одна кнопка
            buttonsContainer.setAlignment(Pos.CENTER);
            transposeButton.setTranslateX(0); // Убедимся, что нет старого смещения
        }
        
        // Для плавной анимации изменения ширины и положения
        // Анимация ширины (может быть сложной для стандартных кнопок, т.к. prefWidth не анимируется напрямую)
        // Проще анимировать TranslateX
        // Если мы используем buttonsContainer.setAlignment(Pos.CENTER) для одной кнопки,
        // то анимация TranslateX не нужна или должна быть 0.
        // Если же мы хотим, чтобы transposeButton плавно "скользила" в центр контейнера,
        // когда inverseButton исчезает, а сам buttonsContainer не меняет выравнивание,
        // то нужен более сложный расчет TranslateX.

        // Давайте попробуем вариант с изменением выравнивания HBox, это проще и часто выглядит хорошо.
        // Если этого будет недостаточно, реализуем более сложную анимацию TranslateTransition.

        // Повторный вызов для применения ширин перед анимацией (если необходима)
        transposeButton.requestLayout();
        if(isSquare) inverseButton.requestLayout();
    }


    @FXML
    private void handleTransposeAction() {
        System.out.println("Transpose button clicked");
        try {
            double[][] inputData = getMatrixFromInput();
            if (inputData == null) {
                // Ошибка уже отображена в getMatrixFromInput
                return;
            }

            Matrix matrix = new Matrix(inputData);
            Matrix transposedMatrix = matrix.transpose();

            displayMatrixResult(transposedMatrix.getData(), "Транспонированная матрица:");

        } catch (IllegalArgumentException e) { // От конструктора Matrix
            resultLabel.setText("Ошибка создания матрицы: " + e.getMessage());
        } catch (Exception e) { // Общий перехватчик
            resultLabel.setText("Произошла ошибка при транспонировании: " + e.getMessage());
            e.printStackTrace(); // Для отладки в консоль
        }
    }

    @FXML
    private void handleInverseAction() {
        System.out.println("Inverse button clicked");
        try {
            double[][] inputData = getMatrixFromInput();
            if (inputData == null) {
                // Ошибка уже отображена в getMatrixFromInput
                return;
            }

            // Проверка на квадратность здесь уже не так критична, т.к. Matrix.inverse() ее выполнит,
            // но можно оставить для более раннего сообщения пользователю.
            if (rowsSpinner.getValue() != colsSpinner.getValue()) {
                resultLabel.setText("Ошибка: Матрица должна быть квадратной для обращения.");
                return; // Хотя Matrix.inverse() тоже выбросит исключение
            }

            Matrix matrix = new Matrix(inputData);
            Matrix invertedMatrix = matrix.inverse(); // Может выбросить MatrixOperationException

            displayMatrixResult(invertedMatrix.getData(), "Обратная матрица:");

        } catch (IllegalArgumentException e) { // От конструктора Matrix
            resultLabel.setText("Ошибка создания матрицы: " + e.getMessage());
        } catch (MatrixOperationException e) { // От Matrix.inverse()
            resultLabel.setText("Ошибка обращения матрицы: " + e.getMessage());
        } catch (Exception e) { // Общий перехватчик
            resultLabel.setText("Произошла ошибка при обращении: " + e.getMessage());
            e.printStackTrace(); // Для отладки в консоль
        }
    }

    private double[][] getMatrixFromInput() {
        int rows = rowsSpinner.getValue();
        int cols = colsSpinner.getValue();
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                TextField textField = matrixTextFields.get(i).get(j);
                String text = textField.getText().trim();
                if (text.isEmpty() || text.equals("-") || text.equals(".")) {
                    matrix[i][j] = 0.0;
                } else {
                    try {
                        matrix[i][j] = Double.parseDouble(text);
                    } catch (NumberFormatException e) {
                        resultLabel.setText("Ошибка: Некорректное значение в ячейке [" + (i + 1) + "," + (j + 1) + "]: '" + textField.getText() + "'");
                        textField.requestFocus();
                        textField.selectAll();
                        return null;
                    }
                }
            }
        }
        return matrix;
    }

    private void displayMatrixResult(double[][] matrix, String title) {
        StringBuilder sb = new StringBuilder(title + "\n\n");
        for (double[] row : matrix) {
            for (int j = 0; j < row.length; j++) {
                // Используем String.format для контроля количества знаков после запятой
                // и выравнивания. %10.3f означает 10 символов всего, 3 после запятой.
                sb.append(String.format("%10.3f", row[j]));
                if (j < row.length - 1) {
                    sb.append("\t"); // Табуляция между элементами
                }
            }
            sb.append("\n");
        }
        resultLabel.setText(sb.toString());
    }
    
    // --- Заглушки ---
    private double[][] mockTranspose(double[][] matrix) {
        if (matrix == null || matrix.length == 0) return new double[0][0];
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    private double[][] mockInverse(double[][] matrix) {
        if (matrix == null || matrix.length == 0) return new double[0][0];
        int n = matrix.length;
        if (n != matrix[0].length) throw new IllegalArgumentException("Матрица не квадратная для обращения.");

        if (n == 1) {
            if (Math.abs(matrix[0][0]) < 1e-9) throw new ArithmeticException("Определитель равен нулю (1x1).");
            return new double[][]{{1.0 / matrix[0][0]}};
        }
        if (n == 2) {
            double a = matrix[0][0], b = matrix[0][1], c = matrix[1][0], d = matrix[1][1];
            double det = a * d - b * c;
            if (Math.abs(det) < 1e-9) throw new ArithmeticException("Матрица вырождена (определитель равен нулю).");
            return new double[][]{{d / det, -b / det}, {-c / det, a / det}};
        }
        throw new UnsupportedOperationException("Обращение для матриц размером больше 2х2 пока не реализовано (заглушка).");
    }
}