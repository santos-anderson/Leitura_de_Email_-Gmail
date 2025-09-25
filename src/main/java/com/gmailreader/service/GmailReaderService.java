package com.gmailreader.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GmailReaderService {

    private final Gmail gmailService;
    private final String userId = "me";

    public GmailReaderService(Gmail gmailService) {
        this.gmailService = gmailService;
    }

    public List<Message> listarEmails() throws IOException {
        return listarMensagens(userId, new ArrayList<>());
    }

    private List<Message> listarMensagens(String userId, List<String> idsExcluidos) throws IOException {
        List<Message> emails = new ArrayList<>();
        String pageToken = null;

        do {
            ListMessagesResponse response = gmailService.users().messages()
                    .list(userId)
                    .setLabelIds(List.of("INBOX"))
                    .setPageToken(pageToken)
                    .execute();

            if (response.getMessages() != null) {
                for (Message message : response.getMessages()) {

                    if (idsExcluidos.isEmpty() || !idsExcluidos.contains(message.getId())) {
                        Message emailCompleto = gmailService.users().messages()
                                .get(userId, message.getId())
                                .execute();
                        emails.add(emailCompleto);
                    }
                }
            }

            pageToken = response.getNextPageToken();

        } while (pageToken != null);

        return emails;
    }
}


