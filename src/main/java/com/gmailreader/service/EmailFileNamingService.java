package com.gmailreader.service;

import com.gmailreader.constants.GmailConstants;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
public class EmailFileNamingService {

    private static final String FILE_PREFIX = GmailConstants.Storage.EMAIL_FILE_PREFIX;
    private static final String FILE_EXTENSION = GmailConstants.Storage.EMAIL_FILE_EXTENSION;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public String gerarNomeArquivo() {
        return gerarNomeArquivo(LocalDate.now());
    }

    public String gerarNomeArquivo(LocalDate data) {
        return FILE_PREFIX + data.format(DATE_FORMATTER) + FILE_EXTENSION;
    }
}
