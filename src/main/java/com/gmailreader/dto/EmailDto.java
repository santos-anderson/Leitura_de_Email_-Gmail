package com.gmailreader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
