package com.gmailreader.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Service
public class GmailWatchService {

    private static final Logger logger = LoggerFactory.getLogger(GmailWatchService.class);

    private final Gmail gmail;
    private final EmailProcessingService emailProcessingService;

    public GmailWatchService(Gmail gmail, EmailProcessingService emailProcessingService) {
        this.gmail = gmail;
        this.emailProcessingService = emailProcessingService;
    }

    public void processarEmailsDoHistorico(BigInteger historyId) {
        try {
            List<History> historyList = buscarHistoricoDoGmailSeguro(historyId);

            if (!historyList.isEmpty()) {
                logger.info("Encontradas {} alterações no histórico para historyId {}. Processando emails não lidos.",
                        historyList.size(), historyId);
            } else {
                logger.info("Nenhum email novo encontrado no histórico para historyId {}. Processando emails manualmente.", historyId);
            }

            emailProcessingService.processarEmails();

        } catch (Exception e) {
            logger.error("Erro inesperado ao processar emails para historyId {}: {}", historyId, e.getMessage(), e);

            emailProcessingService.processarEmails();
        }
    }

    private List<History> buscarHistoricoDoGmailSeguro(BigInteger startHistoryId) {
        try {
            List<History> historyList = gmail.users().history().list("me")
                    .setStartHistoryId(startHistoryId)
                    .execute()
                    .getHistory();
            return historyList != null ? historyList : Collections.emptyList();
        } catch (Exception e) {
            logger.warn("Não foi possível buscar histórico do Gmail para historyId {}: {}. Continuando com processamento manual.", startHistoryId, e.getMessage());
            return Collections.emptyList();
        }
    }
}

