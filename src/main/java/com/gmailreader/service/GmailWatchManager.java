package com.gmailreader.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.WatchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GmailWatchManager {

    private static final Logger logger = LoggerFactory.getLogger(GmailWatchManager.class);

    private final GmailServiceManager gmailServiceManager;

    @Value("${gmail.watch.topic.name:projects/gmailteste10/topics/gmail-notifications}")
    private String topicName;

    public GmailWatchManager(GmailServiceManager gmailServiceManager) {
        this.gmailServiceManager = gmailServiceManager;
    }

    public void iniciarWatch() {
        try {
            Gmail gmailService = gmailServiceManager.getGmailService();
            
            WatchRequest watchRequest = new WatchRequest()
                    .setTopicName(topicName);

            gmailService.users().watch("me", watchRequest).execute();
            logger.info("Watch do Gmail registrado com sucesso no tópico: {}", topicName);
        } catch (Exception e) {
            logger.error("Erro ao registrar Watch do Gmail: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao iniciar Watch do Gmail", e);
        }
    }

    public void pararWatch() {
        try {
            Gmail gmailService = gmailServiceManager.getGmailService();
            gmailService.users().stop("me").execute();
            logger.info("Watch do Gmail parado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao parar Watch do Gmail: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao parar Watch do Gmail", e);
        }
    }
}
