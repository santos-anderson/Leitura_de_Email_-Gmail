package com.gmailreader.config;

import com.gmailreader.service.GmailWatchService;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.auth.oauth2.GoogleCredentials;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigInteger;

@Component
public class PubSubMessageReceiver {

    private static final Logger logger = LoggerFactory.getLogger(PubSubMessageReceiver.class);

    private final GmailWatchService gmailWatchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gcp.project.id}")
    private String projectId;

    @Value("${gcp.pubsub.subscription}")
    private String subscriptionId;

    @Value("${gcp.pubsub.credentials.path}")
    private String credentialsPath;

    public PubSubMessageReceiver(GmailWatchService gmailWatchService) {
        this.gmailWatchService = gmailWatchService;
    }

    @PostConstruct
    public void iniciarSubscriber() {
        try {
            ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);

            GoogleCredentials credentials;
            try (InputStream serviceAccountStream = new ClassPathResource(credentialsPath).getInputStream()) {
                credentials = GoogleCredentials.fromStream(serviceAccountStream);
            }

            Subscriber subscriber = Subscriber.newBuilder(subscriptionName, (PubsubMessage message, AckReplyConsumer consumer) -> {
                        String rawJson = message.getData().toStringUtf8();
                        logger.info("Mensagem recebida do Pub/Sub: {}", rawJson);

                        try {
                            BigInteger historyId = extrairHistoryIdDaNotificacao(rawJson);

                            if (historyId != null) {
                                gmailWatchService.processarEmailsDoHistorico(historyId);
                                consumer.ack();
                                logger.info("Mensagem processada e ack enviada para Pub/Sub.");
                            } else {
                                logger.warn("Mensagem ignorada (sem historyId): {}", rawJson);
                                consumer.ack();
                            }

                        } catch (Exception e) {
                            logger.error("Erro inesperado ao processar mensagem: {}", e.getMessage(), e);
                            consumer.ack();
                        }
                    })
                    .setCredentialsProvider(() -> credentials)
                    .build();

            subscriber.startAsync().awaitRunning();
            logger.info("Subscriber iniciado para assinatura: {}", subscriptionId);

        } catch (Exception e) {
            logger.error("Erro ao inicializar Pub/Sub subscriber: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private BigInteger extrairHistoryIdDaNotificacao(String notificationJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(notificationJson);

            if (jsonNode.has("historyId")) {
                return new BigInteger(jsonNode.get("historyId").asText());
            }
        } catch (Exception e) {
            logger.error("Erro ao parsear JSON da notificação: {}", e.getMessage(), e);
        }

        return null;
    }
}

