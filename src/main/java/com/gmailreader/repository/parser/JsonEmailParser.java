package com.gmailreader.repository.parser;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class JsonEmailParser {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonEmailParser.class);
    
    public Set<String> extrairIdsDeLinhas(List<String> linhas, String nomeArquivo) {
        Set<String> ids = new HashSet<>();
        
        for (String linha : linhas) {
            try {
                JSONObject json = new JSONObject(linha);
                if (json.has("id")) {
                    ids.add(json.getString("id"));
                }
            } catch (Exception e) {
                logger.warn("Erro ao processar linha do arquivo {}: {}", nomeArquivo, e.getMessage());
            }
        }
        
        logger.debug("Extraídos {} IDs do arquivo: {}", ids.size(), nomeArquivo);
        return ids;
    }
    
    public Set<String> extrairIdsDeArquivos(List<File> arquivos, EmailFileReaderAdapter fileReader) {
        Set<String> todosIds = new HashSet<>();
        
        for (File arquivo : arquivos) {
            List<String> linhas = fileReader.lerLinhas(arquivo);
            Set<String> ids = extrairIdsDeLinhas(linhas, arquivo.getName());
            todosIds.addAll(ids);
        }
        
        logger.info("Total de {} IDs extraídos de {} arquivos", todosIds.size(), arquivos.size());
        return todosIds;
    }
    
    @FunctionalInterface
    public interface EmailFileReaderAdapter {
        List<String> lerLinhas(File arquivo);
    }
}
