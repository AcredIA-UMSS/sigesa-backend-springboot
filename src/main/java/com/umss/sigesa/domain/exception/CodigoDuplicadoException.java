package com.umss.sigesa.domain.exception;

public class CodigoDuplicadoException extends RuntimeException {

    public CodigoDuplicadoException(String codigo) {
        super("Ya existe una fase con el código '" + codigo + "'");
    }
}
