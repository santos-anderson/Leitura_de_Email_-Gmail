package com.gmailreader.service.processing;

public class ProcessingContext {
    
    private String emailId;
    private String convertedJson;
    private boolean alreadyProcessed;
    
    public ProcessingContext(String emailId) {
        this.emailId = emailId;
        this.alreadyProcessed = false;
    }

    public String obterJsonConvertido() {
        return convertedJson;
    }
    
    public void definirJsonConvertido(String convertedJson) {
        this.convertedJson = convertedJson;
    }
    
    public boolean jaFoiProcessado() {
        return alreadyProcessed;
    }
    
    public void definirJaProcessado(boolean alreadyProcessed) {
        this.alreadyProcessed = alreadyProcessed;
    }
}
