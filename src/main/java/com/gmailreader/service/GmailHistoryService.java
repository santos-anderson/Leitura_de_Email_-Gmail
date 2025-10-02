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
public class GmailHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(GmailHistoryService.class);

    private final GmailServiceManager gmailServiceManager;

    public GmailHistoryService(GmailServiceManager gmailServiceManager) {
        this.gmailServiceManager = gmailServiceManager;
    }

    public List<History> buscarHistorico(BigInteger startHistoryId) {
        try {
            Gmail gmailService = gmailServiceManager.getGmailService();
            
            List<History> historyList = gmailService.users().history().list("me")
                    .setStartHistoryId(startHistoryId)
                    .execute()
                    .getHistory();
                    
            List<History> resultado = historyList != null ? historyList : Collections.emptyList();
            
            logger.info("Buscado histórico para historyId {}: {} alterações encontradas", 
                       startHistoryId, resultado.size());
                       
            return resultado;
            
        } catch (Exception e) {
            logger.warn("Não foi possível buscar histórico do Gmail para historyId {}: {}. Retornando lista vazia.",
                    startHistoryId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean temAlteracoes(BigInteger startHistoryId) {
        List<History> historico = buscarHistorico(startHistoryId);
        return !historico.isEmpty();
    }
}
