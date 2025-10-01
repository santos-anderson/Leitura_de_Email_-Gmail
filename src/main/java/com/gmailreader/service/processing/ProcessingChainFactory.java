package com.gmailreader.service.processing;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessingChainFactory {

    public ProcessingStep criarCadeia(List<ProcessingStep> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("Lista de steps não pode ser vazia");
        }

        ProcessingStep primeiroStep = steps.get(0);
        ProcessingStep stepAtual = primeiroStep;

        for (int i = 1; i < steps.size(); i++) {
            ProcessingStep proximoStep = steps.get(i);
            stepAtual.setNext(proximoStep);
            stepAtual = proximoStep;
        }

        return primeiroStep;
    }
}
