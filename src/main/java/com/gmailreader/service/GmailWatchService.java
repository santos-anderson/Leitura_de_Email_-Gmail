package com.gmailreader.service;

import com.google.api.services.gmail.model.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.List;

@Service
public class GmailWatchService {

    private static final Logger logger = LoggerFactory.getLogger(GmailWatchService.class);

    private final EmailProcessingService emailProcessingService;
    private final GmailWatchManager gmailWatchManager;
    private final GmailHistoryService gmailHistoryService;

    public GmailWatchService(EmailProcessingService emailProcessingService,
                             GmailWatchManager gmailWatchManager,
                             GmailHistoryService gmailHistoryService) {
        this.emailProcessingService = emailProcessingService;
        this.gmailWatchManager = gmailWatchManager;
        this.gmailHistoryService = gmailHistoryService;
    }

    @PostConstruct
    public void iniciarWatchAoSubir() {
        try {
            iniciarWatch();
        } catch (Exception e) {
            logger.error("Erro ao iniciar Watch do Gmail: {}", e.getMessage(), e);
        }
    }

    public void iniciarWatch() {
        gmailWatchManager.iniciarWatch();
    }


    public void processarEmailsDoHistorico(BigInteger historyId) {
        try {
            List<History> historyList = gmailHistoryService.buscarHistorico(historyId);

            if (!historyList.isEmpty()) {
                logger.info("Encontradas {} alterações no histórico para historyId {}. Processando emails não lidos.",
                        historyList.size(), historyId);
            } else {
                logger.info("Nenhum email novo encontrado no histórico para historyId {}. Processando emails manualmente.", historyId);
            }

            emailProcessingService.processarEmails();
<<<<<<< HEAD
            logger.info("Emails processados com sucesso para historyId: {}", historyId);

        } catch (Exception e) {
            logger.error("Erro inesperado ao processar emails para historyId {}: {}", historyId, e.getMessage(), e);

=======

        } catch (Exception e) {
            logger.error("Erro inesperado ao processar emails para historyId {}: {}", historyId, e.getMessage(), e);
            emailProcessingService.processarEmails();
>>>>>>> 0f9a767be6f6d597b3db65ad04ea1f063a645f9a
        }
    }

}
