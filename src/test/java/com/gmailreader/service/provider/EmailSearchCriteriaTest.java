package com.gmailreader.service.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmailSearchCriteria")
class EmailSearchCriteriaTest {

    @Test
    @DisplayName("Deve criar critério com valores padrão")
    void deveCriarCriterioComValoresPadrao() {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder().build();

        assertEquals("me", criteria.getUserId(), "UserId padrão deve ser 'me'");
        assertEquals(List.of("INBOX"), criteria.getLabelIds(), "Label padrão deve ser INBOX");
        assertEquals(List.of(), criteria.getIdsExcluidos(), "IdsExcluidos padrão deve ser lista vazia");
        assertNull(criteria.getMaxResults(), "MaxResults padrão deve ser null");
        assertNull(criteria.getQuery(), "Query padrão deve ser null");
    }

    @Test
    @DisplayName("Deve criar critério com userId customizado")
    void deveCriarCriterioComUserIdCustomizado() {
        String userIdCustomizado = "usuario@teste.com";

        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId(userIdCustomizado)
                .build();

        assertEquals(userIdCustomizado, criteria.getUserId(), "UserId deve ser customizado");
        assertEquals(List.of("INBOX"), criteria.getLabelIds(), "Outros valores devem manter padrão");
    }

    @Test
    @DisplayName("Deve criar critério com labels customizados")
    void deveCriarCriterioComLabelsCustomizados() {
        List<String> labelsCustomizados = List.of("SENT", "DRAFT", "IMPORTANT");

        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .labelIds(labelsCustomizados)
                .build();

        assertEquals(labelsCustomizados, criteria.getLabelIds(), "Labels devem ser customizados");
        assertEquals("me", criteria.getUserId(), "Outros valores devem manter padrão");
    }

    @Test
    @DisplayName("Deve criar critério com IDs excluídos")
    void deveCriarCriterioComIdsExcluidos() {
        List<String> idsExcluidos = List.of("123", "456", "789");

        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .idsExcluidos(idsExcluidos)
                .build();

        assertEquals(idsExcluidos, criteria.getIdsExcluidos(), "IdsExcluidos devem ser definidos");
        assertEquals(List.of("INBOX"), criteria.getLabelIds(), "Outros valores devem manter padrão");
    }

    @Test
    @DisplayName("Deve criar critério com maxResults")
    void deveCriarCriterioComMaxResults() {
        Integer maxResults = 50;

        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .maxResults(maxResults)
                .build();

        assertEquals(maxResults, criteria.getMaxResults(), "MaxResults deve ser definido");
        assertNull(criteria.getQuery(), "Outros valores opcionais devem manter padrão");
    }

    @Test
    @DisplayName("Deve criar critério com query de busca")
    void deveCriarCriterioComQueryDeBusca() {
        String query = "subject:importante from:chefe@empresa.com";

        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .query(query)
                .build();

        assertEquals(query, criteria.getQuery(), "Query deve ser definida");
        assertNull(criteria.getMaxResults(), "Outros valores opcionais devem manter padrão");
    }

    @Test
    @DisplayName("Deve criar critério completo com todos os parâmetros")
    void deveCriarCriterioCompletoComTodosParametros() {
        String userId = "admin@teste.com";
        List<String> labelIds = List.of("INBOX", "IMPORTANT");
        List<String> idsExcluidos = List.of("111", "222");
        Integer maxResults = 100;
        String query = "has:attachment";

        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId(userId)
                .labelIds(labelIds)
                .idsExcluidos(idsExcluidos)
                .maxResults(maxResults)
                .query(query)
                .build();

        assertEquals(userId, criteria.getUserId(), "UserId deve estar correto");
        assertEquals(labelIds, criteria.getLabelIds(), "LabelIds devem estar corretos");
        assertEquals(idsExcluidos, criteria.getIdsExcluidos(), "IdsExcluidos devem estar corretos");
        assertEquals(maxResults, criteria.getMaxResults(), "MaxResults deve estar correto");
        assertEquals(query, criteria.getQuery(), "Query deve estar correta");
    }

    @Test
    @DisplayName("Deve permitir fluent interface")
    void devePermitirFluentInterface() {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("teste@email.com")
                .labelIds(List.of("SENT"))
                .maxResults(25)
                .query("is:unread")
                .idsExcluidos(List.of("999"))
                .build();

        assertNotNull(criteria, "Critério deve ser criado");
        assertEquals("teste@email.com", criteria.getUserId());
        assertEquals(List.of("SENT"), criteria.getLabelIds());
        assertEquals(25, criteria.getMaxResults());
        assertEquals("is:unread", criteria.getQuery());
        assertEquals(List.of("999"), criteria.getIdsExcluidos());
    }

    @Test
    @DisplayName("Deve criar múltiplos critérios independentes")
    void deveCriarMultiplosCriteriosIndependentes() {
        EmailSearchCriteria criteria1 = EmailSearchCriteria.builder()
                .userId("user1@test.com")
                .maxResults(10)
                .build();

        EmailSearchCriteria criteria2 = EmailSearchCriteria.builder()
                .userId("user2@test.com")
                .maxResults(20)
                .build();

        assertNotEquals(criteria1.getUserId(), criteria2.getUserId(), "UserIds devem ser diferentes");
        assertNotEquals(criteria1.getMaxResults(), criteria2.getMaxResults(), "MaxResults devem ser diferentes");
    }

    @Test
    @DisplayName("Deve tratar lista vazia de labels")
    void deveTratarListaVaziaDeLabels() {
        List<String> labelsVazios = List.of();

        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .labelIds(labelsVazios)
                .build();

        assertEquals(labelsVazios, criteria.getLabelIds(), "Deve aceitar lista vazia de labels");
        assertTrue(criteria.getLabelIds().isEmpty(), "Lista deve estar vazia");
    }

    @Test
    @DisplayName("Deve tratar maxResults zero")
    void deveTratarMaxResultsZero() {
        Integer maxResultsZero = 0;

        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .maxResults(maxResultsZero)
                .build();

        assertEquals(maxResultsZero, criteria.getMaxResults(), "Deve aceitar maxResults zero");
    }

    @Test
    @DisplayName("Deve tratar query vazia")
    void deveTratarQueryVazia() {
        String queryVazia = "";

        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .query(queryVazia)
                .build();

        assertEquals(queryVazia, criteria.getQuery(), "Deve aceitar query vazia");
        assertTrue(criteria.getQuery().isEmpty(), "Query deve estar vazia");
    }

    @Test
    @DisplayName("Deve ter toString informativo")
    void deveTerToStringInformativo() {
        EmailSearchCriteria criteria = EmailSearchCriteria.builder()
                .userId("test@example.com")
                .maxResults(50)
                .build();

        String toString = criteria.toString();

        assertNotNull(toString, "ToString não deve ser null");
        assertTrue(toString.contains("test@example.com"), "Deve conter userId");
        assertTrue(toString.contains("50"), "Deve conter maxResults");
        assertTrue(toString.contains("EmailSearchCriteria"), "Deve conter nome da classe");
    }

    @Test
    @DisplayName("Deve criar builder a partir de método estático")
    void deveCriarBuilderAPartirDeMetodoEstatico() {
        EmailSearchCriteria.Builder builder = EmailSearchCriteria.builder();

        assertNotNull(builder, "Builder não deve ser null");
        
        EmailSearchCriteria criteria = builder.build();
        assertNotNull(criteria, "Critério deve ser criado a partir do builder");
    }
}
