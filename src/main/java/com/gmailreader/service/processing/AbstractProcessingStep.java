package com.gmailreader.service.processing;

import com.google.api.services.gmail.model.Message;

public abstract class AbstractProcessingStep implements ProcessingStep {
    
    private ProcessingStep nextStep;
    
    @Override
    public ProcessingStep setNext(ProcessingStep nextStep) {
        this.nextStep = nextStep;
        return nextStep;
    }
    
    @Override
    public void processar(Message message, ProcessingContext context) throws Exception {

        executarProcessamento(message, context);
        

        if (nextStep != null) {
            nextStep.processar(message, context);
        }
    }

    protected abstract void executarProcessamento(Message message, ProcessingContext context) throws Exception;
}
