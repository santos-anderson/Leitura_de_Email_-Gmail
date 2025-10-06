package com.gmailreader.repository.cache;

import java.util.Set;

public interface EmailCache {
    
    boolean contem(String emailId);
    
    boolean adicionar(String emailId);
    
    void carregar(Set<String> emailIds);
    
    int tamanho();
    
    void limpar();
    
    boolean estaCarregado();
}
