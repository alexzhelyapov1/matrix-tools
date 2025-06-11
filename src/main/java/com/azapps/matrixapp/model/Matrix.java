package com.azapps.matrixapp.model;
import java.util.Arrays;

public class Matrix {
    private final double[][] data;
    private final int rows;
    private final int cols;

    public Matrix(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Размеры матрицы должны быть положительными.");
        }
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }

    public Matrix(double[][] data) {
        if (data == null || data.length == 0 || data[0].length == 0) {
            throw new IllegalArgumentException("Входные данные для матрицы не могут быть пустыми или null.");
        }
        this.rows = data.length;
        this.cols = data[0].length;
        this.data = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            if (data[i].length != cols) {
                throw new IllegalArgumentException("Все строки матрицы должны иметь одинаковую длину.");
            }
            System.arraycopy(data[i], 0, this.data[i], 0, cols);
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public double getElement(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Индекс выходит за пределы матрицы.");
        }
        return data[row][col];
    }

    public void setElement(int row, int col, double value) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Индекс выходит за пределы матрицы.");
        }
        data[row][col] = value;
    }

    public double[][] getData() {

        double[][] copy = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(data[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

    public Matrix transpose() {
        Matrix result = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.setElement(j, i, this.data[i][j]);
            }
        }
        return result;
    }

    public Matrix inverse() throws MatrixOperationException {
        if (rows != cols) {
            throw new MatrixOperationException("Матрица должна быть квадратной для нахождения обратной.");
        }

        int n = rows;
        double[][] augmentedMatrix = new double[n][2 * n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augmentedMatrix[i][j] = data[i][j];
            }
            augmentedMatrix[i][i + n] = 1;
        }

        for (int i = 0; i < n; i++) {

            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(augmentedMatrix[k][i]) > Math.abs(augmentedMatrix[maxRow][i])) {
                    maxRow = k;
                }
            }

            double[] temp = augmentedMatrix[i];
            augmentedMatrix[i] = augmentedMatrix[maxRow];
            augmentedMatrix[maxRow] = temp;

            if (Math.abs(augmentedMatrix[i][i]) < 1e-10) {
                throw new MatrixOperationException("Матрица вырождена (определитель равен нулю), обратной матрицы не существует.");
            }

            double pivot = augmentedMatrix[i][i];
            for (int j = i; j < 2 * n; j++) {
                augmentedMatrix[i][j] /= pivot;
            }

            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = augmentedMatrix[k][i];
                    for (int j = i; j < 2 * n; j++) {
                        augmentedMatrix[k][j] -= factor * augmentedMatrix[i][j];
                    }
                }
            }
        }

        double[][] inverseData = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inverseData[i][j] = augmentedMatrix[i][j + n];
            }
        }
        return new Matrix(inverseData);
    }

    public double determinant() throws MatrixOperationException {
        if (rows != cols) {
            throw new MatrixOperationException("Определитель можно вычислить только для квадратной матрицы.");
        }
        return calculateDeterminant(this.data);
    }

    private double calculateDeterminant(double[][] matrixData) {
        int n = matrixData.length;
        if (n == 1) {
            return matrixData[0][0];
        }
        if (n == 2) {
            return matrixData[0][0] * matrixData[1][1] - matrixData[0][1] * matrixData[1][0];
        }

        double det = 0;
        for (int j = 0; j < n; j++) {
            det += Math.pow(-1, j) * matrixData[0][j] * calculateDeterminant(getSubmatrix(matrixData, 0, j));
        }
        return det;
    }


    private double[][] getSubmatrix(double[][] matrixData, int excluding_row, int excluding_col) {
        int n = matrixData.length;
        double[][] submatrix = new double[n - 1][n - 1];
        int r = -1;
        for (int i = 0; i < n; i++) {
            if (i == excluding_row) {
                continue;
            }
            r++;
            int c = -1;
            for (int j = 0; j < n; j++) {
                if (j == excluding_col) {
                    continue;
                }
                c++;
                submatrix[r][c] = matrixData[i][j];
            }
        }
        return submatrix;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            sb.append(Arrays.toString(data[i])).append("\n");
        }
        return sb.toString();
    }
}