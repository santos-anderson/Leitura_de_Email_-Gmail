package com.gmailreader.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.ListHistoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GmailHistoryService")
class GmailHistoryServiceTest {

    @Mock
    private GmailServiceManager gmailServiceManager;

    @Mock
    private Gmail gmailService;

    @Mock
    private Gmail.Users users;

    @Mock
    private Gmail.Users.History history;

    @Mock
    private Gmail.Users.History.List historyList;

    private GmailHistoryService gmailHistoryService;

    @BeforeEach
    void setUp() {
        gmailHistoryService = new GmailHistoryService(gmailServiceManager);
    }

    @Test
    @DisplayName("Deve buscar histórico com sucesso")
    void deveBuscarHistoricoComSucesso() throws Exception {

        BigInteger historyId = new BigInteger("12345");
        List<History> expectedHistory = Arrays.asList(
                new History().setId(new BigInteger("12346")),
                new History().setId(new BigInteger("12347"))
        );

        configurarMocksParaBuscaHistorico(expectedHistory);

        List<History> result = gmailHistoryService.buscarHistorico(historyId);

        assertNotNull(result, "Resultado não deve ser null");
        assertEquals(2, result.size(), "Deve retornar 2 históricos");
        assertEquals(expectedHistory, result, "Deve retornar a lista de históricos esperada");
        
        verify(gmailServiceManager).getGmailService();
        verify(historyList).setStartHistoryId(historyId);
        verify(historyList).execute();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há histórico")
    void deveRetornarListaVaziaQuandoNaoHaHistorico() throws Exception {

        BigInteger historyId = new BigInteger("12345");
        configurarMocksParaBuscaHistorico(null);

        List<History> result = gmailHistoryService.buscarHistorico(historyId);

        assertNotNull(result, "Resultado não deve ser null");
        assertTrue(result.isEmpty(), "Deve retornar lista vazia");
        
        verify(gmailServiceManager).getGmailService();
        verify(historyList).setStartHistoryId(historyId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando ocorre IOException")
    void deveRetornarListaVaziaQuandoOcorreIOException() throws Exception {

        BigInteger historyId = new BigInteger("12345");
        
        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.history()).thenReturn(history);
        when(history.list("me")).thenReturn(historyList);
        when(historyList.setStartHistoryId(historyId)).thenReturn(historyList);
        when(historyList.execute()).thenThrow(new IOException("Erro de conexão"));

        List<History> result = gmailHistoryService.buscarHistorico(historyId);

        assertNotNull(result, "Resultado não deve ser null");
        assertTrue(result.isEmpty(), "Deve retornar lista vazia quando ocorre erro");
        
        verify(gmailServiceManager).getGmailService();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando ocorre exceção genérica")
    void deveRetornarListaVaziaQuandoOcorreExcecaoGenerica() throws Exception {

        BigInteger historyId = new BigInteger("12345");
        
        when(gmailServiceManager.getGmailService()).thenThrow(new RuntimeException("Erro inesperado"));

        List<History> result = gmailHistoryService.buscarHistorico(historyId);


        assertNotNull(result, "Resultado não deve ser null");
        assertTrue(result.isEmpty(), "Deve retornar lista vazia quando ocorre erro");
        
        verify(gmailServiceManager).getGmailService();
    }

    @Test
    @DisplayName("Deve verificar que tem alterações quando histórico não está vazio")
    void deveVerificarQueTemAlteracoesQuandoHistoricoNaoEstaVazio() throws Exception {

        BigInteger historyId = new BigInteger("12345");
        List<History> expectedHistory = Collections.singletonList(
                new History().setId(new BigInteger("12346"))
        );

        configurarMocksParaBuscaHistorico(expectedHistory);

        boolean temAlteracoes = gmailHistoryService.temAlteracoes(historyId);

        assertTrue(temAlteracoes, "Deve retornar true quando há alterações");
        verify(gmailServiceManager).getGmailService();
    }

    @Test
    @DisplayName("Deve verificar que não tem alterações quando histórico está vazio")
    void deveVerificarQueNaoTemAlteracoesQuandoHistoricoEstaVazio() throws Exception {

        BigInteger historyId = new BigInteger("12345");
        configurarMocksParaBuscaHistorico(null);

        boolean temAlteracoes = gmailHistoryService.temAlteracoes(historyId);

        assertFalse(temAlteracoes, "Deve retornar false quando não há alterações");
        verify(gmailServiceManager).getGmailService();
    }

    @Test
    @DisplayName("Deve buscar histórico com historyId muito grande")
    void deveBuscarHistoricoComHistoryIdMuitoGrande() throws Exception {

        BigInteger historyId = new BigInteger("999999999999999999999");
        List<History> expectedHistory = Collections.singletonList(
                new History().setId(new BigInteger("1000000000000000000000"))
        );

        configurarMocksParaBuscaHistorico(expectedHistory);

        List<History> result = gmailHistoryService.buscarHistorico(historyId);

        assertNotNull(result, "Resultado não deve ser null");
        assertEquals(1, result.size(), "Deve retornar 1 histórico");
        verify(historyList).setStartHistoryId(historyId);
    }

    @Test
    @DisplayName("Deve buscar histórico múltiplas vezes com diferentes historyIds")
    void deveBuscarHistoricoMultiplasVezesComDiferentesHistoryIds() throws Exception {

        BigInteger historyId1 = new BigInteger("100");
        BigInteger historyId2 = new BigInteger("200");
        BigInteger historyId3 = new BigInteger("300");

        List<History> history1 = Collections.singletonList(new History().setId(new BigInteger("101")));
        List<History> history2 = Collections.singletonList(new History().setId(new BigInteger("201")));
        List<History> history3 = Collections.emptyList();

        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.history()).thenReturn(history);
        when(history.list("me")).thenReturn(historyList);
        when(historyList.setStartHistoryId(any())).thenReturn(historyList);

        ListHistoryResponse response1 = new ListHistoryResponse().setHistory(history1);
        ListHistoryResponse response2 = new ListHistoryResponse().setHistory(history2);
        ListHistoryResponse response3 = new ListHistoryResponse().setHistory(null);

        when(historyList.execute()).thenReturn(response1, response2, response3);

        List<History> result1 = gmailHistoryService.buscarHistorico(historyId1);
        List<History> result2 = gmailHistoryService.buscarHistorico(historyId2);
        List<History> result3 = gmailHistoryService.buscarHistorico(historyId3);

        assertEquals(1, result1.size(), "Primeira busca deve retornar 1 histórico");
        assertEquals(1, result2.size(), "Segunda busca deve retornar 1 histórico");
        assertTrue(result3.isEmpty(), "Terceira busca deve retornar lista vazia");
        
        verify(historyList, times(3)).execute();
    }

    @Test
    @DisplayName("Deve lidar com lista de histórico vazia retornada pela API")
    void deveLidarComListaDeHistoricoVaziaRetornadaPelaAPI() throws Exception {

        BigInteger historyId = new BigInteger("12345");
        configurarMocksParaBuscaHistorico(Collections.emptyList());

        List<History> result = gmailHistoryService.buscarHistorico(historyId);

        assertNotNull(result, "Resultado não deve ser null");
        assertTrue(result.isEmpty(), "Deve retornar lista vazia");
        verify(gmailServiceManager).getGmailService();
    }

    @Test
    @DisplayName("Deve buscar histórico com múltiplas alterações")
    void deveBuscarHistoricoComMultiplasAlteracoes() throws Exception {

        BigInteger historyId = new BigInteger("12345");
        List<History> expectedHistory = Arrays.asList(
                new History().setId(new BigInteger("12346")),
                new History().setId(new BigInteger("12347")),
                new History().setId(new BigInteger("12348")),
                new History().setId(new BigInteger("12349")),
                new History().setId(new BigInteger("12350"))
        );

        configurarMocksParaBuscaHistorico(expectedHistory);

        List<History> result = gmailHistoryService.buscarHistorico(historyId);

        assertNotNull(result, "Resultado não deve ser null");
        assertEquals(5, result.size(), "Deve retornar 5 históricos");
        assertEquals(expectedHistory, result, "Deve retornar todos os históricos");
    }

    @Test
    @DisplayName("Deve retornar false em temAlteracoes quando ocorre erro")
    void deveRetornarFalseEmTemAlteracoesQuandoOcorreErro() throws Exception {

        BigInteger historyId = new BigInteger("12345");
        
        when(gmailServiceManager.getGmailService()).thenThrow(new RuntimeException("Erro"));

        boolean temAlteracoes = gmailHistoryService.temAlteracoes(historyId);


        assertFalse(temAlteracoes, "Deve retornar false quando ocorre erro");
    }

    private void configurarMocksParaBuscaHistorico(List<History> historyList) throws Exception {
        when(gmailServiceManager.getGmailService()).thenReturn(gmailService);
        when(gmailService.users()).thenReturn(users);
        when(users.history()).thenReturn(history);
        when(history.list("me")).thenReturn(this.historyList);
        when(this.historyList.setStartHistoryId(any())).thenReturn(this.historyList);
        
        ListHistoryResponse response = new ListHistoryResponse().setHistory(historyList);
        when(this.historyList.execute()).thenReturn(response);
    }
}
