package com.azapps.matrixapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

public class MatrixTest {

    private static final double DELTA = 1e-9;

    @Test
    void constructor_validDimensions_createsMatrix() {
        Matrix matrix = new Matrix(2, 3);
        assertEquals(2, matrix.getRows());
        assertEquals(3, matrix.getCols());

        for (int i = 0; i < matrix.getRows(); i++) {
            for (int j = 0; j < matrix.getCols(); j++) {
                assertEquals(0.0, matrix.getElement(i, j), DELTA);
            }
        }
    }

    @Test
    void constructor_fromValidData_createsMatrix() {
        double[][] data = {{1, 2}, {3, 4}};
        Matrix matrix = new Matrix(data);
        assertEquals(2, matrix.getRows());
        assertEquals(2, matrix.getCols());
        assertArrayEquals(data[0], matrix.getData()[0], DELTA);
        assertArrayEquals(data[1], matrix.getData()[1], DELTA);
    }

    @Test
    void constructor_zeroRows_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Matrix(0, 3));
    }

    @Test
    void constructor_zeroCols_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Matrix(2, 0));
    }

    @Test
    void constructor_fromNullData_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Matrix(null));
    }

    @Test
    void constructor_fromEmptyData_throwsIllegalArgumentException() {
        double[][] emptyData = {};
        assertThrows(IllegalArgumentException.class, () -> new Matrix(emptyData));
        double[][] emptyRowData = {{}};
        assertThrows(IllegalArgumentException.class, () -> new Matrix(emptyRowData));
    }

    @Test
    void constructor_fromJaggedArray_throwsIllegalArgumentException() {
        double[][] jaggedData = {{1, 2}, {3}};
        assertThrows(IllegalArgumentException.class, () -> new Matrix(jaggedData));
    }


    @Test
    void getElement_validIndices_returnsElement() {
        double[][] data = {{1, 2}, {3, 4}};
        Matrix matrix = new Matrix(data);
        assertEquals(3, matrix.getElement(1, 0), DELTA);
    }

    @Test
    void getElement_invalidRow_throwsIndexOutOfBoundsException() {
        Matrix matrix = new Matrix(2, 2);
        assertThrows(IndexOutOfBoundsException.class, () -> matrix.getElement(2, 0));
    }

    @Test
    void setElement_validIndices_setsElement() {
        Matrix matrix = new Matrix(2, 2);
        matrix.setElement(0, 1, 5.5);
        assertEquals(5.5, matrix.getElement(0, 1), DELTA);
    }

    @Test
    void getData_returnsCopyNotReference() {
        double[][] initialData = {{1, 2}, {3, 4}};
        Matrix matrix = new Matrix(initialData);
        double[][] retrievedData = matrix.getData();


        retrievedData[0][0] = 99;


        assertEquals(1, matrix.getElement(0, 0), DELTA, "Изменение копии не должно влиять на оригинал");
    }

    @Test
    void transpose_squareMatrix() {
        double[][] data = {{1, 2}, {3, 4}};
        Matrix matrix = new Matrix(data);
        Matrix transposed = matrix.transpose();
        double[][] expected = {{1, 3}, {2, 4}};
        assertArrayEquals(expected[0], transposed.getData()[0], DELTA);
        assertArrayEquals(expected[1], transposed.getData()[1], DELTA);
        assertEquals(2, transposed.getRows());
        assertEquals(2, transposed.getCols());
    }

    @Test
    void transpose_rectangularMatrix_2x3() {
        double[][] data = {{1, 2, 3}, {4, 5, 6}};
        Matrix matrix = new Matrix(data);
        Matrix transposed = matrix.transpose();
        double[][] expected = {{1, 4}, {2, 5}, {3, 6}};
        assertEquals(3, transposed.getRows());
        assertEquals(2, transposed.getCols());
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], transposed.getData()[i], DELTA);
        }
    }

    @Test
    void transpose_matrix1x1() {
        double[][] data = {{5}};
        Matrix matrix = new Matrix(data);
        Matrix transposed = matrix.transpose();
        double[][] expected = {{5}};
        assertArrayEquals(expected[0], transposed.getData()[0], DELTA);
        assertEquals(1, transposed.getRows());
        assertEquals(1, transposed.getCols());
    }


    @Test
    void inverse_2x2_nonSingularMatrix() throws MatrixOperationException {
        double[][] data = {{1, 2}, {3, 4}};
        Matrix matrix = new Matrix(data);
        Matrix inverted = matrix.inverse();
        double[][] expected = {{-2, 1}, {1.5, -0.5}};
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], inverted.getData()[i], DELTA);
        }
    }

    @Test
    void inverse_3x3_nonSingularMatrix() throws MatrixOperationException {
        double[][] data = {{2, -1, 0}, {-1, 2, -1}, {0, -1, 2}};
        Matrix matrix = new Matrix(data);
        Matrix inverted = matrix.inverse();
        double[][] expected = {{0.75, 0.5, 0.25}, {0.5, 1.0, 0.5}, {0.25, 0.5, 0.75}};
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], inverted.getData()[i], DELTA);
        }
    }

    @Test
    void inverse_identityMatrix() throws MatrixOperationException {
        double[][] data = {{1, 0}, {0, 1}};
        Matrix matrix = new Matrix(data);
        Matrix inverted = matrix.inverse();
        double[][] expected = {{1, 0}, {0, 1}};
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], inverted.getData()[i], DELTA);
        }
    }

    @Test
    void inverse_matrix1x1_nonZero() throws MatrixOperationException {
        double[][] data = {{5}};
        Matrix matrix = new Matrix(data);
        Matrix inverted = matrix.inverse();
        double[][] expected = {{0.2}};
        assertArrayEquals(expected[0], inverted.getData()[0], DELTA);
    }

    @Test
    void inverse_matrix1x1_zero_throwsException() {
        double[][] data = {{0}};
        Matrix matrix = new Matrix(data);
        assertThrows(MatrixOperationException.class, matrix::inverse, "Должно быть выброшено исключение для вырожденной матрицы 1x1");
    }

    @Test
    void inverse_nonSquareMatrix_throwsMatrixOperationException() {
        double[][] data = {{1, 2, 3}, {4, 5, 6}};
        Matrix matrix = new Matrix(data);
        assertThrows(MatrixOperationException.class, matrix::inverse);
    }

    @Test
    void inverse_singularMatrix_throwsMatrixOperationException() {
        double[][] data = {{1, 2}, {2, 4}};
        Matrix matrix = new Matrix(data);
        assertThrows(MatrixOperationException.class, matrix::inverse);
    }


    @Test
    void determinant_2x2_nonSingular() throws MatrixOperationException {
        double[][] data = {{1, 2}, {3, 4}};
        Matrix matrix = new Matrix(data);
        assertEquals(-2.0, matrix.determinant(), DELTA);
    }

    @Test
    void determinant_3x3_nonSingular() throws MatrixOperationException {
        double[][] data = {{2, -1, 0}, {-1, 2, -1}, {0, -1, 2}};
        Matrix matrix = new Matrix(data);
        assertEquals(4.0, matrix.determinant(), DELTA);
    }

    @Test
    void determinant_singularMatrix_isZero() throws MatrixOperationException {
        double[][] data = {{1, 2}, {2, 4}};
        Matrix matrix = new Matrix(data);
        assertEquals(0.0, matrix.determinant(), DELTA);
    }

    @Test
    void determinant_nonSquareMatrix_throwsMatrixOperationException() {
        double[][] data = {{1, 2, 3}, {4, 5, 6}};
        Matrix matrix = new Matrix(data);
        assertThrows(MatrixOperationException.class, matrix::determinant);
    }
}