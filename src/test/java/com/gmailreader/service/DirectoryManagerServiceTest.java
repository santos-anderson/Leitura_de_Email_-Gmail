package com.gmailreader.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DirectoryManagerService")
class DirectoryManagerServiceTest {

    private DirectoryManagerService directoryManagerService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        directoryManagerService = new DirectoryManagerService();
    }

    @Test
    @DisplayName("Deve criar diretório quando não existe")
    void deveCriarDiretorioQuandoNaoExiste() throws IOException {
        Path novoDiretorio = tempDir.resolve("novo-diretorio");
        assertFalse(Files.exists(novoDiretorio), "Diretório não deve existir inicialmente");

        Path resultado = directoryManagerService.garantirDiretorioExiste(novoDiretorio);

        assertTrue(Files.exists(resultado), "Diretório deve ser criado");
        assertTrue(Files.isDirectory(resultado), "Deve ser um diretório");
        assertEquals(novoDiretorio, resultado, "Deve retornar o mesmo path");
    }

    @Test
    @DisplayName("Deve retornar diretório existente sem modificar")
    void deveRetornarDiretorioExistenteSemModificar() throws IOException {
        Path diretorioExistente = tempDir.resolve("existente");
        Files.createDirectory(diretorioExistente);
        assertTrue(Files.exists(diretorioExistente), "Diretório deve existir");

        Path resultado = directoryManagerService.garantirDiretorioExiste(diretorioExistente);

        assertTrue(Files.exists(resultado), "Diretório deve continuar existindo");
        assertEquals(diretorioExistente, resultado, "Deve retornar o mesmo path");
    }

    @Test
    @DisplayName("Deve criar diretórios aninhados")
    void deveCriarDiretoriosAninhados() throws IOException {
        Path diretoriosAninhados = tempDir.resolve("nivel1/nivel2/nivel3");
        assertFalse(Files.exists(diretoriosAninhados), "Diretórios aninhados não devem existir");

        Path resultado = directoryManagerService.garantirDiretorioExiste(diretoriosAninhados);

        assertTrue(Files.exists(resultado), "Diretórios aninhados devem ser criados");
        assertTrue(Files.isDirectory(resultado), "Deve ser um diretório");
        assertTrue(Files.exists(tempDir.resolve("nivel1")), "Nível 1 deve existir");
        assertTrue(Files.exists(tempDir.resolve("nivel1/nivel2")), "Nível 2 deve existir");
    }

    @Test
    @DisplayName("Deve criar diretório usando string")
    void deveCriarDiretorioUsandoString() throws IOException {
        String caminhoString = tempDir.resolve("diretorio-string").toString();

        Path resultado = directoryManagerService.garantirDiretorioExiste(caminhoString);

        assertTrue(Files.exists(resultado), "Diretório deve ser criado");
        assertTrue(Files.isDirectory(resultado), "Deve ser um diretório");
        assertEquals(Paths.get(caminhoString), resultado, "Deve retornar path correto");
    }

    @Test
    @DisplayName("Deve criar caminho de arquivo com diretório e nome")
    void deveCriarCaminhoArquivoComDiretorioENome() throws IOException {
        String diretorio = tempDir.resolve("arquivos").toString();
        String nomeArquivo = "teste.txt";

        File arquivo = directoryManagerService.criarCaminhoArquivo(diretorio, nomeArquivo);

        assertNotNull(arquivo, "Arquivo não deve ser null");
        assertTrue(Files.exists(arquivo.getParentFile().toPath()), "Diretório pai deve existir");
        assertEquals(nomeArquivo, arquivo.getName(), "Nome do arquivo deve estar correto");
        assertTrue(arquivo.getAbsolutePath().contains(diretorio), "Caminho deve conter diretório");
    }

    @Test
    @DisplayName("Deve criar arquivo em diretório existente")
    void deveCriarArquivoEmDiretorioExistente() throws IOException {
        Path diretorioExistente = tempDir.resolve("existente");
        Files.createDirectory(diretorioExistente);
        String nomeArquivo = "arquivo-existente.json";

        File arquivo = directoryManagerService.criarCaminhoArquivo(
                diretorioExistente.toString(), nomeArquivo);

        assertNotNull(arquivo, "Arquivo não deve ser null");
        assertEquals(nomeArquivo, arquivo.getName(), "Nome deve estar correto");
        assertEquals(diretorioExistente.toFile(), arquivo.getParentFile(), 
                "Diretório pai deve ser o existente");
    }

    @Test
    @DisplayName("Deve tratar diretórios com espaços no nome")
    void deveTratarDiretoriosComEspacosNoNome() throws IOException {
        Path diretorioComEspacos = tempDir.resolve("diretório com espaços");

        Path resultado = directoryManagerService.garantirDiretorioExiste(diretorioComEspacos);

        assertTrue(Files.exists(resultado), "Diretório com espaços deve ser criado");
        assertTrue(Files.isDirectory(resultado), "Deve ser um diretório");
        assertTrue(resultado.toString().contains("espaços"), "Nome deve conter espaços");
    }

    @Test
    @DisplayName("Deve tratar caracteres especiais no nome")
    void deveTratarCaracteresEspeciaisNoNome() throws IOException {
        Path diretorioEspecial = tempDir.resolve("dir-test_123");

        Path resultado = directoryManagerService.garantirDiretorioExiste(diretorioEspecial);

        assertTrue(Files.exists(resultado), "Diretório com caracteres especiais deve ser criado");
        assertTrue(Files.isDirectory(resultado), "Deve ser um diretório");
    }

    @Test
    @DisplayName("Deve criar múltiplos diretórios independentes")
    void deveCriarMultiplosDiretoriosIndependentes() throws IOException {
        Path dir1 = tempDir.resolve("diretorio1");
        Path dir2 = tempDir.resolve("diretorio2");
        Path dir3 = tempDir.resolve("diretorio3");

        Path resultado1 = directoryManagerService.garantirDiretorioExiste(dir1);
        Path resultado2 = directoryManagerService.garantirDiretorioExiste(dir2);
        Path resultado3 = directoryManagerService.garantirDiretorioExiste(dir3);

        assertTrue(Files.exists(resultado1), "Diretório 1 deve existir");
        assertTrue(Files.exists(resultado2), "Diretório 2 deve existir");
        assertTrue(Files.exists(resultado3), "Diretório 3 deve existir");
        
        assertNotEquals(resultado1, resultado2, "Diretórios devem ser diferentes");
        assertNotEquals(resultado2, resultado3, "Diretórios devem ser diferentes");
    }

    @Test
    @DisplayName("Deve criar arquivo com extensões diferentes")
    void deveCriarArquivoComExtensoesDiferentes() throws IOException {
        String diretorio = tempDir.resolve("arquivos").toString();

        File arquivoJson = directoryManagerService.criarCaminhoArquivo(diretorio, "teste.json");
        File arquivoTxt = directoryManagerService.criarCaminhoArquivo(diretorio, "teste.txt");
        File arquivoXml = directoryManagerService.criarCaminhoArquivo(diretorio, "teste.xml");

        assertTrue(arquivoJson.getName().endsWith(".json"), "Deve ter extensão .json");
        assertTrue(arquivoTxt.getName().endsWith(".txt"), "Deve ter extensão .txt");
        assertTrue(arquivoXml.getName().endsWith(".xml"), "Deve ter extensão .xml");
        
        assertEquals(arquivoJson.getParentFile(), arquivoTxt.getParentFile(), 
                "Devem estar no mesmo diretório");
    }

    @Test
    @DisplayName("Deve tratar arquivo sem extensão")
    void deveTratarArquivoSemExtensao() throws IOException {
        String diretorio = tempDir.resolve("arquivos").toString();
        String nomeArquivo = "arquivo-sem-extensao";

        File arquivo = directoryManagerService.criarCaminhoArquivo(diretorio, nomeArquivo);

        assertNotNull(arquivo, "Arquivo não deve ser null");
        assertEquals(nomeArquivo, arquivo.getName(), "Nome deve estar correto");
        assertFalse(arquivo.getName().contains("."), "Não deve conter ponto");
    }
}
