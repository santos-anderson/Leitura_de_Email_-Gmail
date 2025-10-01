package com.gmailreader.config;

import com.gmailreader.exception.GmailReaderException;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;

@Component
public class PubSubCredentialsManager {

    private static final Logger logger = LoggerFactory.getLogger(PubSubCredentialsManager.class);

    @Value("${gcp.pubsub.credentials.path}")
    private String credentialsPath;

    public GoogleCredentials obterCredenciais() {
        try (InputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
            logger.info("Credenciais do Pub/Sub carregadas com sucesso");
            return credentials;
        } catch (Exception e) {
            logger.error("Erro ao carregar credenciais do Pub/Sub: {}", e.getMessage(), e);
            throw new GmailReaderException("Falha ao carregar credenciais do Pub/Sub", e);
        }
    }
}

