package com.gmailreader.service.pagination;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.gmailreader.service.provider.EmailSearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
public class PaginationHandler {

    private static final Logger logger = LoggerFactory.getLogger(PaginationHandler.class);


    public List<String> buscarMensagensPaginadas(Gmail gmailService, EmailSearchCriteria criteria) throws IOException {
        logger.debug("Iniciando busca paginada com critérios: {}", criteria);
        
        List<String> messageIds = new ArrayList<>();
        String pageToken = null;
        int paginasProcessadas = 0;

        do {
            logger.debug("Processando página {} (token: {})", paginasProcessadas + 1, pageToken);
            
            ListMessagesResponse response = executarBuscaPagina(gmailService, criteria, pageToken);
            
            if (response.getMessages() != null) {
                int mensagensNaPagina = response.getMessages().size();
                logger.debug("Encontradas {} mensagens na página {}", mensagensNaPagina, paginasProcessadas + 1);
                
                for (Message message : response.getMessages()) {
                    if (deveIncluirMensagem(message.getId(), criteria.getIdsExcluidos())) {
                        messageIds.add(message.getId());
                    }
                }
            } else {
                logger.debug("Nenhuma mensagem encontrada na página {}", paginasProcessadas + 1);
            }

            pageToken = response.getNextPageToken();
            paginasProcessadas++;
            
        } while (pageToken != null);

        logger.info("Busca paginada concluída: {} mensagens encontradas em {} páginas", 
                   messageIds.size(), paginasProcessadas);
        
        return messageIds;
    }


    private ListMessagesResponse executarBuscaPagina(Gmail gmailService, 
                                                   EmailSearchCriteria criteria, 
                                                   String pageToken) throws IOException {
        
        Gmail.Users.Messages.List request = gmailService.users().messages()
                .list(criteria.getUserId())
                .setLabelIds(criteria.getLabelIds())
                .setPageToken(pageToken);

        if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
            request.setQ(criteria.getQuery());
        }

        if (criteria.getMaxResults() != null) {
            request.setMaxResults(Long.valueOf(criteria.getMaxResults()));
        }

        return request.execute();
    }

    private boolean deveIncluirMensagem(String messageId, List<String> idsExcluidos) {
        boolean incluir = idsExcluidos.isEmpty() || !idsExcluidos.contains(messageId);
        
        if (!incluir) {
            logger.debug("Mensagem {} excluída conforme critérios", messageId);
        }
        
        return incluir;
    }
}
