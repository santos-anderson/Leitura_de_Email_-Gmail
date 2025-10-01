package com.gmailreader.service.provider;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.gmailreader.service.pagination.PaginationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
public class GmailEmailProvider implements EmailProvider {

    private static final Logger logger = LoggerFactory.getLogger(GmailEmailProvider.class);
    private static final String PROVIDER_NAME = "Gmail";

    private final Gmail gmailService;
    private final PaginationHandler paginationHandler;
    
    @Value("${gmail.provider.progress.log.interval:10}")
    private int progressLogInterval;

    public GmailEmailProvider(Gmail gmailService, PaginationHandler paginationHandler) {
        this.gmailService = gmailService;
        this.paginationHandler = paginationHandler;
    }

    @Override
    public List<Message> listarEmails(EmailSearchCriteria criteria) throws IOException {
        logger.info("Iniciando listagem de emails com critérios: {}", criteria);
        
        try {

            List<String> messageIds = paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

            List<Message> emails = buscarDetalhesCompletos(messageIds, criteria.getUserId());
            
            logger.info("Listagem concluída: {} emails recuperados", emails.size());
            return emails;
            
        } catch (IOException e) {
            logger.error("Erro ao listar emails do Gmail: {}", e.getMessage(), e);
            throw new IOException("Falha na comunicação com Gmail API", e);
        }
    }

    @Override
    public Message obterMensagemCompleta(String messageId, String userId) throws IOException {
        logger.debug("Buscando detalhes completos da mensagem: {}", messageId);
        
        try {
            Message message = gmailService.users().messages()
                    .get(userId, messageId)
                    .execute();
            
            logger.debug("Mensagem {} recuperada com sucesso", messageId);
            return message;
            
        } catch (IOException e) {
            logger.error("Erro ao buscar mensagem {}: {}", messageId, e.getMessage(), e);
            throw new IOException("Falha ao recuperar mensagem: " + messageId, e);
        }
    }

    @Override
    public boolean isDisponivel() {
        try {

            gmailService.users().getProfile("me").execute();
            logger.debug("Gmail API está disponível e configurada");
            return true;
            
        } catch (Exception e) {
            logger.warn("Gmail API não está disponível: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getNomeProvedor() {
        return PROVIDER_NAME;
    }

    private List<Message> buscarDetalhesCompletos(List<String> messageIds, String userId) throws IOException {
        logger.debug("Buscando detalhes completos de {} mensagens", messageIds.size());
        
        List<Message> emails = new ArrayList<>();
        int processadas = 0;
        
        for (String messageId : messageIds) {
            try {
                Message emailCompleto = obterMensagemCompleta(messageId, userId);
                emails.add(emailCompleto);
                processadas++;
                
                if (processadas % progressLogInterval == 0) {
                    logger.debug("Processadas {}/{} mensagens", processadas, messageIds.size());
                }
                
            } catch (IOException e) {
                logger.warn("Erro ao buscar detalhes da mensagem {}: {}", messageId, e.getMessage());

            }
        }
        
        logger.debug("Detalhes completos recuperados: {}/{} mensagens", emails.size(), messageIds.size());
        return emails;
    }
}
