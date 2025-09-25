package com.gmailreader.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GmailModifierService {

    private final Gmail gmailService;
    private final String userId = "me";

    public GmailModifierService(Gmail gmailService) {
        this.gmailService = gmailService;
    }

    public void marcarComoLido(String messageId) throws IOException {
        gmailService.users().messages().modify(userId, messageId,
                        new ModifyMessageRequest().setRemoveLabelIds(List.of("UNREAD")))
                .execute();
    }
}
