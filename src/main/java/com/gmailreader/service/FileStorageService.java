package com.gmailreader.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

@Service
public class FileStorageService implements StorageService {

    private final String pastaDestino = "emails";

    @Override
    public void salvarEmail(String conteudo) {
        try {
            LocalDate data = LocalDate.now();
            File pasta = new File(pastaDestino);
            if (!pasta.exists()) {
                pasta.mkdirs();
            }

            File arquivo = new File(pasta, "emails-" + data + ".json");

            try (FileWriter writer = new FileWriter(arquivo, true)) {
                writer.write(conteudo + System.lineSeparator());
            }

            System.out.println("Arquivo salvo: " + arquivo.getPath());

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo: " + e.getMessage(), e);
        }
    }

    @Override
    public String getStorageLocation() {
        return pastaDestino;
    }

}
