package com.gmailreader.service.processing;

import com.gmailreader.repository.ProcessedEmailFileRepository;
import com.google.api.services.gmail.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkAsProcessedStep")
class MarkAsProcessedStepTest {

    @Mock
    private ProcessedEmailFileRepository processedEmailRepository;

    @Mock
    private ProcessingStep nextStep;

    private MarkAsProcessedStep markAsProcessedStep;

    @BeforeEach
    void setUp() {
        markAsProcessedStep = new MarkAsProcessedStep(processedEmailRepository);
    }

    @Test
    @DisplayName("Deve marcar email como processado")
    void deveMarcarEmailComoProcessado() throws Exception {
        Message message = new Message().setId("msg123");
        ProcessingContext context = new ProcessingContext("msg123");

        markAsProcessedStep.processar(message, context);

        verify(processedEmailRepository).marcarComoProcessado("msg123");
    }

    @Test
    @DisplayName("Deve continuar cadeia após marcar como processado")
    void deveContinuarCadeiaAposMarcarComoProcessado() throws Exception {
        Message message = new Message().setId("msg456");
        ProcessingContext context = new ProcessingContext("msg456");

        markAsProcessedStep.setNext(nextStep);
        markAsProcessedStep.processar(message, context);

        verify(processedEmailRepository).marcarComoProcessado("msg456");
        verify(nextStep).processar(message, context);
    }

    @Test
    @DisplayName("Deve propagar exceção do repository")
    void devePropagaExcecaoDoRepository() throws Exception {
        Message message = new Message().setId("msg789");
        ProcessingContext context = new ProcessingContext("msg789");

        doThrow(new RuntimeException("Erro no repository")).when(processedEmailRepository).marcarComoProcessado("msg789");

        assertThrows(RuntimeException.class,
                () -> markAsProcessedStep.processar(message, context));
    }

    @Test
    @DisplayName("Deve marcar múltiplos emails como processados")
    void deveMarcarMultiplosEmailsComoProcessados() throws Exception {
        Message message1 = new Message().setId("msg1");
        Message message2 = new Message().setId("msg2");
        Message message3 = new Message().setId("msg3");
        
        ProcessingContext context1 = new ProcessingContext("msg1");
        ProcessingContext context2 = new ProcessingContext("msg2");
        ProcessingContext context3 = new ProcessingContext("msg3");

        markAsProcessedStep.processar(message1, context1);
        markAsProcessedStep.processar(message2, context2);
        markAsProcessedStep.processar(message3, context3);

        verify(processedEmailRepository).marcarComoProcessado("msg1");
        verify(processedEmailRepository).marcarComoProcessado("msg2");
        verify(processedEmailRepository).marcarComoProcessado("msg3");
    }

    @Test
    @DisplayName("Deve funcionar sem próximo step")
    void deveFuncionarSemProximoStep() throws Exception {
        Message message = new Message().setId("msg999");
        ProcessingContext context = new ProcessingContext("msg999");

        assertDoesNotThrow(() -> markAsProcessedStep.processar(message, context));
        verify(processedEmailRepository).marcarComoProcessado("msg999");
    }

    @Test
    @DisplayName("Deve marcar email com ID especial")
    void deveMarcarEmailComIdEspecial() throws Exception {
        String specialId = "msg-123_test@domain";
        Message message = new Message().setId(specialId);
        ProcessingContext context = new ProcessingContext(specialId);

        markAsProcessedStep.processar(message, context);

        verify(processedEmailRepository).marcarComoProcessado(specialId);
    }

    @Test
    @DisplayName("Deve marcar email com ID longo")
    void deveMarcarEmailComIdLongo() throws Exception {
        String longId = "a".repeat(500);
        Message message = new Message().setId(longId);
        ProcessingContext context = new ProcessingContext(longId);

        markAsProcessedStep.processar(message, context);

        verify(processedEmailRepository).marcarComoProcessado(longId);
    }
}
