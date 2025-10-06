package com.gmailreader.repository.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryEmailCache implements EmailCache {
    
    private static final Logger logger = LoggerFactory.getLogger(InMemoryEmailCache.class);
    
    private final Set<String> indiceEmMemoria;
    private volatile boolean carregado = false;
    
    public InMemoryEmailCache() {
        this.indiceEmMemoria = ConcurrentHashMap.newKeySet();
    }
    
    @Override
    public boolean contem(String emailId) {
        return indiceEmMemoria.contains(emailId);
    }
    
    @Override
    public boolean adicionar(String emailId) {
        boolean adicionado = indiceEmMemoria.add(emailId);
        if (adicionado) {
            logger.debug("Email ID adicionado ao cache: {}", emailId);
        }
        return adicionado;
    }
    
    @Override
    public synchronized void carregar(Set<String> emailIds) {
        if (carregado) {
            logger.warn("Cache já foi carregado. Ignorando nova carga.");
            return;
        }
        
        indiceEmMemoria.clear();
        indiceEmMemoria.addAll(emailIds);
        carregado = true;
        
        logger.info("Cache carregado com {} emails processados", emailIds.size());
    }
    
    @Override
    public int tamanho() {
        return indiceEmMemoria.size();
    }
    
    @Override
    public void limpar() {
        indiceEmMemoria.clear();
        carregado = false;
        logger.info("Cache limpo");
    }
    
    @Override
    public boolean estaCarregado() {
        return carregado;
    }
}
