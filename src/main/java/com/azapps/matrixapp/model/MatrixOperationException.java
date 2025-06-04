package com.azapps.matrixapp.model;

public class MatrixOperationException extends Exception {

    public MatrixOperationException(String message) {
        super(message);
    }

    public MatrixOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}