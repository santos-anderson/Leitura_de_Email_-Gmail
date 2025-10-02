package com.gmailreader.service;

import com.gmailreader.config.OAuthManager;
import com.gmailreader.exception.GmailReaderException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GmailServiceManager")
class GmailServiceManagerTest {

    @Mock
    private OAuthManager oAuthManager;

    @Mock
    private Credential credential;

    private GmailServiceManager gmailServiceManager;

    @BeforeEach
    void setUp() {
        gmailServiceManager = new GmailServiceManager(oAuthManager);
    }

    @Test
    @DisplayName("Deve inicializar Gmail Service com sucesso")
    void deveInicializarGmailServiceComSucesso() throws Exception {

        when(oAuthManager.authorize()).thenReturn(credential);

        gmailServiceManager.inicializarGmailService();

        Gmail gmailService = gmailServiceManager.getGmailService();
        assertNotNull(gmailService, "Gmail Service não deve ser null");
        verify(oAuthManager).authorize();
    }

    @Test
    @DisplayName("Deve lançar exceção quando OAuth falha")
    void deveLancarExcecaoQuandoOAuthFalha() throws Exception {

        when(oAuthManager.authorize()).thenThrow(new RuntimeException("Erro de autenticação"));

        GmailReaderException exception = assertThrows(
                GmailReaderException.class,
                () -> gmailServiceManager.inicializarGmailService(),
                "Deve lançar GmailReaderException quando OAuth falha"
        );

        assertEquals("Falha na inicialização do Gmail Service", exception.getMessage());
        assertNotNull(exception.getCause(), "Deve ter causa da exceção");
        verify(oAuthManager).authorize();
    }

    @Test
    @DisplayName("Deve lançar exceção quando Gmail Service não foi inicializado")
    void deveLancarExcecaoQuandoGmailServiceNaoFoiInicializado() {

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gmailServiceManager.getGmailService(),
                "Deve lançar exceção quando service não foi inicializado"
        );

        assertEquals("Gmail Service não foi inicializado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar mesmo Gmail Service via método gmail()")
    void deveRetornarMesmoGmailServiceViaMetodoGmail() throws Exception {

        when(oAuthManager.authorize()).thenReturn(credential);
        gmailServiceManager.inicializarGmailService();

        Gmail service1 = gmailServiceManager.getGmailService();
        Gmail service2 = gmailServiceManager.gmail();

        assertSame(service1, service2, "Deve retornar a mesma instância do Gmail Service");
    }

    @Test
    @DisplayName("Deve configurar application name corretamente")
    void deveConfigurarApplicationNameCorretamente() throws Exception {

        when(oAuthManager.authorize()).thenReturn(credential);

        gmailServiceManager.inicializarGmailService();
        Gmail gmailService = gmailServiceManager.getGmailService();

        assertNotNull(gmailService, "Gmail Service não deve ser null");
        assertEquals("gmailreader", gmailService.getApplicationName(), "Application name deve estar correto");
    }

    @Test
    @DisplayName("Deve permitir múltiplas chamadas de getGmailService após inicialização")
    void devePermitirMultiplasChamadasDeGetGmailServiceAposInicializacao() throws Exception {

        when(oAuthManager.authorize()).thenReturn(credential);
        gmailServiceManager.inicializarGmailService();


        assertDoesNotThrow(() -> {
            Gmail service1 = gmailServiceManager.getGmailService();
            Gmail service2 = gmailServiceManager.getGmailService();
            Gmail service3 = gmailServiceManager.gmail();
            
            assertNotNull(service1);
            assertNotNull(service2);
            assertNotNull(service3);
            assertSame(service1, service2);
            assertSame(service2, service3);
        }, "Deve permitir múltiplas chamadas sem erro");
    }
}
