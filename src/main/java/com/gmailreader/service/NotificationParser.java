package com.gmailreader.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class NotificationParser {

    private static final Logger logger = LoggerFactory.getLogger(NotificationParser.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BigInteger extrairHistoryId(String notificationJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(notificationJson);

            if (jsonNode.has("historyId")) {
                BigInteger historyId = new BigInteger(jsonNode.get("historyId").asText());
                logger.debug("HistoryId extraído com sucesso: {}", historyId);
                return historyId;
            } else {
                logger.warn("Campo 'historyId' não encontrado na notificação");
            }
        } catch (Exception e) {
            logger.error("Erro ao parsear JSON da notificação: {}", e.getMessage(), e);
        }

        return null;
    }
    public boolean isValidNotification(String notificationJson) {
        return extrairHistoryId(notificationJson) != null;
    }
}
