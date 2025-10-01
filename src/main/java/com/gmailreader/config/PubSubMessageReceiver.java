package com.gmailreader.config;

import com.gmailreader.exception.GmailReaderException;
import com.gmailreader.service.PubSubMessageProcessor;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PubSubMessageReceiver {

    private static final Logger logger = LoggerFactory.getLogger(PubSubMessageReceiver.class);

    private final PubSubMessageProcessor messageProcessor;
    private final PubSubCredentialsManager credentialsManager;

    @Value("${gcp.project.id}")
    private String projectId;

    @Value("${gcp.pubsub.subscription}")
    private String subscriptionId;

    public PubSubMessageReceiver(PubSubMessageProcessor messageProcessor,
                                 PubSubCredentialsManager credentialsManager) {
        this.messageProcessor = messageProcessor;
        this.credentialsManager = credentialsManager;
    }

    @PostConstruct
    public void iniciarSubscriber() {
        try {
            ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
            GoogleCredentials credentials = credentialsManager.obterCredenciais();

            Subscriber subscriber = Subscriber.newBuilder(subscriptionName, messageProcessor::processarMensagem)
                    .setCredentialsProvider(() -> credentials)
                    .build();

            subscriber.startAsync().awaitRunning();
            logger.info("Subscriber iniciado para assinatura: {}", subscriptionId);

        } catch (Exception e) {
            logger.error("Erro ao inicializar Pub/Sub subscriber: {}", e.getMessage(), e);
            throw new GmailReaderException("Falha ao inicializar Pub/Sub subscriber", e);
        }
    }
}

