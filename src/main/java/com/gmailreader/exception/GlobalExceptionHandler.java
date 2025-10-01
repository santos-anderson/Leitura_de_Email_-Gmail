package com.gmailreader.exception;

import com.gmailreader.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GmailReaderException.class)
    public ResponseEntity<ApiResponse<Void>> tratarExcecaoGmailReader(GmailReaderException ex) {
        logger.error("Erro no Gmail Reader: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.erro(
            "Falha na operação do Gmail Reader", 
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Void>> tratarExcecaoIO(IOException ex) {
        logger.error("Erro de I/O: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.erro(
            "Erro de entrada/saída", 
            "Falha na comunicação com serviços externos"
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> tratarExcecaoGenerica(Exception ex) {
        logger.error("Erro interno do servidor: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.erro(
            "Erro interno do servidor", 
            "Ocorreu um erro inesperado. Tente novamente mais tarde."
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
