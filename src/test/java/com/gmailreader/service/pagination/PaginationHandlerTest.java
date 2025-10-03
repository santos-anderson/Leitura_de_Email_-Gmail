package com.gmailreader.service.pagination;

import com.gmailreader.service.provider.EmailSearchCriteria;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaginationHandler")
class PaginationHandlerTest {

    @Mock
    private Gmail gmailService;

    @Mock
    private Gmail.Users users;

    @Mock
    private Gmail.Users.Messages messages;

    @Mock
    private Gmail.Users.Messages.List listRequest;

    private PaginationHandler paginationHandler;

    @BeforeEach
    void setUp() {
        paginationHandler = new PaginationHandler();
    }

    @Test
    @DisplayName("Deve buscar mensagens de uma única página")
    void deveBuscarMensagensDeUmaUnicaPagina() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        ListMessagesResponse response = new ListMessagesResponse();
        response.setMessages(Arrays.asList(
                new Message().setId("msg1"),
                new Message().setId("msg2"),
                new Message().setId("msg3")
        ));
        response.setNextPageToken(null);

        configurarMocks(response);

        List<String> result = paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

        assertEquals(3, result.size());
        assertTrue(result.contains("msg1"));
        assertTrue(result.contains("msg2"));
        assertTrue(result.contains("msg3"));
    }

    @Test
    @DisplayName("Deve buscar mensagens de múltiplas páginas")
    void deveBuscarMensagensDeMultiplasPaginas() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        ListMessagesResponse response1 = new ListMessagesResponse();
        response1.setMessages(Arrays.asList(
                new Message().setId("msg1"),
                new Message().setId("msg2")
        ));
        response1.setNextPageToken("token1");

        ListMessagesResponse response2 = new ListMessagesResponse();
        response2.setMessages(Arrays.asList(
                new Message().setId("msg3"),
                new Message().setId("msg4")
        ));
        response2.setNextPageToken(null);

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.list(anyString())).thenReturn(listRequest);
        when(listRequest.setLabelIds(anyList())).thenReturn(listRequest);
        when(listRequest.setPageToken(any())).thenReturn(listRequest);
        when(listRequest.execute()).thenReturn(response1, response2);

        List<String> result = paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

        assertEquals(4, result.size());
        assertEquals(Arrays.asList("msg1", "msg2", "msg3", "msg4"), result);
    }

    @Test
    @DisplayName("Deve excluir mensagens conforme critério")
    void deveExcluirMensagensConformeCriterio() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .idsExcluidos(Arrays.asList("msg2", "msg4"))
                .build();

        ListMessagesResponse response = new ListMessagesResponse();
        response.setMessages(Arrays.asList(
                new Message().setId("msg1"),
                new Message().setId("msg2"),
                new Message().setId("msg3"),
                new Message().setId("msg4")
        ));
        response.setNextPageToken(null);

        configurarMocks(response);

        List<String> result = paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

        assertEquals(2, result.size());
        assertTrue(result.contains("msg1"));
        assertTrue(result.contains("msg3"));
        assertFalse(result.contains("msg2"));
        assertFalse(result.contains("msg4"));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há mensagens")
    void deveRetornarListaVaziaQuandoNaoHaMensagens() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        ListMessagesResponse response = new ListMessagesResponse();
        response.setMessages(null);
        response.setNextPageToken(null);

        configurarMocks(response);

        List<String> result = paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve aplicar query quando fornecida")
    void deveAplicarQueryQuandoFornecida() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .query("subject:test")
                .build();

        ListMessagesResponse response = new ListMessagesResponse();
        response.setMessages(Collections.singletonList(new Message().setId("msg1")));
        response.setNextPageToken(null);

        configurarMocks(response);

        paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

        verify(listRequest).setQ("subject:test");
    }

    @Test
    @DisplayName("Deve aplicar maxResults quando fornecido")
    void deveAplicarMaxResultsQuandoFornecido() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .maxResults(10)
                .build();

        ListMessagesResponse response = new ListMessagesResponse();
        response.setMessages(Collections.singletonList(new Message().setId("msg1")));
        response.setNextPageToken(null);

        configurarMocks(response);

        paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

        verify(listRequest).setMaxResults(10L);
    }

    @Test
    @DisplayName("Deve ignorar query vazia")
    void deveIgnorarQueryVazia() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .query("")
                .build();

        ListMessagesResponse response = new ListMessagesResponse();
        response.setMessages(Collections.singletonList(new Message().setId("msg1")));
        response.setNextPageToken(null);

        configurarMocks(response);

        paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

        verify(listRequest, never()).setQ(anyString());
    }

    @Test
    @DisplayName("Deve ignorar query com apenas espaços")
    void deveIgnorarQueryComApenasEspacos() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .query("   ")
                .build();

        ListMessagesResponse response = new ListMessagesResponse();
        response.setMessages(Collections.singletonList(new Message().setId("msg1")));
        response.setNextPageToken(null);

        configurarMocks(response);

        paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

        verify(listRequest, never()).setQ(anyString());
    }

    @Test
    @DisplayName("Deve lançar IOException quando API falha")
    void deveLancarIOExceptionQuandoAPIFalha() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.list(anyString())).thenReturn(listRequest);
        when(listRequest.setLabelIds(anyList())).thenReturn(listRequest);
        when(listRequest.setPageToken(any())).thenReturn(listRequest);
        when(listRequest.execute()).thenThrow(new IOException("Erro de conexão"));

        assertThrows(IOException.class, 
            () -> paginationHandler.buscarMensagensPaginadas(gmailService, criteria));
    }

    @Test
    @DisplayName("Deve processar múltiplas páginas até não haver mais tokens")
    void deveProcessarMultiplasPaginasAteNaoHaverMaisTokens() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        ListMessagesResponse response1 = criarResponse(Arrays.asList("msg1"), "token1");
        ListMessagesResponse response2 = criarResponse(Arrays.asList("msg2"), "token2");
        ListMessagesResponse response3 = criarResponse(Arrays.asList("msg3"), null);

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.list(anyString())).thenReturn(listRequest);
        when(listRequest.setLabelIds(anyList())).thenReturn(listRequest);
        when(listRequest.setPageToken(any())).thenReturn(listRequest);
        when(listRequest.execute()).thenReturn(response1, response2, response3);

        List<String> result = paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

        assertEquals(3, result.size());
        verify(listRequest, times(3)).execute();
    }

    @Test
    @DisplayName("Deve usar userId do critério")
    void deveUsarUserIdDoCriterio() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("custom-user@example.com")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        ListMessagesResponse response = new ListMessagesResponse();
        response.setMessages(Collections.emptyList());
        response.setNextPageToken(null);

        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.list("custom-user@example.com")).thenReturn(listRequest);
        when(listRequest.setLabelIds(anyList())).thenReturn(listRequest);
        when(listRequest.setPageToken(any())).thenReturn(listRequest);
        when(listRequest.execute()).thenReturn(response);

        paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

        verify(messages).list("custom-user@example.com");
    }

    @Test
    @DisplayName("Deve usar labelIds do critério")
    void deveUsarLabelIdsDoCriterio() throws Exception {
        List<String> labels = Arrays.asList("INBOX", "UNREAD");
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(labels)
                .build();

        ListMessagesResponse response = new ListMessagesResponse();
        response.setMessages(Collections.emptyList());
        response.setNextPageToken(null);

        configurarMocks(response);

        paginationHandler.buscarMensagensPaginadas(gmailService, criteria);

        verify(listRequest).setLabelIds(labels);
    }

    private void configurarMocks(ListMessagesResponse response) throws IOException {
        when(gmailService.users()).thenReturn(users);
        when(users.messages()).thenReturn(messages);
        when(messages.list(anyString())).thenReturn(listRequest);
        when(listRequest.setLabelIds(anyList())).thenReturn(listRequest);
        when(listRequest.setPageToken(any())).thenReturn(listRequest);
        when(listRequest.execute()).thenReturn(response);
    }

    private ListMessagesResponse criarResponse(List<String> messageIds, String nextPageToken) {
        ListMessagesResponse response = new ListMessagesResponse();
        List<Message> messages = messageIds.stream()
                .map(id -> new Message().setId(id))
                .toList();
        response.setMessages(messages);
        response.setNextPageToken(nextPageToken);
        return response;
    }
}
