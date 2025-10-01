package com.gmailreader.service.provider;

import com.gmailreader.constants.GmailConstants;
import lombok.Getter;
import lombok.ToString;

import java.util.List;


@Getter
@ToString
public class EmailSearchCriteria {

    private final String userId;
    private final List<String> labelIds;
    private final List<String> idsExcluidos;
    private final Integer maxResults;
    private final String query;


    private EmailSearchCriteria(Builder builder) {
        this.userId = builder.userId;
        this.labelIds = builder.labelIds;
        this.idsExcluidos = builder.idsExcluidos;
        this.maxResults = builder.maxResults;
        this.query = builder.query;
    }


    public static Builder builder() {
        return new Builder();
    }


    // Getters gerados automaticamente pelo Lombok @Getter


    public static class Builder {
        private String userId = GmailConstants.Gmail.CURRENT_USER_ID;
        private List<String> labelIds = List.of(GmailConstants.Gmail.INBOX_LABEL);
        private List<String> idsExcluidos = List.of();
        private Integer maxResults;
        private String query;


        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }


        public Builder labelIds(List<String> labelIds) {
            this.labelIds = labelIds;
            return this;
        }

        public Builder idsExcluidos(List<String> idsExcluidos) {
            this.idsExcluidos = idsExcluidos;
            return this;
        }

        public Builder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }


        public EmailSearchCriteria build() {
            return new EmailSearchCriteria(this);
        }
    }

}
