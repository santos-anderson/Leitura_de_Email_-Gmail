package com.gmailreader.controller;

import com.gmailreader.dto.ApiResponse;
import com.gmailreader.service.EmailProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private final EmailProcessingService emailProcessingService;

    public EmailController(EmailProcessingService emailProcessingService) {
        this.emailProcessingService = emailProcessingService;
    }

    @GetMapping("/processar-emails")
    public ResponseEntity<ApiResponse<String>> processarEmails() {
        emailProcessingService.processarEmails();
        
        ApiResponse<String> response = ApiResponse.sucesso(
            "Emails processados com sucesso!",
            "Processamento concluído"
        );
        
        return ResponseEntity.ok(response);
    }
}
