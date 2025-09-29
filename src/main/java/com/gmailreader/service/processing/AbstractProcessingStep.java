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
    public void process(Message message, ProcessingContext context) throws Exception {

        doProcess(message, context);
        

        if (nextStep != null) {
            nextStep.process(message, context);
        }
    }

    protected abstract void doProcess(Message message, ProcessingContext context) throws Exception;
}
