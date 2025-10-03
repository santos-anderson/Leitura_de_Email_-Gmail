package com.gmailreader.service.processing;

import com.gmailreader.service.GmailModifierService;
import com.google.api.services.gmail.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkAsReadStep")
class MarkAsReadStepTest {

    @Mock
    private GmailModifierService gmailModifierService;

    @Mock
    private ProcessingStep nextStep;

    private MarkAsReadStep markAsReadStep;

    @BeforeEach
    void setUp() {
        markAsReadStep = new MarkAsReadStep(gmailModifierService);
    }

    @Test
    @DisplayName("Deve marcar email como lido")
    void deveMarcarEmailComoLido() throws Exception {
        Message message = new Message().setId("msg123");
        ProcessingContext context = new ProcessingContext("msg123");

        markAsReadStep.processar(message, context);

        verify(gmailModifierService).marcarComoLido("msg123");
    }

    @Test
    @DisplayName("Deve continuar cadeia após marcar como lido")
    void deveContinuarCadeiaAposMarcarComoLido() throws Exception {
        Message message = new Message().setId("msg456");
        ProcessingContext context = new ProcessingContext("msg456");

        markAsReadStep.setNext(nextStep);
        markAsReadStep.processar(message, context);

        verify(gmailModifierService).marcarComoLido("msg456");
        verify(nextStep).processar(message, context);
    }

    @Test
    @DisplayName("Deve propagar exceção do modifier service")
    void devePropagaExcecaoDoModifierService() throws Exception {
        Message message = new Message().setId("msg789");
        ProcessingContext context = new ProcessingContext("msg789");

        doThrow(new IOException("Erro ao marcar como lido")).when(gmailModifierService).marcarComoLido("msg789");

        assertThrows(IOException.class,
                () -> markAsReadStep.processar(message, context));
    }

    @Test
    @DisplayName("Deve marcar múltiplos emails como lidos")
    void deveMarcarMultiplosEmailsComoLidos() throws Exception {
        Message message1 = new Message().setId("msg1");
        Message message2 = new Message().setId("msg2");
        Message message3 = new Message().setId("msg3");
        
        ProcessingContext context1 = new ProcessingContext("msg1");
        ProcessingContext context2 = new ProcessingContext("msg2");
        ProcessingContext context3 = new ProcessingContext("msg3");

        markAsReadStep.processar(message1, context1);
        markAsReadStep.processar(message2, context2);
        markAsReadStep.processar(message3, context3);

        verify(gmailModifierService).marcarComoLido("msg1");
        verify(gmailModifierService).marcarComoLido("msg2");
        verify(gmailModifierService).marcarComoLido("msg3");
    }

    @Test
    @DisplayName("Deve funcionar sem próximo step")
    void deveFuncionarSemProximoStep() throws Exception {
        Message message = new Message().setId("msg999");
        ProcessingContext context = new ProcessingContext("msg999");

        assertDoesNotThrow(() -> markAsReadStep.processar(message, context));
        verify(gmailModifierService).marcarComoLido("msg999");
    }

    @Test
    @DisplayName("Deve marcar email com ID especial")
    void deveMarcarEmailComIdEspecial() throws Exception {
        String specialId = "msg-123_test@domain";
        Message message = new Message().setId(specialId);
        ProcessingContext context = new ProcessingContext(specialId);

        markAsReadStep.processar(message, context);

        verify(gmailModifierService).marcarComoLido(specialId);
    }

    @Test
    @DisplayName("Deve marcar email com ID longo")
    void deveMarcarEmailComIdLongo() throws Exception {
        String longId = "a".repeat(500);
        Message message = new Message().setId(longId);
        ProcessingContext context = new ProcessingContext(longId);

        markAsReadStep.processar(message, context);

        verify(gmailModifierService).marcarComoLido(longId);
    }
}
