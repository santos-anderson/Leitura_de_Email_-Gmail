package com.gmailreader.config;

import com.gmailreader.exception.GmailReaderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PubSubCredentialsManager")
class PubSubCredentialsManagerTest {

    private PubSubCredentialsManager credentialsManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        credentialsManager = new PubSubCredentialsManager();
    }

    @Test
    @DisplayName("Deve lançar exceção quando arquivo de credenciais não existe")
    void deveLancarExcecaoQuandoArquivoDeCredenciaisNaoExiste() {
        String caminhoInexistente = tempDir.resolve("credenciais-inexistentes.json").toString();
        ReflectionTestUtils.setField(credentialsManager, "credentialsPath", caminhoInexistente);

        GmailReaderException exception = assertThrows(GmailReaderException.class,
                () -> credentialsManager.obterCredenciais());

        assertEquals("Falha ao carregar credenciais do Pub/Sub", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve lançar exceção quando arquivo JSON é inválido")
    void deveLancarExcecaoQuandoArquivoJsonEInvalido() throws Exception {
        File arquivoInvalido = tempDir.resolve("credenciais-invalidas.json").toFile();
        try (FileWriter writer = new FileWriter(arquivoInvalido)) {
            writer.write("{ invalid json content }");
        }

        ReflectionTestUtils.setField(credentialsManager, "credentialsPath", arquivoInvalido.getAbsolutePath());

        GmailReaderException exception = assertThrows(GmailReaderException.class,
                () -> credentialsManager.obterCredenciais());

        assertEquals("Falha ao carregar credenciais do Pub/Sub", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando caminho é vazio")
    void deveLancarExcecaoQuandoCaminhoEVazio() {
        ReflectionTestUtils.setField(credentialsManager, "credentialsPath", "");

        GmailReaderException exception = assertThrows(GmailReaderException.class,
                () -> credentialsManager.obterCredenciais());

        assertEquals("Falha ao carregar credenciais do Pub/Sub", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando caminho é null")
    void deveLancarExcecaoQuandoCaminhoENull() {
        ReflectionTestUtils.setField(credentialsManager, "credentialsPath", null);

        assertThrows(GmailReaderException.class,
                () -> credentialsManager.obterCredenciais());
    }

    @Test
    @DisplayName("Deve lançar exceção com arquivo vazio")
    void deveLancarExcecaoComArquivoVazio() throws Exception {
        File arquivoVazio = tempDir.resolve("credenciais-vazias.json").toFile();
        arquivoVazio.createNewFile();

        ReflectionTestUtils.setField(credentialsManager, "credentialsPath", arquivoVazio.getAbsolutePath());

        GmailReaderException exception = assertThrows(GmailReaderException.class,
                () -> credentialsManager.obterCredenciais());

        assertEquals("Falha ao carregar credenciais do Pub/Sub", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção com JSON sem campos obrigatórios")
    void deveLancarExcecaoComJsonSemCamposObrigatorios() throws Exception {
        File arquivoIncompleto = tempDir.resolve("credenciais-incompletas.json").toFile();
        try (FileWriter writer = new FileWriter(arquivoIncompleto)) {
            writer.write("{\"type\":\"service_account\"}");
        }

        ReflectionTestUtils.setField(credentialsManager, "credentialsPath", arquivoIncompleto.getAbsolutePath());

        GmailReaderException exception = assertThrows(GmailReaderException.class,
                () -> credentialsManager.obterCredenciais());

        assertEquals("Falha ao carregar credenciais do Pub/Sub", exception.getMessage());
    }
}
