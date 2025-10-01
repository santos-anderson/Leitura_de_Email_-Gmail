package com.gmailreader.service;

import com.google.api.services.gmail.model.Message;
import com.gmailreader.service.provider.EmailProvider;
import com.gmailreader.service.provider.EmailSearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
public class GmailReaderService {

    private static final Logger logger = LoggerFactory.getLogger(GmailReaderService.class);

    private final EmailProvider emailProvider;
    
    @Value("${gmail.user.id:me}")
    private String userId;
    
    @Value("${gmail.search.labels:INBOX}")
    private List<String> defaultLabels;
    
    @Value("${gmail.search.max-results:#{null}}")
    private Integer maxResults;

    public GmailReaderService(EmailProvider emailProvider) {
        this.emailProvider = emailProvider;
    }

    public List<Message> listarEmails() throws IOException {
        logger.info("Listando emails com configurações padrão para usuário: {}", userId);
        
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId(userId)
                .labelIds(defaultLabels)
                .maxResults(maxResults)
                .build();
        
        return listarEmails(criteria);
    }

    public List<Message> listarEmails(EmailSearchCriteria criteria) throws IOException {
        logger.info("Listando emails com critérios personalizados: {}", criteria);

        if (!emailProvider.isDisponivel()) {
            throw new IOException("Provedor de email " + emailProvider.getNomeProvedor() + " não está disponível");
        }
        
        try {
            List<Message> emails = emailProvider.listarEmails(criteria);
            logger.info("Listagem concluída: {} emails encontrados usando provedor {}", 
                       emails.size(), emailProvider.getNomeProvedor());
            return emails;
            
        } catch (IOException e) {
            logger.error("Erro ao listar emails: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<Message> listarEmailsExcluindo(List<String> idsExcluidos) throws IOException {
        logger.info("Listando emails excluindo {} IDs", idsExcluidos.size());
        
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId(userId)
                .labelIds(defaultLabels)
                .idsExcluidos(idsExcluidos)
                .maxResults(maxResults)
                .build();
        
        return listarEmails(criteria);
    }

    public String obterInfoProvedor() {
        String status = emailProvider.isDisponivel() ? "disponível" : "indisponível";
        return String.format("Provedor: %s (%s)", emailProvider.getNomeProvedor(), status);
    }
}
