package com.gmailreader.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmailFileNamingService")
class EmailFileNamingServiceTest {

    private EmailFileNamingService emailFileNamingService;

    @BeforeEach
    void setUp() {
        emailFileNamingService = new EmailFileNamingService();
    }

    @Test
    @DisplayName("Deve gerar nome de arquivo com data atual")
    void deveGerarNomeArquivoComDataAtual() {

        String nomeArquivo = emailFileNamingService.gerarNomeArquivo();

        assertNotNull(nomeArquivo, "Nome do arquivo não deve ser null");
        assertTrue(nomeArquivo.startsWith("emails-"), "Nome deve começar com 'emails-'");
        assertTrue(nomeArquivo.endsWith(".json"), "Nome deve terminar com '.json'");

        String dataAtual = LocalDate.now().toString();
        assertTrue(nomeArquivo.contains(dataAtual), 
                "Nome deve conter a data atual: " + dataAtual);

        String nomeEsperado = "emails-" + dataAtual + ".json";
        assertEquals(nomeEsperado, nomeArquivo, 
                "Nome do arquivo deve seguir o padrão esperado");
    }

    @Test
    @DisplayName("Deve gerar nome de arquivo com data específica")
    void deveGerarNomeArquivoComDataEspecifica() {

        LocalDate dataEspecifica = LocalDate.of(2024, 12, 25);

        String nomeArquivo = emailFileNamingService.gerarNomeArquivo(dataEspecifica);

        assertNotNull(nomeArquivo, "Nome do arquivo não deve ser null");
        assertEquals("emails-2024-12-25.json", nomeArquivo, 
                "Nome deve seguir o formato correto com data específica");
    }

    @Test
    @DisplayName("Deve gerar nomes diferentes para datas diferentes")
    void deveGerarNomesDiferentesParaDatasDiferentes() {

        LocalDate data1 = LocalDate.of(2024, 1, 1);
        LocalDate data2 = LocalDate.of(2024, 12, 31);
        

        String nome1 = emailFileNamingService.gerarNomeArquivo(data1);
        String nome2 = emailFileNamingService.gerarNomeArquivo(data2);
        

        assertNotEquals(nome1, nome2, "Nomes devem ser diferentes para datas diferentes");
        assertEquals("emails-2024-01-01.json", nome1);
        assertEquals("emails-2024-12-31.json", nome2);
    }

    @Test
    @DisplayName("Deve gerar mesmo nome para mesma data")
    void deveGerarMesmoNomeParaMesmaData() {

        LocalDate data = LocalDate.of(2024, 6, 15);
        

        String nome1 = emailFileNamingService.gerarNomeArquivo(data);
        String nome2 = emailFileNamingService.gerarNomeArquivo(data);

        assertEquals(nome1, nome2, "Mesmo nome deve ser gerado para a mesma data");
        assertEquals("emails-2024-06-15.json", nome1);
    }

    @Test
    @DisplayName("Deve tratar anos bissextos corretamente")
    void deveTratarAnosBissextosCorretamente() {

        LocalDate dataAnoBissexto = LocalDate.of(2024, 2, 29);

        String nomeArquivo = emailFileNamingService.gerarNomeArquivo(dataAnoBissexto);

        assertEquals("emails-2024-02-29.json", nomeArquivo, 
                "Deve tratar corretamente 29 de fevereiro em ano bissexto");
    }

    @Test
    @DisplayName("Deve funcionar com datas muito antigas")
    void deveFuncionarComDatasMuitoAntigas() {

        LocalDate dataAntiga = LocalDate.of(1900, 1, 1);

        String nomeArquivo = emailFileNamingService.gerarNomeArquivo(dataAntiga);

        assertEquals("emails-1900-01-01.json", nomeArquivo, 
                "Deve funcionar com datas muito antigas");
    }

    @Test
    @DisplayName("Deve funcionar com datas futuras")
    void deveFuncionarComDatasFuturas() {

        LocalDate dataFutura = LocalDate.of(2030, 12, 31);

        String nomeArquivo = emailFileNamingService.gerarNomeArquivo(dataFutura);

        assertEquals("emails-2030-12-31.json", nomeArquivo, 
                "Deve funcionar com datas futuras");
    }
}
