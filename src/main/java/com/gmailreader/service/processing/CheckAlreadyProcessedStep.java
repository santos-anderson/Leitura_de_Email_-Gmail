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
    protected void doProcess(Message message, ProcessingContext context) throws Exception {
        boolean alreadyProcessed = processedEmailRepository.emailJaProcessado(message.getId());
        context.setAlreadyProcessed(alreadyProcessed);
    }
    
    @Override
    public void process(Message message, ProcessingContext context) throws Exception {
        doProcess(message, context);

        if (!context.isAlreadyProcessed()) {
            super.process(message, context);
        }
    }
}
