package com.gmailreader.service.processing;

import com.gmailreader.service.EmailToJsonService;
import com.google.api.services.gmail.model.Message;
import org.springframework.stereotype.Component;

@Component
public class ConvertEmailStep extends AbstractProcessingStep {
    
    private final EmailToJsonService emailToJsonService;
    
    public ConvertEmailStep(EmailToJsonService emailToJsonService) {
        this.emailToJsonService = emailToJsonService;
    }
    
    @Override
    protected void doProcess(Message message, ProcessingContext context) throws Exception {
        String json = emailToJsonService.converter(message);
        context.setConvertedJson(json);
    }
}
