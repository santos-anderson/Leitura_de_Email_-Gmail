package com.gmailreader.service.processing;

import com.gmailreader.repository.ProcessedEmailFileRepository;
import com.google.api.services.gmail.model.Message;
import org.springframework.stereotype.Component;

@Component
public class MarkAsProcessedStep extends AbstractProcessingStep {
    
    private final ProcessedEmailFileRepository processedEmailRepository;
    
    public MarkAsProcessedStep(ProcessedEmailFileRepository processedEmailRepository) {
        this.processedEmailRepository = processedEmailRepository;
    }
    
    @Override
    protected void doProcess(Message message, ProcessingContext context) throws Exception {
        processedEmailRepository.marcarComoProcessado(message.getId());
    }
}
