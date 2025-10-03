package com.gmailreader.service;

import com.google.api.services.gmail.model.History;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GmailWatchService")
class GmailWatchServiceTest {

    @Mock
    private EmailProcessingService emailProcessingService;

    @Mock
    private GmailWatchManager gmailWatchManager;

    @Mock
    private GmailHistoryService gmailHistoryService;

    private GmailWatchService gmailWatchService;

    @BeforeEach
    void setUp() {
        gmailWatchService = new GmailWatchService(
                emailProcessingService,
                gmailWatchManager,
                gmailHistoryService
        );
    }

    @Test
    @DisplayName("Deve iniciar watch com sucesso")
    void deveIniciarWatchComSucesso() {
        assertDoesNotThrow(() -> gmailWatchService.iniciarWatch());
        verify(gmailWatchManager).iniciarWatch();
    }

    @Test
    @DisplayName("Deve processar emails quando há histórico")
    void deveProcessarEmailsQuandoHaHistorico() {
        BigInteger historyId = BigInteger.valueOf(12345);
        List<History> historyList = Arrays.asList(new History(), new History());

        when(gmailHistoryService.buscarHistorico(historyId)).thenReturn(historyList);

        gmailWatchService.processarEmailsDoHistorico(historyId);

        verify(gmailHistoryService).buscarHistorico(historyId);
        verify(emailProcessingService).processarEmails();
    }

    @Test
    @DisplayName("Deve processar emails quando não há histórico")
    void deveProcessarEmailsQuandoNaoHaHistorico() {
        BigInteger historyId = BigInteger.valueOf(67890);

        when(gmailHistoryService.buscarHistorico(historyId)).thenReturn(Collections.emptyList());

        gmailWatchService.processarEmailsDoHistorico(historyId);

        verify(gmailHistoryService).buscarHistorico(historyId);
        verify(emailProcessingService).processarEmails();
    }

    @Test
    @DisplayName("Deve capturar exceção ao processar histórico")
    void deveCapturaExcecaoAoProcessarHistorico() {
        BigInteger historyId = BigInteger.valueOf(99999);

        when(gmailHistoryService.buscarHistorico(historyId)).thenThrow(new RuntimeException("Erro no histórico"));

        assertDoesNotThrow(() -> gmailWatchService.processarEmailsDoHistorico(historyId));
        verify(gmailHistoryService).buscarHistorico(historyId);
    }

    @Test
    @DisplayName("Deve capturar exceção ao processar emails")
    void deveCapturaExcecaoAoProcessarEmails() {
        BigInteger historyId = BigInteger.valueOf(11111);

        when(gmailHistoryService.buscarHistorico(historyId)).thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("Erro ao processar")).when(emailProcessingService).processarEmails();

        assertDoesNotThrow(() -> gmailWatchService.processarEmailsDoHistorico(historyId));
        verify(emailProcessingService).processarEmails();
    }

    @Test
    @DisplayName("Deve capturar exceção ao iniciar watch no PostConstruct")
    void deveCapturaExcecaoAoIniciarWatchNoPostConstruct() {
        doThrow(new RuntimeException("Erro ao iniciar watch")).when(gmailWatchManager).iniciarWatch();

        assertDoesNotThrow(() -> gmailWatchService.iniciarWatchAoSubir());
        verify(gmailWatchManager).iniciarWatch();
    }

    @Test
    @DisplayName("Deve processar múltiplos historyIds sequencialmente")
    void deveProcessarMultiplosHistoryIdsSequencialmente() {
        BigInteger historyId1 = BigInteger.valueOf(100);
        BigInteger historyId2 = BigInteger.valueOf(200);
        BigInteger historyId3 = BigInteger.valueOf(300);

        when(gmailHistoryService.buscarHistorico(any())).thenReturn(Collections.emptyList());

        gmailWatchService.processarEmailsDoHistorico(historyId1);
        gmailWatchService.processarEmailsDoHistorico(historyId2);
        gmailWatchService.processarEmailsDoHistorico(historyId3);

        verify(gmailHistoryService, times(3)).buscarHistorico(any());
        verify(emailProcessingService, times(3)).processarEmails();
    }

    @Test
    @DisplayName("Deve processar historyId muito grande")
    void deveProcessarHistoryIdMuitoGrande() {
        BigInteger historyId = new BigInteger("999999999999999999999999999");

        when(gmailHistoryService.buscarHistorico(historyId)).thenReturn(Collections.emptyList());

        gmailWatchService.processarEmailsDoHistorico(historyId);

        verify(gmailHistoryService).buscarHistorico(historyId);
        verify(emailProcessingService).processarEmails();
    }

    @Test
    @DisplayName("Deve processar com lista de histórico vazia")
    void deveProcessarComListaDeHistoricoVazia() {
        BigInteger historyId = BigInteger.valueOf(55555);

        when(gmailHistoryService.buscarHistorico(historyId)).thenReturn(Collections.emptyList());

        gmailWatchService.processarEmailsDoHistorico(historyId);

        verify(emailProcessingService).processarEmails();
    }

    @Test
    @DisplayName("Deve processar com múltiplas alterações no histórico")
    void deveProcessarComMultiplasAlteracoesNoHistorico() {
        BigInteger historyId = BigInteger.valueOf(77777);
        List<History> historyList = Arrays.asList(
                new History(),
                new History(),
                new History(),
                new History(),
                new History()
        );

        when(gmailHistoryService.buscarHistorico(historyId)).thenReturn(historyList);

        gmailWatchService.processarEmailsDoHistorico(historyId);

        verify(gmailHistoryService).buscarHistorico(historyId);
        verify(emailProcessingService).processarEmails();
    }
}
