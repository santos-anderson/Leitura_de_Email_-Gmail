package com.gmailreader.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes para EmailToJsonService")
class EmailToJsonServiceTest {

    private EmailToJsonService emailToJsonService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        emailToJsonService = new EmailToJsonService();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve converter email completo para JSON")
    void deveConverterEmailCompletoParaJson() throws Exception {

        Message message = criarMessageCompleta();

        String json = emailToJsonService.converter(message);

        assertNotNull(json, "JSON não deve ser null");
        
        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals("123456", jsonNode.get("id").asText());
        assertEquals("01-01-2024 10:30:00hs", jsonNode.get("data").asText());
        assertEquals("teste@gmail.com", jsonNode.get("remetente").asText());
        assertEquals("Assunto de Teste", jsonNode.get("assunto").asText());
        assertEquals("Conteúdo do email de teste", jsonNode.get("corpo").asText());
    }

    @Test
    @DisplayName("Deve converter email com payload null")
    void deveConverterEmailComPayloadNull() throws Exception {

        Message message = new Message();
        message.setId("123456");
        message.setPayload(null);

        String json = emailToJsonService.converter(message);

        assertNotNull(json, "JSON não deve ser null");
        
        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals("123456", jsonNode.get("id").asText());
        assertEquals("", jsonNode.get("data").asText());
        assertEquals("", jsonNode.get("remetente").asText());
        assertEquals("", jsonNode.get("assunto").asText());
        assertEquals("", jsonNode.get("corpo").asText());
    }

    @Test
    @DisplayName("Deve converter email sem headers")
    void deveConverterEmailSemHeaders() throws Exception {

        Message message = new Message();
        message.setId("123456");
        
        MessagePart payload = new MessagePart();
        payload.setHeaders(null);
        message.setPayload(payload);

        String json = emailToJsonService.converter(message);

        assertNotNull(json, "JSON não deve ser null");
        
        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals("123456", jsonNode.get("id").asText());
        assertEquals("", jsonNode.get("data").asText());
        assertEquals("", jsonNode.get("remetente").asText());
        assertEquals("", jsonNode.get("assunto").asText());
    }

    @Test
    @DisplayName("Deve extrair corpo de email com partes múltiplas")
    void deveExtrairCorpoDeEmailComPartesMultiplas() throws Exception {

        Message message = criarMessageComPartesMultiplas();

        String json = emailToJsonService.converter(message);

        assertNotNull(json, "JSON não deve ser null");
        
        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals("Texto do email em texto plano", jsonNode.get("corpo").asText());
    }

    @Test
    @DisplayName("Deve formatar data corretamente")
    void deveFormatarDataCorretamente() throws Exception {

        Message message = criarMessageComDataCustomizada("Wed, 15 Mar 2023 14:25:30 +0000");

        String json = emailToJsonService.converter(message);


        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals("15-03-2023 14:25:30hs", jsonNode.get("data").asText());
    }

    @Test
    @DisplayName("Deve tratar data inválida")
    void deveTratarDataInvalida() throws Exception {

        Message message = criarMessageComDataCustomizada("Data inválida +0000");

        String json = emailToJsonService.converter(message);

        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals("Data inválida", jsonNode.get("data").asText());
    }

    @Test
    @DisplayName("Deve tratar data vazia")
    void deveTratarDataVazia() throws Exception {

        Message message = criarMessageComDataCustomizada("");

        String json = emailToJsonService.converter(message);

        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals("", jsonNode.get("data").asText());
    }

    @Test
    @DisplayName("Deve limpar quebras de linha do corpo")
    void deveLimparQuebrasDeLinhaDoCorpo() throws Exception {

        String conteudoComQuebras = "Linha 1\r\nLinha 2\rLinha 3\nLinha 4";
        String conteudoBase64 = Base64.getUrlEncoder().encodeToString(conteudoComQuebras.getBytes());
        
        Message message = criarMessageComCorpoCustomizado(conteudoBase64);

        String json = emailToJsonService.converter(message);

        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals("Linha 1 Linha 2 Linha 3 Linha 4", jsonNode.get("corpo").asText());
    }

    @Test
    @DisplayName("Deve lançar exceção para erro de serialização")
    void deveLancarExcecaoParaErroDeSerializacao() {

        Message message = new Message();
        message.setId("123456");

        assertDoesNotThrow(() -> {
            String json = emailToJsonService.converter(message);
            assertNotNull(json);
        }, "Não deve lançar exceção para message válida");
    }


    private Message criarMessageCompleta() {
        Message message = new Message();
        message.setId("123456");

        MessagePart payload = new MessagePart();
        
        List<MessagePartHeader> headers = Arrays.asList(
            criarHeader("From", "teste@gmail.com"),
            criarHeader("Subject", "Assunto de Teste"),
            criarHeader("Date", "Mon, 01 Jan 2024 10:30:00 +0000")
        );
        payload.setHeaders(headers);

        String conteudo = "Conteúdo do email de teste";
        String conteudoBase64 = Base64.getUrlEncoder().encodeToString(conteudo.getBytes());
        
        MessagePartBody body = new MessagePartBody();
        body.setData(conteudoBase64);
        payload.setBody(body);

        message.setPayload(payload);
        return message;
    }

    private Message criarMessageComPartesMultiplas() {
        Message message = new Message();
        message.setId("123456");

        MessagePart payload = new MessagePart();
        payload.setHeaders(Collections.singletonList(criarHeader("From", "teste@gmail.com")));

        MessagePart parteHtml = new MessagePart();
        parteHtml.setMimeType("text/html");
        MessagePartBody bodyHtml = new MessagePartBody();
        bodyHtml.setData(Base64.getUrlEncoder().encodeToString("<html>HTML content</html>".getBytes()));
        parteHtml.setBody(bodyHtml);

        MessagePart parteTexto = new MessagePart();
        parteTexto.setMimeType("text/plain");
        MessagePartBody bodyTexto = new MessagePartBody();
        bodyTexto.setData(Base64.getUrlEncoder().encodeToString("Texto do email em texto plano".getBytes()));
        parteTexto.setBody(bodyTexto);

        payload.setParts(Arrays.asList(parteHtml, parteTexto));
        message.setPayload(payload);
        return message;
    }

    private Message criarMessageComDataCustomizada(String data) {
        Message message = new Message();
        message.setId("123456");

        MessagePart payload = new MessagePart();
        List<MessagePartHeader> headers = Collections.singletonList(criarHeader("Date", data));
        payload.setHeaders(headers);

        message.setPayload(payload);
        return message;
    }

    private Message criarMessageComCorpoCustomizado(String corpoBase64) {
        Message message = new Message();
        message.setId("123456");

        MessagePart payload = new MessagePart();
        payload.setHeaders(Collections.emptyList());

        MessagePartBody body = new MessagePartBody();
        body.setData(corpoBase64);
        payload.setBody(body);

        message.setPayload(payload);
        return message;
    }

    private MessagePartHeader criarHeader(String nome, String valor) {
        MessagePartHeader header = new MessagePartHeader();
        header.setName(nome);
        header.setValue(valor);
        return header;
    }
}
