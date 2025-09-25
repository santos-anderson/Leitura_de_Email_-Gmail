package com.gmailreader.repository;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;
import com.gmailreader.service.StorageService;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ProcessedEmailFileRepository {

    private final StorageService storageService;
    private final Set<String> indiceEmMemoria;
    private volatile boolean indiceCarregado = false;

    public ProcessedEmailFileRepository(StorageService storageService) {
        this.storageService = storageService;
        this.indiceEmMemoria = ConcurrentHashMap.newKeySet();
    }

    @PostConstruct
    public void inicializarIndice() {
        carregarIndiceEmMemoria();
    }



    public boolean emailJaProcessado(String emailId) {
        garantirIndiceCarregado();
        return indiceEmMemoria.contains(emailId);
    }

    public void marcarComoProcessado(String emailId) {
        garantirIndiceCarregado();
        indiceEmMemoria.add(emailId);
    }

    private synchronized void carregarIndiceEmMemoria() {
        if (indiceCarregado) {
            return;
        }

        indiceEmMemoria.clear();
        File pasta = new File(storageService.getStorageLocation());
        
        if (!pasta.exists()) {
            indiceCarregado = true;
            return;
        }

        File[] arquivos = pasta.listFiles((dir, nome) -> nome.endsWith(".json"));
        if (arquivos == null) {
            indiceCarregado = true;
            return;
        }

        for (File arquivo : arquivos) {
            indiceEmMemoria.addAll(extrairIdsDoArquivo(arquivo));
        }
        
        indiceCarregado = true;
        System.out.println("Índice carregado em memória: " + indiceEmMemoria.size() + " emails processados");
    }

    private void garantirIndiceCarregado() {
        if (!indiceCarregado) {
            carregarIndiceEmMemoria();
        }
    }


    private Set<String> extrairIdsDoArquivo(File arquivo) {
        Set<String> ids = new HashSet<>();
        
        try (Scanner scanner = new Scanner(arquivo)) {
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine();
                if (!linha.trim().isEmpty()) {
                    try {
                        JSONObject json = new JSONObject(linha);
                        if (json.has("id")) {
                            ids.add(json.getString("id"));
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao processar linha do arquivo " + arquivo.getName() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler arquivo " + arquivo.getName() + ": " + e.getMessage());
        }
        
        return ids;
    }
}
