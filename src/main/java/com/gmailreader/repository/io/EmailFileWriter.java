package com.gmailreader.repository.io;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;

@Component
public class EmailFileWriter {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailFileWriter.class);
    private final Object lockEscrita = new Object();
    
    public void salvarEmailId(String caminhoPasta, String nomeArquivo, String emailId) {
        synchronized (lockEscrita) {
            try {
                File pasta = new File(caminhoPasta);
                if (!pasta.exists()) {
                    pasta.mkdirs();
                    logger.debug("Pasta criada: {}", caminhoPasta);
                }
                
                File arquivo = new File(pasta, nomeArquivo);
                
                try (FileWriter writer = new FileWriter(arquivo, true)) {
                    JSONObject json = new JSONObject();
                    json.put("id", emailId);
                    writer.write(json.toString() + "\n");
                    
                    logger.debug("Email ID salvo no arquivo {}: {}", nomeArquivo, emailId);
                }
            } catch (Exception e) {
                logger.error("Erro ao salvar email ID {}: {}", emailId, e.getMessage(), e);
            }
        }
    }
}
