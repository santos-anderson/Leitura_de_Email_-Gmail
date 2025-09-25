package com.gmailreader.config;

import com.google.api.services.gmail.Gmail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GmailConfig {

    private final OAuthManager oAuthManager;
    private final GmailServiceFactory gmailServiceFactory;

    public GmailConfig(OAuthManager oAuthManager, GmailServiceFactory gmailServiceFactory) {
        this.oAuthManager = oAuthManager;
        this.gmailServiceFactory = gmailServiceFactory;
    }

    @Bean
    public Gmail gmail() throws Exception {
        return gmailServiceFactory.createGmailService(oAuthManager.authorize());
    }
}
