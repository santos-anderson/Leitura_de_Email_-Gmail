package com.gmailreader.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationParser")
class NotificationParserTest {

    private NotificationParser notificationParser;

    @BeforeEach
    void setUp() {
        notificationParser = new NotificationParser();
    }

    @Test
    @DisplayName("Deve extrair historyId de JSON válido")
    void deveExtrairHistoryIdDeJsonValido() {
        String jsonValido = "{\"historyId\":\"12345\",\"emailAddress\":\"test@gmail.com\"}";

        BigInteger historyId = notificationParser.extrairHistoryId(jsonValido);

        assertNotNull(historyId, "HistoryId não deve ser null");
        assertEquals(new BigInteger("12345"), historyId, "HistoryId deve ser 12345");
    }

    @Test
    @DisplayName("Deve extrair historyId grande")
    void deveExtrairHistoryIdGrande() {
        String jsonComHistoryIdGrande = "{\"historyId\":\"999999999999999999999\"}";

        BigInteger historyId = notificationParser.extrairHistoryId(jsonComHistoryIdGrande);

        assertNotNull(historyId, "HistoryId não deve ser null");
        assertEquals(new BigInteger("999999999999999999999"), historyId, 
                "Deve suportar números muito grandes");
    }

    @Test
    @DisplayName("Deve retornar null para JSON sem historyId")
    void deveRetornarNullParaJsonSemHistoryId() {
        String jsonSemHistoryId = "{\"emailAddress\":\"test@gmail.com\",\"action\":\"update\"}";

        BigInteger historyId = notificationParser.extrairHistoryId(jsonSemHistoryId);

        assertNull(historyId, "HistoryId deve ser null quando campo não existe");
    }

    @Test
    @DisplayName("Deve retornar null para JSON inválido")
    void deveRetornarNullParaJsonInvalido() {
        String jsonInvalido = "{historyId:12345,emailAddress:test@gmail.com}";

        BigInteger historyId = notificationParser.extrairHistoryId(jsonInvalido);

        assertNull(historyId, "HistoryId deve ser null para JSON malformado");
    }

    @Test
    @DisplayName("Deve retornar null para string vazia")
    void deveRetornarNullParaStringVazia() {
        String stringVazia = "";

        BigInteger historyId = notificationParser.extrairHistoryId(stringVazia);

        assertNull(historyId, "HistoryId deve ser null para string vazia");
    }

    @Test
    @DisplayName("Deve retornar null para null")
    void deveRetornarNullParaNull() {
        BigInteger historyId = notificationParser.extrairHistoryId(null);

        assertNull(historyId, "HistoryId deve ser null quando input é null");
    }

    @Test
    @DisplayName("Deve extrair historyId com valor zero")
    void deveExtrairHistoryIdComValorZero() {
        String jsonComZero = "{\"historyId\":\"0\"}";

        BigInteger historyId = notificationParser.extrairHistoryId(jsonComZero);

        assertNotNull(historyId, "HistoryId não deve ser null");
        assertEquals(BigInteger.ZERO, historyId, "HistoryId deve ser zero");
    }

    @Test
    @DisplayName("Deve extrair historyId de JSON com campos extras")
    void deveExtrairHistoryIdDeJsonComCamposExtras() {
        String jsonComplexo = """
                {
                    "historyId": "54321",
                    "emailAddress": "user@example.com",
                    "timestamp": "2024-10-02T14:45:00Z",
                    "action": "messageAdded",
                    "metadata": {
                        "source": "gmail",
                        "version": "1.0"
                    }
                }
                """;

        BigInteger historyId = notificationParser.extrairHistoryId(jsonComplexo);

        assertNotNull(historyId, "HistoryId não deve ser null");
        assertEquals(new BigInteger("54321"), historyId, "Deve extrair historyId mesmo com campos extras");
    }

    @Test
    @DisplayName("Deve validar notificação válida")
    void deveValidarNotificacaoValida() {
        String jsonValido = "{\"historyId\":\"12345\"}";

        boolean isValid = notificationParser.isValidNotification(jsonValido);

        assertTrue(isValid, "Notificação com historyId deve ser válida");
    }

    @Test
    @DisplayName("Deve invalidar notificação sem historyId")
    void deveInvalidarNotificacaoSemHistoryId() {
        String jsonSemHistoryId = "{\"emailAddress\":\"test@gmail.com\"}";

        boolean isValid = notificationParser.isValidNotification(jsonSemHistoryId);

        assertFalse(isValid, "Notificação sem historyId deve ser inválida");
    }

    @Test
    @DisplayName("Deve invalidar JSON malformado")
    void deveInvalidarJsonMalformado() {
        String jsonInvalido = "not a json";

        boolean isValid = notificationParser.isValidNotification(jsonInvalido);

        assertFalse(isValid, "JSON malformado deve ser inválido");
    }

    @Test
    @DisplayName("Deve tratar historyId como string numérica")
    void deveTratarHistoryIdComoStringNumerica() {
        String jsonComHistoryIdString = "{\"historyId\":\"000123\"}";

        BigInteger historyId = notificationParser.extrairHistoryId(jsonComHistoryIdString);

        assertNotNull(historyId, "HistoryId não deve ser null");
        assertEquals(new BigInteger("123"), historyId, "Deve converter string numérica corretamente");
    }

    @Test
    @DisplayName("Deve retornar null para historyId não numérico")
    void deveRetornarNullParaHistoryIdNaoNumerico() {
        String jsonComHistoryIdInvalido = "{\"historyId\":\"abc123\"}";

        BigInteger historyId = notificationParser.extrairHistoryId(jsonComHistoryIdInvalido);

        assertNull(historyId, "HistoryId deve ser null para valor não numérico");
    }
}
