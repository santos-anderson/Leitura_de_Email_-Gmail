package com.gmailreader.service;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.pubsub.v1.PubsubMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class PubSubMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PubSubMessageProcessor.class);

    private final NotificationParser notificationParser;
    private final GmailWatchService gmailWatchService;

    public PubSubMessageProcessor(NotificationParser notificationParser, 
                                  GmailWatchService gmailWatchService) {
        this.notificationParser = notificationParser;
        this.gmailWatchService = gmailWatchService;
    }

    public void processarMensagem(PubsubMessage message, AckReplyConsumer consumer) {
        String rawJson = message.getData().toStringUtf8();
        logger.info("Mensagem recebida do Pub/Sub: {}", rawJson);

        try {
            BigInteger historyId = notificationParser.extrairHistoryId(rawJson);

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
    }
}
