package com.gmailreader.service.provider;

import com.gmailreader.service.pagination.PaginationHandler;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GmailEmailProvider")
class GmailEmailProviderTest {

    @Mock
    private Gmail gmailService;

    @Mock
    private PaginationHandler paginationHandler;

    @Mock
    private Gmail.Users users;

    @Mock
    private Gmail.Users.Messages messages;

    @Mock
    private Gmail.Users.Messages.Get messageGet;

    @Mock
    private Gmail.Users.GetProfile getProfile;

    private GmailEmailProvider gmailEmailProvider;

    @BeforeEach
    void setUp() {
        gmailEmailProvider = new GmailEmailProvider(gmailService, paginationHandler);
        ReflectionTestUtils.setField(gmailEmailProvider, "progressLogInterval", 10);
    }

    @Test
    @DisplayName("Deve listar emails com sucesso")
    void deveListarEmailsComSucesso() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        List<String> messageIds = Arrays.asList("msg1", "msg2", "msg3");
        Message message1 = new Message().setId("msg1");
        Message message2 = new Message().setId("msg2");
        Message message3 = new Message().setId("msg3");

        when(paginationHandler.buscarMensagensPaginadas(gmailService, criteria)).thenReturn(messageIds);
        
        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.get("me", "msg1")).thenReturn(messageGet);
        when(messages.get("me", "msg2")).thenReturn(messageGet);
        when(messages.get("me", "msg3")).thenReturn(messageGet);
        when(messageGet.execute()).thenReturn(message1, message2, message3);

        List<Message> result = gmailEmailProvider.listarEmails(criteria);

        assertEquals(3, result.size());
        assertEquals("msg1", result.get(0).getId());
        assertEquals("msg2", result.get(1).getId());
        assertEquals("msg3", result.get(2).getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há emails")
    void deveRetornarListaVaziaQuandoNaoHaEmails() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        when(paginationHandler.buscarMensagensPaginadas(gmailService, criteria)).thenReturn(Collections.emptyList());

        List<Message> result = gmailEmailProvider.listarEmails(criteria);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve lançar IOException quando paginação falha")
    void deveLancarIOExceptionQuandoPaginacaoFalha() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        when(paginationHandler.buscarMensagensPaginadas(gmailService, criteria))
                .thenThrow(new IOException("Erro de conexão"));

        IOException exception = assertThrows(IOException.class,
                () -> gmailEmailProvider.listarEmails(criteria));

        assertEquals("Falha na comunicação com Gmail API", exception.getMessage());
    }

    @Test
    @DisplayName("Deve obter mensagem completa com sucesso")
    void deveObterMensagemCompletaComSucesso() throws Exception {
        String messageId = "msg123";
        Message expectedMessage = new Message().setId(messageId);

        configurarMockParaObterMensagem(messageId, expectedMessage);

        Message result = gmailEmailProvider.obterMensagemCompleta(messageId, "me");

        assertNotNull(result);
        assertEquals(messageId, result.getId());
    }

    @Test
    @DisplayName("Deve lançar IOException quando busca de mensagem falha")
    void deveLancarIOExceptionQuandoBuscaDeMensagemFalha() throws Exception {
        String messageId = "msg999";

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.get("me", messageId)).thenReturn(messageGet);
        when(messageGet.execute()).thenThrow(new IOException("Mensagem não encontrada"));

        IOException exception = assertThrows(IOException.class,
                () -> gmailEmailProvider.obterMensagemCompleta(messageId, "me"));

        assertTrue(exception.getMessage().contains("Falha ao recuperar mensagem: msg999"));
    }

    @Test
    @DisplayName("Deve verificar disponibilidade quando API está acessível")
    void deveVerificarDisponibilidadeQuandoAPIEstaAcessivel() throws Exception {
        Profile profile = new Profile();

        when(gmailService.users()).thenReturn(users);
        when(users.getProfile("me")).thenReturn(getProfile);
        when(getProfile.execute()).thenReturn(profile);

        boolean disponivel = gmailEmailProvider.isDisponivel();

        assertTrue(disponivel);
    }

    @Test
    @DisplayName("Deve retornar false quando API não está disponível")
    void deveRetornarFalseQuandoAPINaoEstaDisponivel() throws Exception {
        when(gmailService.users()).thenReturn(users);
        when(users.getProfile("me")).thenReturn(getProfile);
        when(getProfile.execute()).thenThrow(new IOException("Erro de conexão"));

        boolean disponivel = gmailEmailProvider.isDisponivel();

        assertFalse(disponivel);
    }

    @Test
    @DisplayName("Deve retornar nome do provedor")
    void deveRetornarNomeDoProvedor() {
        String nomeProvedor = gmailEmailProvider.getNomeProvedor();

        assertEquals("Gmail", nomeProvedor);
    }

    @Test
    @DisplayName("Deve continuar processamento quando uma mensagem falha")
    void deveContinuarProcessamentoQuandoUmaMensagemFalha() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        List<String> messageIds = Arrays.asList("msg1", "msg2", "msg3");
        Message message1 = new Message().setId("msg1");
        Message message3 = new Message().setId("msg3");

        when(paginationHandler.buscarMensagensPaginadas(gmailService, criteria)).thenReturn(messageIds);
        
        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        
        Gmail.Users.Messages.Get messageGet1 = mock(Gmail.Users.Messages.Get.class);
        Gmail.Users.Messages.Get messageGet2 = mock(Gmail.Users.Messages.Get.class);
        Gmail.Users.Messages.Get messageGet3 = mock(Gmail.Users.Messages.Get.class);
        
        when(messages.get("me", "msg1")).thenReturn(messageGet1);
        when(messageGet1.execute()).thenReturn(message1);
        
        when(messages.get("me", "msg2")).thenReturn(messageGet2);
        when(messageGet2.execute()).thenThrow(new IOException("Erro na msg2"));
        
        when(messages.get("me", "msg3")).thenReturn(messageGet3);
        when(messageGet3.execute()).thenReturn(message3);

        List<Message> result = gmailEmailProvider.listarEmails(criteria);

        assertEquals(2, result.size());
        assertEquals("msg1", result.get(0).getId());
        assertEquals("msg3", result.get(1).getId());
    }

    @Test
    @DisplayName("Deve processar múltiplas mensagens com userId customizado")
    void deveProcessarMultiplasMensagensComUserIdCustomizado() throws Exception {
        String customUserId = "user@example.com";
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId(customUserId)
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        List<String> messageIds = Arrays.asList("msg1", "msg2");
        Message message1 = new Message().setId("msg1");
        Message message2 = new Message().setId("msg2");

        when(paginationHandler.buscarMensagensPaginadas(gmailService, criteria)).thenReturn(messageIds);
        
        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        
        Gmail.Users.Messages.Get messageGet1 = mock(Gmail.Users.Messages.Get.class);
        Gmail.Users.Messages.Get messageGet2 = mock(Gmail.Users.Messages.Get.class);
        
        when(messages.get(customUserId, "msg1")).thenReturn(messageGet1);
        when(messageGet1.execute()).thenReturn(message1);
        when(messages.get(customUserId, "msg2")).thenReturn(messageGet2);
        when(messageGet2.execute()).thenReturn(message2);

        List<Message> result = gmailEmailProvider.listarEmails(criteria);

        assertEquals(2, result.size());
        verify(messages).get(customUserId, "msg1");
        verify(messages).get(customUserId, "msg2");
    }

    @Test
    @DisplayName("Deve lidar com lista grande de mensagens")
    void deveLidarComListaGrandeDeMensagens() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        List<String> messageIds = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            messageIds.add("msg" + i);
        }

        when(paginationHandler.buscarMensagensPaginadas(gmailService, criteria)).thenReturn(messageIds);
        
        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.get(eq("me"), anyString())).thenReturn(messageGet);
        when(messageGet.execute()).thenAnswer(invocation -> new Message().setId("msg"));

        List<Message> result = gmailEmailProvider.listarEmails(criteria);

        assertEquals(25, result.size());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando todas as mensagens falham")
    void deveRetornarListaVaziaQuandoTodasAsMensagensFalham() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        List<String> messageIds = Arrays.asList("msg1", "msg2");

        when(paginationHandler.buscarMensagensPaginadas(gmailService, criteria)).thenReturn(messageIds);
        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.get(eq("me"), anyString())).thenReturn(messageGet);
        when(messageGet.execute()).thenThrow(new IOException("Erro"));

        List<Message> result = gmailEmailProvider.listarEmails(criteria);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve usar progressLogInterval configurado")
    void deveUsarProgressLogIntervalConfigurado() throws Exception {
        ReflectionTestUtils.setField(gmailEmailProvider, "progressLogInterval", 5);

        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        List<String> messageIds = Arrays.asList("msg1", "msg2", "msg3", "msg4", "msg5");

        when(paginationHandler.buscarMensagensPaginadas(gmailService, criteria)).thenReturn(messageIds);
        
        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.get(eq("me"), anyString())).thenReturn(messageGet);
        when(messageGet.execute()).thenAnswer(invocation -> new Message().setId("msg"));

        List<Message> result = gmailEmailProvider.listarEmails(criteria);

        assertEquals(5, result.size());
    }

    @Test
    @DisplayName("Deve retornar false quando isDisponivel lança RuntimeException")
    void deveRetornarFalseQuandoIsDisponivelLancaRuntimeException() throws Exception {
        when(gmailService.users()).thenReturn(users);
        when(users.getProfile("me")).thenReturn(getProfile);
        when(getProfile.execute()).thenThrow(new RuntimeException("Erro inesperado"));

        boolean disponivel = gmailEmailProvider.isDisponivel();

        assertFalse(disponivel);
    }

    @Test
    @DisplayName("Deve obter mensagem com userId customizado")
    void deveObterMensagemComUserIdCustomizado() throws Exception {
        String customUserId = "custom@example.com";
        String messageId = "msg456";
        Message expectedMessage = new Message().setId(messageId);

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.get(customUserId, messageId)).thenReturn(messageGet);
        when(messageGet.execute()).thenReturn(expectedMessage);

        Message result = gmailEmailProvider.obterMensagemCompleta(messageId, customUserId);

        assertNotNull(result);
        assertEquals(messageId, result.getId());
        verify(messages).get(customUserId, messageId);
    }

    private void configurarMockParaObterMensagem(String messageId, Message message) throws IOException {
        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.get("me", messageId)).thenReturn(messageGet);
        when(messageGet.execute()).thenReturn(message);
    }
}
