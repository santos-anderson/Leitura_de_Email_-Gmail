package com.gmailreader.service.processing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProcessingContext")
class ProcessingContextTest {

    @Test
    @DisplayName("Deve criar contexto com emailId")
    void deveCriarContextoComEmailId() {
        String emailId = "email123";
        
        ProcessingContext context = new ProcessingContext(emailId);
        
        assertEquals(emailId, context.getEmailId());
        assertNull(context.getConvertedJson());
        assertFalse(context.isAlreadyProcessed());
    }

    @Test
    @DisplayName("Deve inicializar alreadyProcessed como false")
    void deveInicializarAlreadyProcessedComoFalse() {
        ProcessingContext context = new ProcessingContext("email123");
        
        assertFalse(context.isAlreadyProcessed());
        assertFalse(context.jaFoiProcessado());
    }

    @Test
    @DisplayName("Deve definir e obter JSON convertido")
    void deveDefinirEObterJsonConvertido() {
        ProcessingContext context = new ProcessingContext("email123");
        String json = "{\"id\":\"123\",\"subject\":\"Test\"}";
        
        context.setConvertedJson(json);
        
        assertEquals(json, context.getConvertedJson());
        assertEquals(json, context.obterJsonConvertido());
    }

    @Test
    @DisplayName("Deve definir e verificar se já foi processado")
    void deveDefinirEVerificarSeJaFoiProcessado() {
        ProcessingContext context = new ProcessingContext("email123");
        
        context.setAlreadyProcessed(true);
        
        assertTrue(context.isAlreadyProcessed());
        assertTrue(context.jaFoiProcessado());
    }

    @Test
    @DisplayName("Deve usar método definirJsonConvertido")
    void deveUsarMetodoDefinirJsonConvertido() {
        ProcessingContext context = new ProcessingContext("email123");
        String json = "{\"data\":\"test\"}";
        
        context.definirJsonConvertido(json);
        
        assertEquals(json, context.getConvertedJson());
    }

    @Test
    @DisplayName("Deve usar método definirJaProcessado")
    void deveUsarMetodoDefinirJaProcessado() {
        ProcessingContext context = new ProcessingContext("email123");
        
        context.definirJaProcessado(true);
        
        assertTrue(context.isAlreadyProcessed());
    }

    @Test
    @DisplayName("Deve alterar emailId")
    void deveAlterarEmailId() {
        ProcessingContext context = new ProcessingContext("email123");
        String novoEmailId = "email456";
        
        context.setEmailId(novoEmailId);
        
        assertEquals(novoEmailId, context.getEmailId());
    }

    @Test
    @DisplayName("Deve permitir múltiplas alterações de estado")
    void devePermitirMultiplasAlteracoesDeEstado() {
        ProcessingContext context = new ProcessingContext("email123");
        
        context.setConvertedJson("{\"v1\":\"data\"}");
        context.setAlreadyProcessed(true);
        
        assertEquals("{\"v1\":\"data\"}", context.getConvertedJson());
        assertTrue(context.isAlreadyProcessed());
        
        context.setConvertedJson("{\"v2\":\"updated\"}");
        context.setAlreadyProcessed(false);
        
        assertEquals("{\"v2\":\"updated\"}", context.getConvertedJson());
        assertFalse(context.isAlreadyProcessed());
    }

    @Test
    @DisplayName("Deve aceitar JSON null")
    void deveAceitarJsonNull() {
        ProcessingContext context = new ProcessingContext("email123");
        
        context.setConvertedJson(null);
        
        assertNull(context.getConvertedJson());
    }

    @Test
    @DisplayName("Deve aceitar JSON vazio")
    void deveAceitarJsonVazio() {
        ProcessingContext context = new ProcessingContext("email123");
        
        context.setConvertedJson("");
        
        assertEquals("", context.getConvertedJson());
    }

    @Test
    @DisplayName("Deve aceitar JSON complexo")
    void deveAceitarJsonComplexo() {
        ProcessingContext context = new ProcessingContext("email123");
        String jsonComplexo = "{\"id\":\"123\",\"from\":\"test@example.com\",\"subject\":\"Test\",\"body\":\"Long text here\",\"attachments\":[{\"name\":\"file.pdf\"}]}";
        
        context.setConvertedJson(jsonComplexo);
        
        assertEquals(jsonComplexo, context.getConvertedJson());
    }

    @Test
    @DisplayName("Deve manter independência entre instâncias")
    void deveManterIndependenciaEntreInstancias() {
        ProcessingContext context1 = new ProcessingContext("email1");
        ProcessingContext context2 = new ProcessingContext("email2");
        
        context1.setConvertedJson("{\"data\":\"context1\"}");
        context1.setAlreadyProcessed(true);
        
        context2.setConvertedJson("{\"data\":\"context2\"}");
        context2.setAlreadyProcessed(false);
        
        assertEquals("email1", context1.getEmailId());
        assertEquals("{\"data\":\"context1\"}", context1.getConvertedJson());
        assertTrue(context1.isAlreadyProcessed());
        
        assertEquals("email2", context2.getEmailId());
        assertEquals("{\"data\":\"context2\"}", context2.getConvertedJson());
        assertFalse(context2.isAlreadyProcessed());
    }

    @Test
    @DisplayName("Deve aceitar emailId com caracteres especiais")
    void deveAceitarEmailIdComCaracteresEspeciais() {
        String emailIdEspecial = "email-123_test@domain.com";
        
        ProcessingContext context = new ProcessingContext(emailIdEspecial);
        
        assertEquals(emailIdEspecial, context.getEmailId());
    }

    @Test
    @DisplayName("Deve aceitar emailId muito longo")
    void deveAceitarEmailIdMuitoLongo() {
        String emailIdLongo = "a".repeat(1000);
        
        ProcessingContext context = new ProcessingContext(emailIdLongo);
        
        assertEquals(emailIdLongo, context.getEmailId());
    }

    @Test
    @DisplayName("Deve permitir resetar estado para false")
    void devePermitirResetarEstadoParaFalse() {
        ProcessingContext context = new ProcessingContext("email123");
        
        context.definirJaProcessado(true);
        assertTrue(context.jaFoiProcessado());
        
        context.definirJaProcessado(false);
        assertFalse(context.jaFoiProcessado());
    }
}
