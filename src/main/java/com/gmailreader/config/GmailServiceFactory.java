package com.gmailreader.config;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.auth.oauth2.Credential;
import org.springframework.stereotype.Component;

@Component
public class GmailServiceFactory {

    private static final String APPLICATION_NAME = "Gmail Reader API";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public Gmail criarServicoGmail(Credential credential) throws Exception {
        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential
        )
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}

