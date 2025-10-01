package com.gmailreader.constants;


public final class GmailConstants {


    private GmailConstants() {
        throw new UnsupportedOperationException("Classe de constantes não deve ser instanciada");
    }


    public static final class Gmail {

        public static final String CURRENT_USER_ID = "me";

        public static final String INBOX_LABEL = "INBOX";


        private Gmail() {
            throw new UnsupportedOperationException("Classe de constantes não deve ser instanciada");
        }
    }


    public static final class Storage {

        public static final String EMAIL_FILE_PREFIX = "emails-";

        public static final String EMAIL_FILE_EXTENSION = ".json";

        private Storage() {
            throw new UnsupportedOperationException("Classe de constantes não deve ser instanciada");
        }
    }
}
