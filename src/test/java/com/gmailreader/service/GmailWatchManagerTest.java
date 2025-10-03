package com.gmailreader.service;

import com.gmailreader.exception.GmailReaderException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GmailWatchManager")
class GmailWatchManagerTest {

    @Mock
    private GmailServiceManager gmailServiceManager;

    @Mock
    private Gmail gmailService;

    @Mock
    private Gmail.Users users;

    @Mock
    private Gmail.Users.Watch watch;

    private GmailWatchManager gmailWatchManager;

    @BeforeEach
    void setUp() {
        gmailWatchManager = new GmailWatchManager(gmailServiceManager);
        ReflectionTestUtils.setField(gmailWatchManager, "topicName", "projects/test/topics/gmail-test");
    }

    @Test
    @DisplayName("Deve iniciar watch com sucesso")
    void deveIniciarWatchComSucesso() throws Exception {
        WatchResponse watchResponse = new WatchResponse();
        watchResponse.setHistoryId(BigInteger.valueOf(12345));

        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.watch(eq("me"), any(WatchRequest.class))).thenReturn(watch);
        when(watch.execute()).thenReturn(watchResponse);

        assertDoesNotThrow(() -> gmailWatchManager.iniciarWatch());

        verify(gmailServiceManager).getGmailService();
        verify(gmailService.users()).watch(eq("me"), any(WatchRequest.class));
        verify(watch).execute();
    }

    @Test
    @DisplayName("Deve lançar exceção quando Gmail Service falha")
    void deveLancarExcecaoQuandoGmailServiceFalha() throws Exception {
        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.watch(eq("me"), any(WatchRequest.class))).thenReturn(watch);
        when(watch.execute()).thenThrow(new IOException("Erro de conexão"));

        GmailReaderException exception = assertThrows(
                GmailReaderException.class,
                () -> gmailWatchManager.iniciarWatch()
        );

        assertEquals("Falha ao iniciar Watch do Gmail", exception.getMessage());
        assertNotNull(exception.getCause());
        verify(gmailServiceManager).getGmailService();
    }

    @Test
    @DisplayName("Deve lançar exceção quando GmailServiceManager falha")
    void deveLancarExcecaoQuandoGmailServiceManagerFalha() {
        when(gmailServiceManager.getGmailService()).thenThrow(new IllegalStateException("Service não inicializado"));

        GmailReaderException exception = assertThrows(
                GmailReaderException.class,
                () -> gmailWatchManager.iniciarWatch()
        );

        assertEquals("Falha ao iniciar Watch do Gmail", exception.getMessage());
        verify(gmailServiceManager).getGmailService();
    }

    @Test
    @DisplayName("Deve usar topicName configurado")
    void deveUsarTopicNameConfigurado() throws Exception {
        String customTopic = "projects/custom/topics/custom-topic";
        ReflectionTestUtils.setField(gmailWatchManager, "topicName", customTopic);

        WatchResponse watchResponse = new WatchResponse();
        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.watch(eq("me"), any(WatchRequest.class))).thenReturn(watch);
        when(watch.execute()).thenReturn(watchResponse);

        gmailWatchManager.iniciarWatch();

        verify(users).watch(eq("me"), argThat(request -> 
            request.getTopicName().equals(customTopic)
        ));
    }

    @Test
    @DisplayName("Deve usar userId 'me' na requisição")
    void deveUsarUserIdMeNaRequisicao() throws Exception {
        WatchResponse watchResponse = new WatchResponse();
        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.watch(eq("me"), any(WatchRequest.class))).thenReturn(watch);
        when(watch.execute()).thenReturn(watchResponse);

        gmailWatchManager.iniciarWatch();

        verify(users).watch(eq("me"), any(WatchRequest.class));
    }

    @Test
    @DisplayName("Deve propagar exceção de runtime")
    void devePropagaExcecaoDeRuntime() throws Exception {
        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.watch(eq("me"), any(WatchRequest.class))).thenReturn(watch);
        when(watch.execute()).thenThrow(new RuntimeException("Erro inesperado"));

        GmailReaderException exception = assertThrows(
                GmailReaderException.class,
                () -> gmailWatchManager.iniciarWatch()
        );

        assertEquals("Falha ao iniciar Watch do Gmail", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    @DisplayName("Deve criar WatchRequest corretamente")
    void deveCriarWatchRequestCorretamente() throws Exception {
        WatchResponse watchResponse = new WatchResponse();
        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.watch(eq("me"), any(WatchRequest.class))).thenReturn(watch);
        when(watch.execute()).thenReturn(watchResponse);

        gmailWatchManager.iniciarWatch();

        verify(users).watch(eq("me"), argThat(request -> 
            request.getTopicName() != null && 
            request.getTopicName().equals("projects/test/topics/gmail-test")
        ));
    }

    @Test
    @DisplayName("Deve permitir múltiplas chamadas de iniciarWatch")
    void devePermitirMultiplasChamadasDeIniciarWatch() throws Exception {
        WatchResponse watchResponse = new WatchResponse();
        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.watch(eq("me"), any(WatchRequest.class))).thenReturn(watch);
        when(watch.execute()).thenReturn(watchResponse);

        gmailWatchManager.iniciarWatch();
        gmailWatchManager.iniciarWatch();
        gmailWatchManager.iniciarWatch();

        verify(watch, times(3)).execute();
    }

    @Test
    @DisplayName("Deve lidar com topicName vazio")
    void deveLidarComTopicNameVazio() throws Exception {
        ReflectionTestUtils.setField(gmailWatchManager, "topicName", "");

        WatchResponse watchResponse = new WatchResponse();
        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.watch(eq("me"), any(WatchRequest.class))).thenReturn(watch);
        when(watch.execute()).thenReturn(watchResponse);

        gmailWatchManager.iniciarWatch();

        verify(users).watch(eq("me"), argThat(request -> 
            request.getTopicName().equals("")
        ));
    }

    @Test
    @DisplayName("Deve lidar com diferentes formatos de topicName")
    void deveLidarComDiferentesFormatosDeTopicName() throws Exception {
        String[] topicNames = {
            "projects/project1/topics/topic1",
            "projects/my-project-123/topics/my-topic-456",
            "projects/test_project/topics/test_topic"
        };

        WatchResponse watchResponse = new WatchResponse();
        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.watch(eq("me"), any(WatchRequest.class))).thenReturn(watch);
        when(watch.execute()).thenReturn(watchResponse);

        for (String topicName : topicNames) {
            ReflectionTestUtils.setField(gmailWatchManager, "topicName", topicName);
            gmailWatchManager.iniciarWatch();
        }

        verify(watch, times(topicNames.length)).execute();
    }
}
