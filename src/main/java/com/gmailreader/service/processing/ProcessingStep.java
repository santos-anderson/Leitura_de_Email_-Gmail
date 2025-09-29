package com.gmailreader.service.processing;

import com.google.api.services.gmail.model.Message;


public interface ProcessingStep {

    void processar(Message message, ProcessingContext context) throws Exception;

    ProcessingStep setNext(ProcessingStep nextStep);
}
