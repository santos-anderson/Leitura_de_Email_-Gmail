package com.gmailreader.service.processing;

import lombok.Data;


@Data
public class ProcessingContext {
    
    private String emailId;
    private String convertedJson;
    private boolean alreadyProcessed;
    private Exception lastError;
    
    public ProcessingContext(String emailId) {
        this.emailId = emailId;
        this.alreadyProcessed = false;
    }
}
