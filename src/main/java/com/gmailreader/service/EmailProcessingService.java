package com.gmailreader.service;

import com.gmailreader.exception.GmailReaderException;
import com.gmailreader.service.processing.ProcessingContext;
import com.gmailreader.service.processing.ProcessingStep;
import com.google.api.services.gmail.model.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class EmailProcessingService {

    private final GmailReaderService gmailReaderService;
    private final ProcessingStep processingChain;

    public EmailProcessingService(
            GmailReaderService gmailReaderService,
            ProcessingStep processingChain) {

        this.gmailReaderService = gmailReaderService;
        this.processingChain = processingChain;
    }

    public void processarEmails() {
        try {
            List<Message> emails = gmailReaderService.listarEmails();

            for (Message email : emails) {
                processarEmail(email);
            }
        } catch (IOException e) {
            throw new GmailReaderException(
                    "Falha na comunicação com Gmail API", e
            );
        } catch (GmailReaderException e) {
            throw e;
        } catch (Exception e) {
            throw new GmailReaderException(
                    "Erro inesperado durante o processamento de emails", e
            );
        }
    }

    public void processarEmail(Message email) {
        try {
            ProcessingContext context = new ProcessingContext(email.getId());
            processingChain.processar(email, context);
        } catch (Exception e) {
            throw new GmailReaderException(
                    "Falha ao processar email ID: " + email.getId(), e
            );
        }
    }
}
