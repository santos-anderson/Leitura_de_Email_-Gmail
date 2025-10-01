package com.gmailreader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class DirectoryManagerService {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryManagerService.class);

    public Path garantirDiretorioExiste(String diretorio) throws IOException {
        Path path = Paths.get(diretorio);
        return garantirDiretorioExiste(path);
    }

    public Path garantirDiretorioExiste(Path path) throws IOException {
        if (!Files.exists(path)) {
            logger.info("Criando diretório: {}", path.toAbsolutePath());
            Files.createDirectories(path);
            logger.info("Diretório criado com sucesso: {}", path.toAbsolutePath());
        } else {
            logger.debug("Diretório já existe: {}", path.toAbsolutePath());
        }
        return path;
    }

    public File criarCaminhoArquivo(String diretorio, String nomeArquivo) throws IOException {
        Path diretorioPath = garantirDiretorioExiste(diretorio);
        return new File(diretorioPath.toFile(), nomeArquivo);
    }
}
