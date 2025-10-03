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
@DisplayName("CheckAlreadyProcessedStep")
class CheckAlreadyProcessedStepTest {

    @Mock
    private ProcessedEmailFileRepository processedEmailRepository;

    @Mock
    private ProcessingStep nextStep;

    private CheckAlreadyProcessedStep checkAlreadyProcessedStep;

    @BeforeEach
    void setUp() {
        checkAlreadyProcessedStep = new CheckAlreadyProcessedStep(processedEmailRepository);
    }

    @Test
    @DisplayName("Deve marcar como processado quando email já foi processado")
    void deveMarcarComoProcessadoQuandoEmailJaFoiProcessado() throws Exception {
        Message message = new Message().setId("msg123");
        ProcessingContext context = new ProcessingContext("msg123");

        when(processedEmailRepository.emailJaProcessado("msg123")).thenReturn(true);

        checkAlreadyProcessedStep.processar(message, context);

        assertTrue(context.jaFoiProcessado());
        verify(processedEmailRepository).emailJaProcessado("msg123");
    }

    @Test
    @DisplayName("Deve continuar cadeia quando email não foi processado")
    void deveContinuarCadeiaQuandoEmailNaoFoiProcessado() throws Exception {
        Message message = new Message().setId("msg456");
        ProcessingContext context = new ProcessingContext("msg456");

        when(processedEmailRepository.emailJaProcessado("msg456")).thenReturn(false);
        checkAlreadyProcessedStep.setNext(nextStep);

        checkAlreadyProcessedStep.processar(message, context);

        assertFalse(context.jaFoiProcessado());
        verify(processedEmailRepository, atLeastOnce()).emailJaProcessado("msg456");
        verify(nextStep).processar(message, context);
    }

    @Test
    @DisplayName("Deve parar cadeia quando email já foi processado")
    void devePararCadeiaQuandoEmailJaFoiProcessado() throws Exception {
        Message message = new Message().setId("msg789");
        ProcessingContext context = new ProcessingContext("msg789");

        when(processedEmailRepository.emailJaProcessado("msg789")).thenReturn(true);
        checkAlreadyProcessedStep.setNext(nextStep);

        checkAlreadyProcessedStep.processar(message, context);

        assertTrue(context.jaFoiProcessado());
        verify(nextStep, never()).processar(any(), any());
    }

    @Test
    @DisplayName("Deve funcionar sem próximo step quando email já processado")
    void deveFuncionarSemProximoStepQuandoEmailJaProcessado() throws Exception {
        Message message = new Message().setId("msg999");
        ProcessingContext context = new ProcessingContext("msg999");

        when(processedEmailRepository.emailJaProcessado("msg999")).thenReturn(true);

        assertDoesNotThrow(() -> checkAlreadyProcessedStep.processar(message, context));
        assertTrue(context.jaFoiProcessado());
    }

    @Test
    @DisplayName("Deve processar múltiplos emails sequencialmente")
    void deveProcessarMultiplosEmailsSequencialmente() throws Exception {
        Message message1 = new Message().setId("msg1");
        Message message2 = new Message().setId("msg2");
        Message message3 = new Message().setId("msg3");
        
        ProcessingContext context1 = new ProcessingContext("msg1");
        ProcessingContext context2 = new ProcessingContext("msg2");
        ProcessingContext context3 = new ProcessingContext("msg3");

        when(processedEmailRepository.emailJaProcessado("msg1")).thenReturn(true);
        when(processedEmailRepository.emailJaProcessado("msg2")).thenReturn(false);
        when(processedEmailRepository.emailJaProcessado("msg3")).thenReturn(true);

        checkAlreadyProcessedStep.processar(message1, context1);
        checkAlreadyProcessedStep.processar(message2, context2);
        checkAlreadyProcessedStep.processar(message3, context3);

        assertTrue(context1.jaFoiProcessado());
        assertFalse(context2.jaFoiProcessado());
        assertTrue(context3.jaFoiProcessado());
    }

    @Test
    @DisplayName("Deve propagar exceção do repository")
    void devePropagaExcecaoDoRepository() throws Exception {
        Message message = new Message().setId("msg111");
        ProcessingContext context = new ProcessingContext("msg111");

        when(processedEmailRepository.emailJaProcessado("msg111"))
                .thenThrow(new RuntimeException("Erro no repository"));

        assertThrows(RuntimeException.class,
                () -> checkAlreadyProcessedStep.processar(message, context));
    }

    @Test
    @DisplayName("Deve verificar email com ID longo")
    void deveVerificarEmailComIdLongo() throws Exception {
        String longId = "a".repeat(500);
        Message message = new Message().setId(longId);
        ProcessingContext context = new ProcessingContext(longId);

        when(processedEmailRepository.emailJaProcessado(longId)).thenReturn(false);

        checkAlreadyProcessedStep.processar(message, context);

        assertFalse(context.jaFoiProcessado());
        verify(processedEmailRepository, atLeastOnce()).emailJaProcessado(longId);
    }

    @Test
    @DisplayName("Deve verificar email com ID especial")
    void deveVerificarEmailComIdEspecial() throws Exception {
        String specialId = "msg-123_test@domain";
        Message message = new Message().setId(specialId);
        ProcessingContext context = new ProcessingContext(specialId);

        when(processedEmailRepository.emailJaProcessado(specialId)).thenReturn(false);

        checkAlreadyProcessedStep.processar(message, context);

        assertFalse(context.jaFoiProcessado());
        verify(processedEmailRepository, atLeastOnce()).emailJaProcessado(specialId);
    }
}
