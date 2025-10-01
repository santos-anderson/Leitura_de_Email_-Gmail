package com.gmailreader.config;

import com.gmailreader.service.processing.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class ProcessingChainConfiguration {

    @Bean
    public List<ProcessingStep> processingStepsOrder(
            CheckAlreadyProcessedStep checkStep,
            ConvertEmailStep convertStep,
            SaveEmailStep saveStep,
            MarkAsProcessedStep markProcessedStep,
            MarkAsReadStep markReadStep) {

        return List.of(
            checkStep,
            convertStep,
            saveStep,
            markProcessedStep,
            markReadStep
        );
    }

    @Bean
    public ProcessingStep processingChain(
            ProcessingChainFactory factory,
            List<ProcessingStep> processingStepsOrder) {
        
        return factory.criarCadeia(processingStepsOrder);
    }
}
