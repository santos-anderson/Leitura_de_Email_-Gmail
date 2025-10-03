package com.gmailreader.service;

import com.gmailreader.exception.GmailReaderException;
import com.gmailreader.service.processing.ProcessingContext;
import com.gmailreader.service.processing.ProcessingStep;
import com.google.api.services.gmail.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailProcessingService")
class EmailProcessingServiceTest {

    @Mock
    private GmailReaderService gmailReaderService;

    @Mock
    private ProcessingStep processingChain;

    private EmailProcessingService emailProcessingService;

    @BeforeEach
    void setUp() {
        emailProcessingService = new EmailProcessingService(gmailReaderService, processingChain);
    }

    @Test
    @DisplayName("Deve processar múltiplos emails")
    void deveProcessarMultiplosEmails() throws Exception {
        List<Message> messages = Arrays.asList(
                new Message().setId("msg1"),
                new Message().setId("msg2"),
                new Message().setId("msg3")
        );

        when(gmailReaderService.listarEmails()).thenReturn(messages);

        emailProcessingService.processarEmails();

        verify(gmailReaderService).listarEmails();
        verify(processingChain, times(3)).processar(any(Message.class), any(ProcessingContext.class));
    }

    @Test
    @DisplayName("Deve processar email único")
    void deveProcessarEmailUnico() throws Exception {
        Message message = new Message().setId("msg123");

        emailProcessingService.processarEmail(message);

        verify(processingChain).processar(eq(message), any(ProcessingContext.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando Gmail Reader Service falha")
    void deveLancarExcecaoQuandoGmailReaderServiceFalha() throws Exception {
        when(gmailReaderService.listarEmails()).thenThrow(new IOException("Erro de conexão"));

        GmailReaderException exception = assertThrows(GmailReaderException.class,
                () -> emailProcessingService.processarEmails());

        assertEquals("Falha na comunicação com Gmail API", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando processing chain falha")
    void deveLancarExcecaoQuandoProcessingChainFalha() throws Exception {
        Message message = new Message().setId("msg456");

        doThrow(new RuntimeException("Erro no processamento"))
                .when(processingChain).processar(any(Message.class), any(ProcessingContext.class));

        GmailReaderException exception = assertThrows(GmailReaderException.class,
                () -> emailProcessingService.processarEmail(message));

        assertTrue(exception.getMessage().contains("msg456"));
    }

    @Test
    @DisplayName("Deve processar lista vazia sem erros")
    void deveProcessarListaVaziaSemErros() throws Exception {
        when(gmailReaderService.listarEmails()).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> emailProcessingService.processarEmails());

        verify(gmailReaderService).listarEmails();
        verify(processingChain, never()).processar(any(), any());
    }

    @Test
    @DisplayName("Deve criar contexto com ID correto para cada email")
    void deveCriarContextoComIdCorretoParaCadaEmail() throws Exception {
        List<Message> messages = Arrays.asList(
                new Message().setId("msg1"),
                new Message().setId("msg2")
        );

        when(gmailReaderService.listarEmails()).thenReturn(messages);

        emailProcessingService.processarEmails();

        verify(processingChain).processar(argThat(msg -> msg.getId().equals("msg1")), any(ProcessingContext.class));
        verify(processingChain).processar(argThat(msg -> msg.getId().equals("msg2")), any(ProcessingContext.class));
    }

    @Test
    @DisplayName("Deve relançar GmailReaderException sem encapsular")
    void deveRelancarGmailReaderExceptionSemEncapsular() throws Exception {
        GmailReaderException originalException = new GmailReaderException("Erro original");
        when(gmailReaderService.listarEmails()).thenThrow(originalException);

        GmailReaderException exception = assertThrows(GmailReaderException.class,
                () -> emailProcessingService.processarEmails());

        assertEquals("Erro original", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve encapsular exceções genéricas")
    void deveEncapsularExcecoesGenericas() throws Exception {
        when(gmailReaderService.listarEmails()).thenThrow(new RuntimeException("Erro inesperado"));

        GmailReaderException exception = assertThrows(GmailReaderException.class,
                () -> emailProcessingService.processarEmails());

        assertEquals("Erro inesperado durante o processamento de emails", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve processar email com ID especial")
    void deveProcessarEmailComIdEspecial() throws Exception {
        Message message = new Message().setId("msg-123_test@domain");

        emailProcessingService.processarEmail(message);

        verify(processingChain).processar(eq(message), argThat(context ->
                context.getEmailId().equals("msg-123_test@domain")
        ));
    }

    @Test
    @DisplayName("Deve processar múltiplos emails sequencialmente")
    void deveProcessarMultiplosEmailsSequencialmente() throws Exception {
        List<Message> messages = Arrays.asList(
                new Message().setId("msg1"),
                new Message().setId("msg2"),
                new Message().setId("msg3"),
                new Message().setId("msg4"),
                new Message().setId("msg5")
        );

        when(gmailReaderService.listarEmails()).thenReturn(messages);

        emailProcessingService.processarEmails();

        verify(processingChain, times(5)).processar(any(Message.class), any(ProcessingContext.class));
    }
}
