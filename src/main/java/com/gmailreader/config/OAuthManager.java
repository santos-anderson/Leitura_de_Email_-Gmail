package com.gmailreader.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.GmailScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Component
public class OAuthManager {

    private static final Logger logger = LoggerFactory.getLogger(OAuthManager.class);

    @Value("${oauth.credentials.file.path}")
    private String credentialsFilePath;

    @Value("${oauth.tokens.directory.path}")
    private String tokensDirectoryPath;

    @Value("${oauth.server.port}")
    private int serverPort;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_MODIFY);

    public Credential authorize() throws Exception {

        FileInputStream in = new FileInputStream(credentialsFilePath);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        )
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(serverPort)
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        // Verifica e renova token se necessário
        credential = verificarERenovarToken(credential);

        logger.info("OAuth2 autorizado para usuário.");
        logger.info("Access token expira em: {} segundos", credential.getExpiresInSeconds());
        logger.info("Refresh token disponível: {}", credential.getRefreshToken() != null);

        return credential;
    }

    private Credential verificarERenovarToken(Credential credential) throws Exception {
        if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 300) {
            logger.warn("Token expira em {} segundos. Renovando automaticamente...", credential.getExpiresInSeconds());
            
            if (credential.refreshToken()) {
                logger.info("Token renovado com sucesso! Nova expiração em: {} segundos", credential.getExpiresInSeconds());
            } else {
                logger.error("Falha ao renovar token. Refresh token pode estar inválido.");
                throw new RuntimeException("Não foi possível renovar o access token");
            }
        } else if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() < 0) {
            logger.error("Token já expirou há {} segundos. Forçando renovação...", Math.abs(credential.getExpiresInSeconds()));
            
            if (credential.refreshToken()) {
                logger.info("Token expirado renovado com sucesso! Nova expiração em: {} segundos", credential.getExpiresInSeconds());
            } else {
                logger.error("Falha ao renovar token expirado. Pode ser necessário reautorizar.");
                throw new RuntimeException("Token expirado e não foi possível renovar");
            }
        }
        
        return credential;
    }
}




