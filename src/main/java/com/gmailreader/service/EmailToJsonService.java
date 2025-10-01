package com.gmailreader.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmailreader.dto.EmailDto;
import com.gmailreader.exception.GmailReaderException;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailToJsonService {

    private final ObjectMapper objectMapper;

    public EmailToJsonService() {
        this.objectMapper = new ObjectMapper();
    }

    public String converter(Message message) {
        try {
            if (message.getPayload() == null) {
                return objectMapper.writeValueAsString(new EmailDto(message.getId(), "", "", "", ""));
            }

            String remetente = extrairHeader(message.getPayload().getHeaders(), "From");
            String assunto = extrairHeader(message.getPayload().getHeaders(), "Subject");
            String data = formatarData(extrairHeader(message.getPayload().getHeaders(), "Date"));
            String corpo = extrairCorpo(message.getPayload());

            EmailDto emailDto = new EmailDto(message.getId(), data, remetente, assunto, corpo);
            return objectMapper.writeValueAsString(emailDto);
        } catch (JsonProcessingException e) {
            throw new GmailReaderException("Erro ao serializar email para JSON: " + e.getMessage(), e);
        }
    }

    private String extrairHeader(List<MessagePartHeader> headers, String nomeHeader) {
        if (headers == null) return "";
        return headers.stream()
                .filter(header -> nomeHeader.equals(header.getName()))
                .findFirst()
                .map(MessagePartHeader::getValue)
                .orElse("");
    }

    private String extrairCorpo(MessagePart payload) {
        String corpo = "";
        
        if (payload.getBody() != null && payload.getBody().getData() != null) {
            corpo = decodificarBase64(payload.getBody().getData());
        } else if (payload.getParts() != null) {
            corpo = payload.getParts().stream()
                    .filter(part -> "text/plain".equals(part.getMimeType()))
                    .filter(part -> part.getBody() != null && part.getBody().getData() != null)
                    .findFirst()
                    .map(part -> decodificarBase64(part.getBody().getData()))
                    .orElse("");
        }
        
        return corpo.replaceAll("\\r\\n|\\r|\\n", " ").trim();
    }

    private String decodificarBase64(String data) {
        return new String(Base64.getUrlDecoder().decode(data));
    }

    private String formatarData(String dataOriginal) {
        if (dataOriginal == null || dataOriginal.isEmpty()) return "";
        
        try {
            String dataSemTimezone = dataOriginal.replaceAll(" [+-]\\d{4}$", "");
            DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
            DateTimeFormatter formatoSaida = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            
            return LocalDateTime.parse(dataSemTimezone, formatoEntrada).format(formatoSaida) + "hs";
        } catch (Exception e) {
            return dataOriginal.replaceAll(" [+-]\\d{4}$", "");
        }
    }
    
}

