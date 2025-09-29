package com.gmailreader.service.processing;

import com.gmailreader.service.GmailModifierService;
import com.google.api.services.gmail.model.Message;
import org.springframework.stereotype.Component;

@Component
public class MarkAsReadStep extends AbstractProcessingStep {
    
    private final GmailModifierService gmailModifierService;
    
    public MarkAsReadStep(GmailModifierService gmailModifierService) {
        this.gmailModifierService = gmailModifierService;
    }
    
    @Override
    protected void executarProcessamento(Message message, ProcessingContext context) throws Exception {
        gmailModifierService.marcarComoLido(message.getId());
    }
}
