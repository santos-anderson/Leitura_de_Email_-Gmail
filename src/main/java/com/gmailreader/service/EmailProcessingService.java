package com.gmailreader.service;

import com.gmailreader.exception.EmailProcessingException;
import com.gmailreader.service.processing.*;
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
            CheckAlreadyProcessedStep checkStep,
            ConvertEmailStep convertStep,
            SaveEmailStep saveStep,
            MarkAsProcessedStep markProcessedStep,
            MarkAsReadStep markReadStep) {

        this.gmailReaderService = gmailReaderService;

        // Configura a cadeia de processamento
        this.processingChain = checkStep;
        checkStep.setNext(convertStep)
                .setNext(saveStep)
                .setNext(markProcessedStep)
                .setNext(markReadStep);
    }

    public void processarEmails() {
        try {
            List<Message> emails = gmailReaderService.listarEmails();

            for (Message email : emails) {
                processarEmail(email);
            }
        } catch (IOException e) {
            throw new EmailProcessingException(
                    "Falha na comunicação com Gmail API", e
            );
        } catch (EmailProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new EmailProcessingException(
                    "Erro inesperado durante o processamento de emails", e
            );
        }
    }

    public void processarEmail(Message email) {
        try {
            ProcessingContext context = new ProcessingContext(email.getId());
            processingChain.processar(email, context);
        } catch (Exception e) {
            throw new EmailProcessingException(
                    "Falha ao processar email ID: " + email.getId(), e
            );
        }
    }
}
