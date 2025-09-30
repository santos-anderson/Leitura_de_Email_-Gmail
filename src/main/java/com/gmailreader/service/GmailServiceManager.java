package com.gmailreader.service;

import com.gmailreader.config.OAuthManager;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class GmailServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(GmailServiceManager.class);
    private static final String APPLICATION_NAME = "gmailreader";

    private final OAuthManager oAuthManager;
    private Gmail gmailService;

    public GmailServiceManager(OAuthManager oAuthManager) {
        this.oAuthManager = oAuthManager;
    }

    @PostConstruct
    public void inicializarGmailService() {
        try {
            Credential credential = oAuthManager.authorize();
            this.gmailService = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName(APPLICATION_NAME).build();
            
            logger.info("Gmail Service inicializado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao inicializar Gmail Service: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na inicialização do Gmail Service", e);
        }
    }

    @Bean
    public Gmail gmail() {
        return getGmailService();
    }

    public Gmail getGmailService() {
        if (gmailService == null) {
            throw new IllegalStateException("Gmail Service não foi inicializado");
        }
        return gmailService;
    }
}
