package com.gmailreader.service;

import com.gmailreader.constants.GmailConstants;
import com.gmailreader.exception.GmailReaderException;
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

            gmailService.users().watch(GmailConstants.Gmail.CURRENT_USER_ID, watchRequest).execute();
            logger.info("Watch do Gmail registrado com sucesso no tópico: {}", topicName);
        } catch (Exception e) {
            logger.error("Erro ao registrar Watch do Gmail: {}", e.getMessage(), e);
            throw new GmailReaderException("Falha ao iniciar Watch do Gmail", e);
        }
    }
}
