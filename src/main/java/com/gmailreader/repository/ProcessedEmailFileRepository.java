package com.gmailreader.repository;

import com.gmailreader.repository.cache.EmailCache;
import com.gmailreader.repository.io.EmailFileReader;
import com.gmailreader.repository.io.EmailFileWriter;
import com.gmailreader.repository.parser.JsonEmailParser;
import com.gmailreader.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.Set;

@Repository
public class ProcessedEmailFileRepository {

    private static final Logger logger = LoggerFactory.getLogger(ProcessedEmailFileRepository.class);
    private static final String ARQUIVO_PROCESSADOS = "emails_processados.json";

    private final StorageService storageService;
    private final EmailCache emailCache;
    private final EmailFileReader fileReader;
    private final EmailFileWriter fileWriter;
    private final JsonEmailParser jsonParser;

    public ProcessedEmailFileRepository(
            StorageService storageService,
            EmailCache emailCache,
            EmailFileReader fileReader,
            EmailFileWriter fileWriter,
            JsonEmailParser jsonParser) {
        this.storageService = storageService;
        this.emailCache = emailCache;
        this.fileReader = fileReader;
        this.fileWriter = fileWriter;
        this.jsonParser = jsonParser;
    }

    @PostConstruct
    public void inicializarIndice() {
        carregarIndiceEmMemoria();
    }

    public boolean emailJaProcessado(String emailId) {
        garantirIndiceCarregado();
        return emailCache.contem(emailId);
    }

    public void marcarComoProcessado(String emailId) {
        garantirIndiceCarregado();
        
        if (emailCache.adicionar(emailId)) {
            String caminhoPasta = storageService.obterLocalizacaoArmazenamento();
            fileWriter.salvarEmailId(caminhoPasta, ARQUIVO_PROCESSADOS, emailId);
        }
    }

    private void garantirIndiceCarregado() {
        if (!emailCache.estaCarregado()) {
            carregarIndiceEmMemoria();
        }
    }

    private synchronized void carregarIndiceEmMemoria() {
        if (emailCache.estaCarregado()) {
            return;
        }

        String caminhoPasta = storageService.obterLocalizacaoArmazenamento();
        List<File> arquivos = fileReader.listarArquivosJson(caminhoPasta);
        
        Set<String> todosIds = jsonParser.extrairIdsDeArquivos(arquivos, fileReader::lerLinhasDoArquivo);
        
        emailCache.carregar(todosIds);
        
        logger.info("Repositório inicializado com {} emails processados", emailCache.tamanho());
    }
}
