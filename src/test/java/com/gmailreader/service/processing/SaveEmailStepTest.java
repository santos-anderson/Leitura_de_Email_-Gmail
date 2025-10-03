package com.gmailreader.service.processing;

import com.gmailreader.service.StorageService;
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
@DisplayName("SaveEmailStep")
class SaveEmailStepTest {

    @Mock
    private StorageService storageService;

    @Mock
    private ProcessingStep nextStep;

    private SaveEmailStep saveEmailStep;

    @BeforeEach
    void setUp() {
        saveEmailStep = new SaveEmailStep(storageService);
    }

    @Test
    @DisplayName("Deve salvar email quando JSON está presente")
    void deveSalvarEmailQuandoJsonEstaPresente() throws Exception {
        Message message = new Message().setId("msg123");
        ProcessingContext context = new ProcessingContext("msg123");
        String json = "{\"id\":\"msg123\"}";
        context.definirJsonConvertido(json);

        saveEmailStep.processar(message, context);

        verify(storageService).salvarEmail(json);
    }

    @Test
    @DisplayName("Deve continuar cadeia após salvar")
    void deveContinuarCadeiaAposSalvar() throws Exception {
        Message message = new Message().setId("msg456");
        ProcessingContext context = new ProcessingContext("msg456");
        String json = "{\"id\":\"msg456\"}";
        context.definirJsonConvertido(json);

        saveEmailStep.setNext(nextStep);
        saveEmailStep.processar(message, context);

        verify(storageService).salvarEmail(json);
        verify(nextStep).processar(message, context);
    }

    @Test
    @DisplayName("Não deve salvar quando JSON é null")
    void naoDeveSalvarQuandoJsonENull() throws Exception {
        Message message = new Message().setId("msg789");
        ProcessingContext context = new ProcessingContext("msg789");
        context.definirJsonConvertido(null);

        saveEmailStep.processar(message, context);

        verify(storageService, never()).salvarEmail(any());
    }

    @Test
    @DisplayName("Deve continuar cadeia mesmo quando JSON é null")
    void deveContinuarCadeiaMesmoQuandoJsonENull() throws Exception {
        Message message = new Message().setId("msg999");
        ProcessingContext context = new ProcessingContext("msg999");
        context.definirJsonConvertido(null);

        saveEmailStep.setNext(nextStep);
        saveEmailStep.processar(message, context);

        verify(storageService, never()).salvarEmail(any());
        verify(nextStep).processar(message, context);
    }

    @Test
    @DisplayName("Deve propagar exceção do storage service")
    void devePropagaExcecaoDoStorageService() throws Exception {
        Message message = new Message().setId("msg111");
        ProcessingContext context = new ProcessingContext("msg111");
        String json = "{\"id\":\"msg111\"}";
        context.definirJsonConvertido(json);

        doThrow(new RuntimeException("Erro ao salvar")).when(storageService).salvarEmail(json);

        assertThrows(RuntimeException.class,
                () -> saveEmailStep.processar(message, context));
    }

    @Test
    @DisplayName("Deve salvar JSON complexo")
    void deveSalvarJsonComplexo() throws Exception {
        Message message = new Message().setId("msg222");
        ProcessingContext context = new ProcessingContext("msg222");
        String jsonComplexo = "{\"id\":\"msg222\",\"from\":\"test@example.com\",\"subject\":\"Test\",\"body\":\"Long text\"}";
        context.definirJsonConvertido(jsonComplexo);

        saveEmailStep.processar(message, context);

        verify(storageService).salvarEmail(jsonComplexo);
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
        
        context1.definirJsonConvertido("{\"id\":\"msg1\"}");
        context2.definirJsonConvertido("{\"id\":\"msg2\"}");
        context3.definirJsonConvertido("{\"id\":\"msg3\"}");

        saveEmailStep.processar(message1, context1);
        saveEmailStep.processar(message2, context2);
        saveEmailStep.processar(message3, context3);

        verify(storageService, times(3)).salvarEmail(anyString());
    }

    @Test
    @DisplayName("Deve funcionar sem próximo step")
    void deveFuncionarSemProximoStep() throws Exception {
        Message message = new Message().setId("msg333");
        ProcessingContext context = new ProcessingContext("msg333");
        String json = "{\"id\":\"msg333\"}";
        context.definirJsonConvertido(json);

        assertDoesNotThrow(() -> saveEmailStep.processar(message, context));
        verify(storageService).salvarEmail(json);
    }
}
