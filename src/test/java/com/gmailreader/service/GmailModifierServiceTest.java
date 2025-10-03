package com.gmailreader.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GmailModifierService")
class GmailModifierServiceTest {

    @Mock
    private Gmail gmailService;

    @Mock
    private Gmail.Users users;

    @Mock
    private Gmail.Users.Messages messages;

    @Mock
    private Gmail.Users.Messages.Modify modify;

    private GmailModifierService gmailModifierService;

    @BeforeEach
    void setUp() {
        gmailModifierService = new GmailModifierService(gmailService);
    }

    @Test
    @DisplayName("Deve marcar email como lido com sucesso")
    void deveMarcarEmailComoLidoComSucesso() throws Exception {
        String messageId = "msg123";
        Message message = new Message();

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(modify);
        when(modify.execute()).thenReturn(message);

        assertDoesNotThrow(() -> gmailModifierService.marcarComoLido(messageId));

        verify(gmailService.users()).messages();
        verify(messages).modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class));
        verify(modify).execute();
    }

    @Test
    @DisplayName("Deve usar userId 'me' na requisição")
    void deveUsarUserIdMeNaRequisicao() throws Exception {
        String messageId = "msg456";
        Message message = new Message();

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(modify);
        when(modify.execute()).thenReturn(message);

        gmailModifierService.marcarComoLido(messageId);

        verify(messages).modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class));
    }

    @Test
    @DisplayName("Deve remover label UNREAD")
    void deveRemoverLabelUnread() throws Exception {
        String messageId = "msg789";
        Message message = new Message();

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(modify);
        when(modify.execute()).thenReturn(message);

        gmailModifierService.marcarComoLido(messageId);

        verify(messages).modify(eq("me"), eq(messageId), argThat(request ->
            request.getRemoveLabelIds() != null &&
            request.getRemoveLabelIds().contains("UNREAD")
        ));
    }

    @Test
    @DisplayName("Deve lançar IOException quando API falha")
    void deveLancarIOExceptionQuandoAPIFalha() throws Exception {
        String messageId = "msg999";

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(modify);
        when(modify.execute()).thenThrow(new IOException("Erro de conexão"));

        assertThrows(IOException.class, () -> gmailModifierService.marcarComoLido(messageId));

        verify(modify).execute();
    }

    @Test
    @DisplayName("Deve processar messageId com caracteres especiais")
    void deveProcessarMessageIdComCaracteresEspeciais() throws Exception {
        String messageId = "msg-123_abc@xyz";
        Message message = new Message();

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(modify);
        when(modify.execute()).thenReturn(message);

        gmailModifierService.marcarComoLido(messageId);

        verify(messages).modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class));
    }

    @Test
    @DisplayName("Deve processar messageId muito longo")
    void deveProcessarMessageIdMuitoLongo() throws Exception {
        String messageId = "a".repeat(500);
        Message message = new Message();

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(modify);
        when(modify.execute()).thenReturn(message);

        gmailModifierService.marcarComoLido(messageId);

        verify(messages).modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class));
    }

    @Test
    @DisplayName("Deve marcar múltiplos emails como lidos sequencialmente")
    void deveMarcarMultiplosEmailsComoLidosSequencialmente() throws Exception {
        String[] messageIds = {"msg1", "msg2", "msg3"};
        Message message = new Message();

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.modify(eq("me"), anyString(), any(ModifyMessageRequest.class))).thenReturn(modify);
        when(modify.execute()).thenReturn(message);

        for (String messageId : messageIds) {
            gmailModifierService.marcarComoLido(messageId);
        }

        verify(modify, times(3)).execute();
    }

    @Test
    @DisplayName("Deve criar ModifyMessageRequest corretamente")
    void deveCriarModifyMessageRequestCorretamente() throws Exception {
        String messageId = "msg111";
        Message message = new Message();

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(modify);
        when(modify.execute()).thenReturn(message);

        gmailModifierService.marcarComoLido(messageId);

        verify(messages).modify(eq("me"), eq(messageId), argThat(request ->
            request.getRemoveLabelIds() != null &&
            request.getRemoveLabelIds().size() == 1 &&
            request.getRemoveLabelIds().get(0).equals("UNREAD")
        ));
    }

    @Test
    @DisplayName("Deve propagar IOException original")
    void devePropagaIOExceptionOriginal() throws Exception {
        String messageId = "msg222";
        IOException originalException = new IOException("Timeout na conexão");

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(modify);
        when(modify.execute()).thenThrow(originalException);

        IOException thrown = assertThrows(IOException.class, 
            () -> gmailModifierService.marcarComoLido(messageId));

        assertEquals("Timeout na conexão", thrown.getMessage());
    }

    @Test
    @DisplayName("Deve processar messageId vazio")
    void deveProcessarMessageIdVazio() throws Exception {
        String messageId = "";
        Message message = new Message();

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(modify);
        when(modify.execute()).thenReturn(message);

        gmailModifierService.marcarComoLido(messageId);

        verify(messages).modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class));
    }
}
