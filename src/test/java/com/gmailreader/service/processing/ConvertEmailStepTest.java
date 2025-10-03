package com.gmailreader.service.processing;

import com.gmailreader.service.EmailToJsonService;
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
@DisplayName("ConvertEmailStep")
class ConvertEmailStepTest {

    @Mock
    private EmailToJsonService emailToJsonService;

    @Mock
    private ProcessingStep nextStep;

    private ConvertEmailStep convertEmailStep;

    @BeforeEach
    void setUp() {
        convertEmailStep = new ConvertEmailStep(emailToJsonService);
    }

    @Test
    @DisplayName("Deve converter email para JSON")
    void deveConverterEmailParaJson() throws Exception {
        Message message = new Message().setId("msg123");
        ProcessingContext context = new ProcessingContext("msg123");
        String expectedJson = "{\"id\":\"msg123\",\"subject\":\"Test\"}";

        when(emailToJsonService.converter(message)).thenReturn(expectedJson);

        convertEmailStep.processar(message, context);

        assertEquals(expectedJson, context.obterJsonConvertido());
        verify(emailToJsonService).converter(message);
    }

    @Test
    @DisplayName("Deve continuar cadeia após conversão")
    void deveContinuarCadeiaAposConversao() throws Exception {
        Message message = new Message().setId("msg456");
        ProcessingContext context = new ProcessingContext("msg456");
        String json = "{\"id\":\"msg456\"}";

        when(emailToJsonService.converter(message)).thenReturn(json);
        convertEmailStep.setNext(nextStep);

        convertEmailStep.processar(message, context);

        assertEquals(json, context.obterJsonConvertido());
        verify(nextStep).processar(message, context);
    }

    @Test
    @DisplayName("Deve propagar exceção do serviço de conversão")
    void devePropagaExcecaoDoServicoDeConversao() throws Exception {
        Message message = new Message().setId("msg789");
        ProcessingContext context = new ProcessingContext("msg789");

        when(emailToJsonService.converter(message)).thenThrow(new RuntimeException("Erro na conversão"));

        assertThrows(RuntimeException.class,
                () -> convertEmailStep.processar(message, context));
    }

    @Test
    @DisplayName("Deve converter email com JSON vazio")
    void deveConverterEmailComJsonVazio() throws Exception {
        Message message = new Message().setId("msg999");
        ProcessingContext context = new ProcessingContext("msg999");

        when(emailToJsonService.converter(message)).thenReturn("");

        convertEmailStep.processar(message, context);

        assertEquals("", context.obterJsonConvertido());
    }

    @Test
    @DisplayName("Deve converter email com JSON complexo")
    void deveConverterEmailComJsonComplexo() throws Exception {
        Message message = new Message().setId("msg111");
        ProcessingContext context = new ProcessingContext("msg111");
        String jsonComplexo = "{\"id\":\"msg111\",\"from\":\"test@example.com\",\"subject\":\"Test Subject\",\"body\":\"Long body text\",\"attachments\":[{\"name\":\"file.pdf\",\"size\":1024}]}";

        when(emailToJsonService.converter(message)).thenReturn(jsonComplexo);

        convertEmailStep.processar(message, context);

        assertEquals(jsonComplexo, context.obterJsonConvertido());
    }

    @Test
    @DisplayName("Deve processar múltiplos emails sequencialmente")
    void deveProcessarMultiplosEmailsSequencialmente() throws Exception {
        Message message1 = new Message().setId("msg1");
        Message message2 = new Message().setId("msg2");
        
        ProcessingContext context1 = new ProcessingContext("msg1");
        ProcessingContext context2 = new ProcessingContext("msg2");
        
        String json1 = "{\"id\":\"msg1\"}";
        String json2 = "{\"id\":\"msg2\"}";

        when(emailToJsonService.converter(message1)).thenReturn(json1);
        when(emailToJsonService.converter(message2)).thenReturn(json2);

        convertEmailStep.processar(message1, context1);
        convertEmailStep.processar(message2, context2);

        assertEquals(json1, context1.obterJsonConvertido());
        assertEquals(json2, context2.obterJsonConvertido());
    }

    @Test
    @DisplayName("Deve funcionar sem próximo step")
    void deveFuncionarSemProximoStep() throws Exception {
        Message message = new Message().setId("msg222");
        ProcessingContext context = new ProcessingContext("msg222");
        String json = "{\"id\":\"msg222\"}";

        when(emailToJsonService.converter(message)).thenReturn(json);

        assertDoesNotThrow(() -> convertEmailStep.processar(message, context));
        assertEquals(json, context.obterJsonConvertido());
    }
}
