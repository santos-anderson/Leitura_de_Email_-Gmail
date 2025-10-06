package com.gmailreader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDto {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("data")
    private String data;
    
    @JsonProperty("remetente")
    private String remetente;
    
    @JsonProperty("assunto")
    private String assunto;
    
    @JsonProperty("corpo")
    private String corpo;
    
    @JsonProperty("temAnexo")
    private boolean temAnexo;
    
    @JsonProperty("anexos")
    private List<String> anexos;
    
    public EmailDto(String id, String data, String remetente, String assunto, String corpo) {
        this.id = id;
        this.data = data;
        this.remetente = remetente;
        this.assunto = assunto;
        this.corpo = corpo;
        this.temAnexo = false;
        this.anexos = List.of();
    }
}
