package com.gmailreader.service;

import com.gmailreader.exception.GmailReaderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("FileStorageService")
class FileStorageServiceTest {

    @Mock
    private EmailFileNamingService emailFileNamingService;

    @Mock
    private DirectoryManagerService directoryManagerService;

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileStorageService = new FileStorageService(emailFileNamingService, directoryManagerService);
        ReflectionTestUtils.setField(fileStorageService, "pastaDestino", "emails");
    }

    @Test
    @DisplayName("Deve salvar email com sucesso")
    void deveSalvarEmailComSucesso() throws IOException {
        String conteudo = "{\"id\":\"123\",\"subject\":\"Teste\"}";
        String nomeArquivo = "emails-2024-10-02.json";
        File arquivo = tempDir.resolve(nomeArquivo).toFile();

        when(emailFileNamingService.gerarNomeArquivo()).thenReturn(nomeArquivo);
        when(directoryManagerService.criarCaminhoArquivo("emails", nomeArquivo))
                .thenReturn(arquivo);

        assertDoesNotThrow(() -> fileStorageService.salvarEmail(conteudo));

        assertTrue(arquivo.exists(), "Arquivo deve ser criado");
        String conteudoSalvo = Files.readString(arquivo.toPath());
        assertTrue(conteudoSalvo.contains(conteudo), "Conteúdo deve estar no arquivo");

        verify(emailFileNamingService).gerarNomeArquivo();
        verify(directoryManagerService).criarCaminhoArquivo("emails", nomeArquivo);
    }

    @Test
    @DisplayName("Deve lançar exceção para conteúdo null")
    void deveLancarExcecaoParaConteudoNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.salvarEmail(null),
                "Deve lançar exceção para conteúdo null"
        );

        assertEquals("Conteúdo do email não pode ser null ou vazio", exception.getMessage());
        verifyNoInteractions(emailFileNamingService, directoryManagerService);
    }

    @Test
    @DisplayName("Deve lançar exceção para conteúdo vazio")
    void deveLancarExcecaoParaConteudoVazio() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.salvarEmail(""),
                "Deve lançar exceção para conteúdo vazio"
        );

        assertEquals("Conteúdo do email não pode ser null ou vazio", exception.getMessage());
        verifyNoInteractions(emailFileNamingService, directoryManagerService);
    }

    @Test
    @DisplayName("Deve lançar exceção para conteúdo apenas com espaços")
    void deveLancarExcecaoParaConteudoApenasComEspacos() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.salvarEmail("   \t\n   "),
                "Deve lançar exceção para conteúdo apenas com espaços"
        );

        assertEquals("Conteúdo do email não pode ser null ou vazio", exception.getMessage());
        verifyNoInteractions(emailFileNamingService, directoryManagerService);
    }

    @Test
    @DisplayName("Deve retornar localização de armazenamento")
    void deveRetornarLocalizacaoDeArmazenamento() {
        String localizacao = fileStorageService.obterLocalizacaoArmazenamento();

        assertNotNull(localizacao, "Localização não deve ser null");
        assertEquals("emails", localizacao, "Deve retornar pasta configurada");
    }

    @Test
    @DisplayName("Deve anexar conteúdo a arquivo existente")
    void deveAnexarConteudoAArquivoExistente() throws IOException {
        String conteudoExistente = "{\"id\":\"111\",\"subject\":\"Primeiro\"}";
        String novoConteudo = "{\"id\":\"222\",\"subject\":\"Segundo\"}";
        String nomeArquivo = "emails-2024-10-02.json";
        File arquivo = tempDir.resolve(nomeArquivo).toFile();

        Files.writeString(arquivo.toPath(), conteudoExistente + System.lineSeparator());

        when(emailFileNamingService.gerarNomeArquivo()).thenReturn(nomeArquivo);
        when(directoryManagerService.criarCaminhoArquivo("emails", nomeArquivo))
                .thenReturn(arquivo);

        fileStorageService.salvarEmail(novoConteudo);

        String conteudoCompleto = Files.readString(arquivo.toPath());
        assertTrue(conteudoCompleto.contains(conteudoExistente), "Deve manter conteúdo existente");
        assertTrue(conteudoCompleto.contains(novoConteudo), "Deve adicionar novo conteúdo");
    }

    @Test
    @DisplayName("Deve tratar erro de IOException")
    void deveTratarErroDeIOException() throws IOException {
        String conteudo = "{\"id\":\"123\",\"subject\":\"Teste\"}";
        String nomeArquivo = "emails-2024-10-02.json";

        when(emailFileNamingService.gerarNomeArquivo()).thenReturn(nomeArquivo);
        when(directoryManagerService.criarCaminhoArquivo("emails", nomeArquivo))
                .thenThrow(new IOException("Erro de I/O simulado"));

        GmailReaderException exception = assertThrows(
                GmailReaderException.class,
                () -> fileStorageService.salvarEmail(conteudo),
                "Deve lançar GmailReaderException para IOException"
        );

        assertEquals("Erro ao salvar arquivo de email", exception.getMessage());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    @DisplayName("Deve salvar múltiplos emails no mesmo arquivo")
    void deveSalvarMultiplosEmailsNoMesmoArquivo() throws IOException {
        String email1 = "{\"id\":\"001\",\"subject\":\"Primeiro email\"}";
        String email2 = "{\"id\":\"002\",\"subject\":\"Segundo email\"}";
        String nomeArquivo = "emails-2024-10-02.json";
        File arquivo = tempDir.resolve(nomeArquivo).toFile();

        when(emailFileNamingService.gerarNomeArquivo()).thenReturn(nomeArquivo);
        when(directoryManagerService.criarCaminhoArquivo("emails", nomeArquivo))
                .thenReturn(arquivo);

        fileStorageService.salvarEmail(email1);
        fileStorageService.salvarEmail(email2);

        String conteudoCompleto = Files.readString(arquivo.toPath());
        assertTrue(conteudoCompleto.contains("001"), "Deve conter primeiro email");
        assertTrue(conteudoCompleto.contains("002"), "Deve conter segundo email");

        long linhas = conteudoCompleto.lines().count();
        assertEquals(2, linhas, "Deve ter 2 linhas (uma para cada email)");
    }

    @Test
    @DisplayName("Deve validar conteúdo JSON válido")
    void deveValidarConteudoJsonValido() throws IOException {
        String conteudoJson = "{\"id\":\"456\",\"subject\":\"Email JSON\",\"body\":\"Conteúdo do email\"}";
        String nomeArquivo = "emails-2024-10-02.json";
        File arquivo = tempDir.resolve(nomeArquivo).toFile();

        when(emailFileNamingService.gerarNomeArquivo()).thenReturn(nomeArquivo);
        when(directoryManagerService.criarCaminhoArquivo("emails", nomeArquivo))
                .thenReturn(arquivo);

        assertDoesNotThrow(() -> fileStorageService.salvarEmail(conteudoJson));

        assertTrue(arquivo.exists(), "Arquivo deve ser criado");
        String conteudoSalvo = Files.readString(arquivo.toPath());
        assertTrue(conteudoSalvo.contains("456"), "Deve conter ID do email");
        assertTrue(conteudoSalvo.contains("Email JSON"), "Deve conter subject");
    }
}
