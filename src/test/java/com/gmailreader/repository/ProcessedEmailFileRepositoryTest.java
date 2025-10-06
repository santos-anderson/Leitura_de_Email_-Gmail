package com.gmailreader.repository;

import com.gmailreader.repository.cache.InMemoryEmailCache;
import com.gmailreader.repository.io.EmailFileReader;
import com.gmailreader.repository.io.EmailFileWriter;
import com.gmailreader.repository.parser.JsonEmailParser;
import com.gmailreader.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessedEmailFileRepository")
class ProcessedEmailFileRepositoryTest {

    @Mock
    private StorageService storageService;

    private ProcessedEmailFileRepository repository;
    private InMemoryEmailCache emailCache;
    private EmailFileReader fileReader;
    private EmailFileWriter fileWriter;
    private JsonEmailParser jsonParser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        emailCache = new InMemoryEmailCache();
        fileReader = new EmailFileReader();
        fileWriter = new EmailFileWriter();
        jsonParser = new JsonEmailParser();
        
        repository = new ProcessedEmailFileRepository(
            storageService,
            emailCache,
            fileReader,
            fileWriter,
            jsonParser
        );
        
        when(storageService.obterLocalizacaoArmazenamento()).thenReturn(tempDir.toString());
    }

    @Test
    @DisplayName("Deve retornar false para email não processado")
    void deveRetornarFalseParaEmailNaoProcessado() {
        repository.inicializarIndice();

        boolean resultado = repository.emailJaProcessado("msg123");

        assertFalse(resultado);
    }

    @Test
    @DisplayName("Deve retornar true para email já processado")
    void deveRetornarTrueParaEmailJaProcessado() {
        repository.inicializarIndice();
        repository.marcarComoProcessado("msg456");

        boolean resultado = repository.emailJaProcessado("msg456");

        assertTrue(resultado);
    }

    @Test
    @DisplayName("Deve marcar email como processado")
    void deveMarcarEmailComoProcessado() {
        repository.inicializarIndice();

        repository.marcarComoProcessado("msg789");

        assertTrue(repository.emailJaProcessado("msg789"));
    }

    @Test
    @DisplayName("Não deve duplicar email já processado")
    void naoDeveDuplicarEmailJaProcessado() {
        repository.inicializarIndice();

        repository.marcarComoProcessado("msg111");
        repository.marcarComoProcessado("msg111");
        repository.marcarComoProcessado("msg111");

        assertTrue(repository.emailJaProcessado("msg111"));
    }

    @Test
    @DisplayName("Deve processar múltiplos emails diferentes")
    void deveProcessarMultiplosEmailsDiferentes() {
        repository.inicializarIndice();

        repository.marcarComoProcessado("msg1");
        repository.marcarComoProcessado("msg2");
        repository.marcarComoProcessado("msg3");

        assertTrue(repository.emailJaProcessado("msg1"));
        assertTrue(repository.emailJaProcessado("msg2"));
        assertTrue(repository.emailJaProcessado("msg3"));
    }

    @Test
    @DisplayName("Deve carregar índice de arquivo existente")
    void deveCarregarIndiceDeArquivoExistente() throws Exception {
        File arquivo = new File(tempDir.toFile(), "emails_processados.json");
        try (FileWriter writer = new FileWriter(arquivo)) {
            writer.write("{\"id\":\"msg-from-file-1\"}\n");
            writer.write("{\"id\":\"msg-from-file-2\"}\n");
        }

        repository.inicializarIndice();

        assertTrue(repository.emailJaProcessado("msg-from-file-1"));
        assertTrue(repository.emailJaProcessado("msg-from-file-2"));
    }

    @Test
    @DisplayName("Deve ignorar linhas vazias ao carregar índice")
    void deveIgnorarLinhasVaziasAoCarregarIndice() throws Exception {
        File arquivo = new File(tempDir.toFile(), "emails_processados.json");
        try (FileWriter writer = new FileWriter(arquivo)) {
            writer.write("{\"id\":\"msg1\"}\n");
            writer.write("\n");
            writer.write("   \n");
            writer.write("{\"id\":\"msg2\"}\n");
        }

        repository.inicializarIndice();

        assertTrue(repository.emailJaProcessado("msg1"));
        assertTrue(repository.emailJaProcessado("msg2"));
    }

    @Test
    @DisplayName("Deve lidar com arquivo JSON inválido")
    void deveLidarComArquivoJsonInvalido() throws Exception {
        File arquivo = new File(tempDir.toFile(), "emails_processados.json");
        try (FileWriter writer = new FileWriter(arquivo)) {
            writer.write("{\"id\":\"msg-valid\"}\n");
            writer.write("invalid json line\n");
            writer.write("{\"id\":\"msg-valid-2\"}\n");
        }

        assertDoesNotThrow(() -> repository.inicializarIndice());

        assertTrue(repository.emailJaProcessado("msg-valid"));
        assertTrue(repository.emailJaProcessado("msg-valid-2"));
    }

    @Test
    @DisplayName("Deve funcionar quando pasta não existe")
    void deveFuncionarQuandoPastaNaoExiste() {
        File pastaNaoExistente = new File(tempDir.toFile(), "pasta-inexistente");
        when(storageService.obterLocalizacaoArmazenamento()).thenReturn(pastaNaoExistente.getAbsolutePath());

        assertDoesNotThrow(() -> repository.inicializarIndice());
        assertFalse(repository.emailJaProcessado("msg999"));
    }

    @Test
    @DisplayName("Deve processar email com ID especial")
    void deveProcessarEmailComIdEspecial() {
        repository.inicializarIndice();
        String specialId = "msg-123_test@domain.com";

        repository.marcarComoProcessado(specialId);

        assertTrue(repository.emailJaProcessado(specialId));
    }

    @Test
    @DisplayName("Deve processar email com ID muito longo")
    void deveProcessarEmailComIdMuitoLongo() {
        repository.inicializarIndice();
        String longId = "a".repeat(500);

        repository.marcarComoProcessado(longId);

        assertTrue(repository.emailJaProcessado(longId));
    }

    @Test
    @DisplayName("Deve ser thread-safe para múltiplas marcações")
    void deveSerThreadSafeParaMultiplasMarcacoes() {
        repository.inicializarIndice();

        repository.marcarComoProcessado("msg-concurrent-1");
        repository.marcarComoProcessado("msg-concurrent-2");
        repository.marcarComoProcessado("msg-concurrent-3");

        assertTrue(repository.emailJaProcessado("msg-concurrent-1"));
        assertTrue(repository.emailJaProcessado("msg-concurrent-2"));
        assertTrue(repository.emailJaProcessado("msg-concurrent-3"));
    }
}
