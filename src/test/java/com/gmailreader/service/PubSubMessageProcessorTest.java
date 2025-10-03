package com.gmailreader.service;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PubSubMessageProcessor")
class PubSubMessageProcessorTest {

    @Mock
    private NotificationParser notificationParser;

    @Mock
    private GmailWatchService gmailWatchService;

    @Mock
    private AckReplyConsumer ackReplyConsumer;

    private PubSubMessageProcessor pubSubMessageProcessor;

    @BeforeEach
    void setUp() {
        pubSubMessageProcessor = new PubSubMessageProcessor(notificationParser, gmailWatchService);
    }

    @Test
    @DisplayName("Deve processar mensagem com historyId válido")
    void deveProcessarMensagemComHistoryIdValido() {

        String jsonNotification = "{\"historyId\":\"12345\"}";
        PubsubMessage message = criarPubsubMessage(jsonNotification);
        BigInteger historyId = new BigInteger("12345");

        when(notificationParser.extrairHistoryId(jsonNotification)).thenReturn(historyId);


        pubSubMessageProcessor.processarMensagem(message, ackReplyConsumer);


        verify(notificationParser).extrairHistoryId(jsonNotification);
        verify(gmailWatchService).processarEmailsDoHistorico(historyId);
        verify(ackReplyConsumer).ack();
    }

    @Test
    @DisplayName("Deve processar mensagem sem historyId e enviar ack")
    void deveProcessarMensagemSemHistoryIdEEnviarAck() {

        String jsonNotification = "{\"data\":\"teste\"}";
        PubsubMessage message = criarPubsubMessage(jsonNotification);

        when(notificationParser.extrairHistoryId(jsonNotification)).thenReturn(null);

        pubSubMessageProcessor.processarMensagem(message, ackReplyConsumer);

        verify(notificationParser).extrairHistoryId(jsonNotification);
        verify(gmailWatchService, never()).processarEmailsDoHistorico(any());
        verify(ackReplyConsumer).ack();
    }

    @Test
    @DisplayName("Deve enviar ack mesmo quando ocorre exceção no processamento")
    void deveEnviarAckMesmoQuandoOcorreExcecaoNoProcessamento() {

        String jsonNotification = "{\"historyId\":\"12345\"}";
        PubsubMessage message = criarPubsubMessage(jsonNotification);
        BigInteger historyId = new BigInteger("12345");

        when(notificationParser.extrairHistoryId(jsonNotification)).thenReturn(historyId);
        doThrow(new RuntimeException("Erro no processamento"))
                .when(gmailWatchService).processarEmailsDoHistorico(historyId);

        pubSubMessageProcessor.processarMensagem(message, ackReplyConsumer);

        verify(notificationParser).extrairHistoryId(jsonNotification);
        verify(gmailWatchService).processarEmailsDoHistorico(historyId);
        verify(ackReplyConsumer).ack();
    }

    @Test
    @DisplayName("Deve enviar ack quando parser lança exceção")
    void deveEnviarAckQuandoParserLancaExcecao() {

        String jsonNotification = "JSON inválido";
        PubsubMessage message = criarPubsubMessage(jsonNotification);

        when(notificationParser.extrairHistoryId(jsonNotification))
                .thenThrow(new RuntimeException("Erro ao parsear JSON"));

        pubSubMessageProcessor.processarMensagem(message, ackReplyConsumer);

        verify(notificationParser).extrairHistoryId(jsonNotification);
        verify(gmailWatchService, never()).processarEmailsDoHistorico(any());
        verify(ackReplyConsumer).ack();
    }

    @Test
    @DisplayName("Deve processar mensagem com historyId grande")
    void deveProcessarMensagemComHistoryIdGrande() {

        String jsonNotification = "{\"historyId\":\"999999999999999999\"}";
        PubsubMessage message = criarPubsubMessage(jsonNotification);
        BigInteger historyId = new BigInteger("999999999999999999");

        when(notificationParser.extrairHistoryId(jsonNotification)).thenReturn(historyId);

        pubSubMessageProcessor.processarMensagem(message, ackReplyConsumer);

        verify(notificationParser).extrairHistoryId(jsonNotification);
        verify(gmailWatchService).processarEmailsDoHistorico(historyId);
        verify(ackReplyConsumer).ack();
    }

    @Test
    @DisplayName("Deve processar mensagem vazia")
    void deveProcessarMensagemVazia() {

        String jsonNotification = "";
        PubsubMessage message = criarPubsubMessage(jsonNotification);

        when(notificationParser.extrairHistoryId(jsonNotification)).thenReturn(null);

        pubSubMessageProcessor.processarMensagem(message, ackReplyConsumer);

        verify(notificationParser).extrairHistoryId(jsonNotification);
        verify(gmailWatchService, never()).processarEmailsDoHistorico(any());
        verify(ackReplyConsumer).ack();
    }

    @Test
    @DisplayName("Deve processar múltiplas mensagens sequencialmente")
    void deveProcessarMultiplasMensagensSequencialmente() {

        String json1 = "{\"historyId\":\"100\"}";
        String json2 = "{\"historyId\":\"200\"}";
        String json3 = "{\"historyId\":\"300\"}";
        
        PubsubMessage message1 = criarPubsubMessage(json1);
        PubsubMessage message2 = criarPubsubMessage(json2);
        PubsubMessage message3 = criarPubsubMessage(json3);
        
        BigInteger historyId1 = new BigInteger("100");
        BigInteger historyId2 = new BigInteger("200");
        BigInteger historyId3 = new BigInteger("300");

        when(notificationParser.extrairHistoryId(json1)).thenReturn(historyId1);
        when(notificationParser.extrairHistoryId(json2)).thenReturn(historyId2);
        when(notificationParser.extrairHistoryId(json3)).thenReturn(historyId3);

        pubSubMessageProcessor.processarMensagem(message1, ackReplyConsumer);
        pubSubMessageProcessor.processarMensagem(message2, ackReplyConsumer);
        pubSubMessageProcessor.processarMensagem(message3, ackReplyConsumer);

        verify(gmailWatchService).processarEmailsDoHistorico(historyId1);
        verify(gmailWatchService).processarEmailsDoHistorico(historyId2);
        verify(gmailWatchService).processarEmailsDoHistorico(historyId3);
        verify(ackReplyConsumer, times(3)).ack();
    }

    @Test
    @DisplayName("Deve processar mensagem com JSON complexo")
    void deveProcessarMensagemComJsonComplexo() {

        String jsonComplexo = "{\"emailAddress\":\"user@example.com\",\"historyId\":\"54321\",\"expiration\":\"1234567890\"}";
        PubsubMessage message = criarPubsubMessage(jsonComplexo);
        BigInteger historyId = new BigInteger("54321");

        when(notificationParser.extrairHistoryId(jsonComplexo)).thenReturn(historyId);

        pubSubMessageProcessor.processarMensagem(message, ackReplyConsumer);

        verify(notificationParser).extrairHistoryId(jsonComplexo);
        verify(gmailWatchService).processarEmailsDoHistorico(historyId);
        verify(ackReplyConsumer).ack();
    }

    @Test
    @DisplayName("Deve garantir que ack é sempre chamado uma única vez")
    void deveGarantirQueAckESempreChamadoUmaUnicaVez() {

        String jsonNotification = "{\"historyId\":\"12345\"}";
        PubsubMessage message = criarPubsubMessage(jsonNotification);
        BigInteger historyId = new BigInteger("12345");

        when(notificationParser.extrairHistoryId(jsonNotification)).thenReturn(historyId);

        pubSubMessageProcessor.processarMensagem(message, ackReplyConsumer);

        verify(ackReplyConsumer, times(1)).ack();
        verify(ackReplyConsumer, never()).nack();
    }

    @Test
    @DisplayName("Deve processar mensagem com caracteres especiais UTF-8")
    void deveProcessarMensagemComCaracteresEspeciaisUTF8() {

        String jsonNotification = "{\"historyId\":\"12345\",\"data\":\"Olá Mundo! 你好 🌍\"}";
        PubsubMessage message = criarPubsubMessage(jsonNotification);
        BigInteger historyId = new BigInteger("12345");

        when(notificationParser.extrairHistoryId(jsonNotification)).thenReturn(historyId);

        pubSubMessageProcessor.processarMensagem(message, ackReplyConsumer);

        verify(notificationParser).extrairHistoryId(jsonNotification);
        verify(gmailWatchService).processarEmailsDoHistorico(historyId);
        verify(ackReplyConsumer).ack();
    }

    private PubsubMessage criarPubsubMessage(String data) {
        return PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(data))
                .build();
    }
}
