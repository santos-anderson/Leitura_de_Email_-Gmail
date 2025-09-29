package com.gmailreader.service.processing;

import com.gmailreader.service.StorageService;
import com.google.api.services.gmail.model.Message;
import org.springframework.stereotype.Component;

@Component
public class SaveEmailStep extends AbstractProcessingStep {
    
    private final StorageService storageService;
    
    public SaveEmailStep(StorageService storageService) {
        this.storageService = storageService;
    }
    
    @Override
    protected void executarProcessamento(Message message, ProcessingContext context) throws Exception {
        if (context.obterJsonConvertido() != null) {
            storageService.salvarEmail(context.obterJsonConvertido());
        }
    }
}
