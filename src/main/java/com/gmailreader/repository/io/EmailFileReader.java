package com.gmailreader.repository.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class EmailFileReader {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailFileReader.class);
    
    public List<String> lerLinhasDoArquivo(File arquivo) {
        List<String> linhas = new ArrayList<>();
        
        try (Scanner scanner = new Scanner(arquivo)) {
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine();
                if (!linha.trim().isEmpty()) {
                    linhas.add(linha);
                }
            }
            logger.debug("Lidas {} linhas do arquivo: {}", linhas.size(), arquivo.getName());
        } catch (Exception e) {
            logger.error("Erro ao ler arquivo {}: {}", arquivo.getName(), e.getMessage(), e);
        }
        
        return linhas;
    }
    
    public List<File> listarArquivosJson(String caminhoPasta) {
        List<File> arquivos = new ArrayList<>();
        
        File pasta = new File(caminhoPasta);
        if (!pasta.exists()) {
            logger.warn("Pasta não existe: {}", caminhoPasta);
            return arquivos;
        }
        
        File[] arquivosEncontrados = pasta.listFiles((dir, nome) -> nome.endsWith(".json"));
        if (arquivosEncontrados != null) {
            arquivos = List.of(arquivosEncontrados);
            logger.debug("Encontrados {} arquivos JSON em: {}", arquivos.size(), caminhoPasta);
        }
        
        return arquivos;
    }
}
