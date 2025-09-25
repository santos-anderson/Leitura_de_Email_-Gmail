package com.gmailreader.service;

public interface StorageService {

    void salvarEmail(String conteudo);

    String getStorageLocation();
}
