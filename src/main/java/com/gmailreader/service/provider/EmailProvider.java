package com.gmailreader.service.provider;

import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.util.List;


public interface EmailProvider {

    List<Message> listarEmails(EmailSearchCriteria criteria) throws IOException;

    Message obterMensagemCompleta(String messageId, String userId) throws IOException;

    boolean isDisponivel();

    String getNomeProvedor();
}
