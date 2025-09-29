package com.gmailreader.service.processing;

import com.gmailreader.repository.ProcessedEmailFileRepository;
import com.google.api.services.gmail.model.Message;
import org.springframework.stereotype.Component;

@Component
public class CheckAlreadyProcessedStep extends AbstractProcessingStep {

    private final ProcessedEmailFileRepository processedEmailRepository;

    public CheckAlreadyProcessedStep(ProcessedEmailFileRepository processedEmailRepository) {
        this.processedEmailRepository = processedEmailRepository;
    }

    @Override
    protected void executarProcessamento(Message message, ProcessingContext context) throws Exception {
        boolean alreadyProcessed = processedEmailRepository.emailJaProcessado(message.getId());
        context.definirJaProcessado(alreadyProcessed);
    }

    @Override
    public void processar(Message message, ProcessingContext context) throws Exception {
        executarProcessamento(message, context);
        if (!context.jaFoiProcessado()) {
            super.processar(message, context);
        }
    }
}

