package com.gmailreader.service;

import com.gmailreader.service.provider.EmailProvider;
import com.gmailreader.service.provider.EmailSearchCriteria;
import com.google.api.services.gmail.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GmailReaderService")
class GmailReaderServiceTest {

    @Mock
    private EmailProvider emailProvider;

    private GmailReaderService gmailReaderService;

    @BeforeEach
    void setUp() {
        gmailReaderService = new GmailReaderService(emailProvider);
        ReflectionTestUtils.setField(gmailReaderService, "userId", "me");
        ReflectionTestUtils.setField(gmailReaderService, "defaultLabels", Collections.singletonList("INBOX"));
        ReflectionTestUtils.setField(gmailReaderService, "maxResults", null);
    }

    @Test
    @DisplayName("Deve listar emails com configurações padrão")
    void deveListarEmailsComConfiguracoespadrao() throws Exception {
        List<Message> expectedMessages = Arrays.asList(
                new Message().setId("msg1"),
                new Message().setId("msg2")
        );

        when(emailProvider.isDisponivel()).thenReturn(true);
        when(emailProvider.listarEmails(any(EmailSearchCriteria.class))).thenReturn(expectedMessages);
        when(emailProvider.getNomeProvedor()).thenReturn("Gmail");

        List<Message> result = gmailReaderService.listarEmails();

        assertEquals(2, result.size());
        verify(emailProvider).isDisponivel();
        verify(emailProvider).listarEmails(any(EmailSearchCriteria.class));
    }

    @Test
    @DisplayName("Deve listar emails com critérios personalizados")
    void deveListarEmailsComCriteriosPersonalizados() throws Exception {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("custom@example.com")
                .labelIds(Arrays.asList("INBOX", "UNREAD"))
                .build();

        List<Message> expectedMessages = Collections.singletonList(new Message().setId("msg1"));

        when(emailProvider.isDisponivel()).thenReturn(true);
        when(emailProvider.listarEmails(criteria)).thenReturn(expectedMessages);
        when(emailProvider.getNomeProvedor()).thenReturn("Gmail");

        List<Message> result = gmailReaderService.listarEmails(criteria);

        assertEquals(1, result.size());
        verify(emailProvider).listarEmails(criteria);
    }

    @Test
    @DisplayName("Deve lançar exceção quando provider não está disponível")
    void deveLancarExcecaoQuandoProviderNaoEstaDisponivel() {
        when(emailProvider.isDisponivel()).thenReturn(false);
        when(emailProvider.getNomeProvedor()).thenReturn("Gmail");

        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("me")
                .labelIds(Collections.singletonList("INBOX"))
                .build();

        IOException exception = assertThrows(IOException.class,
                () -> gmailReaderService.listarEmails(criteria));

        assertTrue(exception.getMessage().contains("não está disponível"));
    }

    @Test
    @DisplayName("Deve listar emails excluindo IDs específicos")
    void deveListarEmailsExcluindoIdsEspecificos() throws Exception {
        List<String> idsExcluidos = Arrays.asList("msg1", "msg2");
        List<Message> expectedMessages = Collections.singletonList(new Message().setId("msg3"));

        when(emailProvider.isDisponivel()).thenReturn(true);
        when(emailProvider.listarEmails(any(EmailSearchCriteria.class))).thenReturn(expectedMessages);
        when(emailProvider.getNomeProvedor()).thenReturn("Gmail");

        List<Message> result = gmailReaderService.listarEmailsExcluindo(idsExcluidos);

        assertEquals(1, result.size());
        verify(emailProvider).listarEmails(argThat(criteria ->
                criteria.getIdsExcluidos().equals(idsExcluidos)
        ));
    }

    @Test
    @DisplayName("Deve propagar IOException do provider")
    void devePropagaIOExceptionDoProvider() throws Exception {
        when(emailProvider.isDisponivel()).thenReturn(true);
        when(emailProvider.listarEmails(any(EmailSearchCriteria.class)))
                .thenThrow(new IOException("Erro de conexão"));

        assertThrows(IOException.class, () -> gmailReaderService.listarEmails());
    }

    @Test
    @DisplayName("Deve obter informação do provedor quando disponível")
    void deveObterInformacaoDoProvedorQuandoDisponivel() {
        when(emailProvider.getNomeProvedor()).thenReturn("Gmail");
        when(emailProvider.isDisponivel()).thenReturn(true);

        String info = gmailReaderService.obterInfoProvedor();

        assertEquals("Provedor: Gmail (disponível)", info);
    }

    @Test
    @DisplayName("Deve obter informação do provedor quando indisponível")
    void deveObterInformacaoDoProvedorQuandoIndisponivel() {
        when(emailProvider.getNomeProvedor()).thenReturn("Gmail");
        when(emailProvider.isDisponivel()).thenReturn(false);

        String info = gmailReaderService.obterInfoProvedor();

        assertEquals("Provedor: Gmail (indisponível)", info);
    }

    @Test
    @DisplayName("Deve usar userId configurado")
    void deveUsarUserIdConfigurado() throws Exception {
        ReflectionTestUtils.setField(gmailReaderService, "userId", "custom@example.com");

        when(emailProvider.isDisponivel()).thenReturn(true);
        when(emailProvider.listarEmails(any(EmailSearchCriteria.class))).thenReturn(Collections.emptyList());
        when(emailProvider.getNomeProvedor()).thenReturn("Gmail");

        gmailReaderService.listarEmails();

        verify(emailProvider).listarEmails(argThat(criteria ->
                criteria.getUserId().equals("custom@example.com")
        ));
    }

    @Test
    @DisplayName("Deve usar labels configurados")
    void deveUsarLabelsConfigurados() throws Exception {
        List<String> customLabels = Arrays.asList("INBOX", "UNREAD", "IMPORTANT");
        ReflectionTestUtils.setField(gmailReaderService, "defaultLabels", customLabels);

        when(emailProvider.isDisponivel()).thenReturn(true);
        when(emailProvider.listarEmails(any(EmailSearchCriteria.class))).thenReturn(Collections.emptyList());
        when(emailProvider.getNomeProvedor()).thenReturn("Gmail");

        gmailReaderService.listarEmails();

        verify(emailProvider).listarEmails(argThat(criteria ->
                criteria.getLabelIds().equals(customLabels)
        ));
    }

    @Test
    @DisplayName("Deve usar maxResults configurado")
    void deveUsarMaxResultsConfigurado() throws Exception {
        ReflectionTestUtils.setField(gmailReaderService, "maxResults", 50);

        when(emailProvider.isDisponivel()).thenReturn(true);
        when(emailProvider.listarEmails(any(EmailSearchCriteria.class))).thenReturn(Collections.emptyList());
        when(emailProvider.getNomeProvedor()).thenReturn("Gmail");

        gmailReaderService.listarEmails();

        verify(emailProvider).listarEmails(argThat(criteria ->
                criteria.getMaxResults() != null && criteria.getMaxResults() == 50
        ));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há emails")
    void deveRetornarListaVaziaQuandoNaoHaEmails() throws Exception {
        when(emailProvider.isDisponivel()).thenReturn(true);
        when(emailProvider.listarEmails(any(EmailSearchCriteria.class))).thenReturn(Collections.emptyList());
        when(emailProvider.getNomeProvedor()).thenReturn("Gmail");

        List<Message> result = gmailReaderService.listarEmails();

        assertTrue(result.isEmpty());
    }
}
