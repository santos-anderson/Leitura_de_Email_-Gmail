package com.gmailreader.service;

import com.gmailreader.exception.GmailReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


@Service
public class FileStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    @Value("${gmail.storage.directory:emails}")
    private String pastaDestino;

    private final EmailFileNamingService fileNamingService;
    private final DirectoryManagerService directoryManagerService;

    public FileStorageService(EmailFileNamingService fileNamingService,
                              DirectoryManagerService directoryManagerService) {
        this.fileNamingService = fileNamingService;
        this.directoryManagerService = directoryManagerService;
    }

    @Override
    public void salvarEmail(String conteudo) {
        logger.debug("Iniciando salvamento de email");
        
        validarConteudo(conteudo);
        
        try {

            String nomeArquivo = fileNamingService.gerarNomeArquivo();
            logger.debug("Nome do arquivo gerado: {}", nomeArquivo);

            File arquivo = directoryManagerService.criarCaminhoArquivo(pastaDestino, nomeArquivo);
            logger.debug("Caminho do arquivo: {}", arquivo.getAbsolutePath());

            boolean arquivoExistia = arquivo.exists();

            escreverConteudoNoArquivo(arquivo, conteudo);
            
            String acao = arquivoExistia ? "atualizado" : "criado";
            logger.info("Email {} com sucesso: {} (tamanho: {} bytes)", acao, arquivo.getPath(), conteudo.length());

        } catch (IOException e) {
            logger.error("Erro de I/O ao salvar email: {}", e.getMessage(), e);
            throw new GmailReaderException("Erro ao salvar arquivo de email", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao salvar email: {}", e.getMessage(), e);
            throw new GmailReaderException("Erro inesperado durante o salvamento", e);
        }
    }

    @Override
    public String obterLocalizacaoArmazenamento() {
        return pastaDestino;
    }

    private void validarConteudo(String conteudo) {
        if (!StringUtils.hasText(conteudo)) {
            throw new IllegalArgumentException("Conteúdo do email não pode ser null ou vazio");
        }
        
        if (conteudo.trim().isEmpty()) {
            throw new IllegalArgumentException("Conteúdo do email não pode conter apenas espaços em branco");
        }
        
        logger.debug("Conteúdo validado (tamanho: {} caracteres)", conteudo.length());
    }

    private void escreverConteudoNoArquivo(File arquivo, String conteudo) throws IOException {
        if (arquivo == null) {
            throw new IllegalArgumentException("Arquivo não pode ser null");
        }
        
        logger.debug("Escrevendo conteúdo no arquivo: {}", arquivo.getAbsolutePath());
        
        try (FileWriter writer = new FileWriter(arquivo, true)) {
            writer.write(conteudo + System.lineSeparator());
            writer.flush();
            
            logger.debug("Conteúdo escrito com sucesso");
        }
    }
}
