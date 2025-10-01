# Gmail Reader

Sistema automatizado para leitura e processamento de emails do Gmail usando Google Cloud Pub/Sub.

## Funcionalidades

- ✅ **Processamento automático** de emails via Pub/Sub
- ✅ **Watch do Gmail** para notificações em tempo real
- ✅ **Parsing de notificações** JSON
- ✅ **Salvamento em arquivos** JSON organizados por data
- ✅ **Controle de duplicatas** - evita reprocessamento
- ✅ **Logs detalhados** para monitoramento

## Configuração

### 1. Credenciais do Google

Você precisará de dois arquivos de credenciais:

1. **OAuth Credentials** (`credentials.json`):
   - Baixe do Google Cloud Console → APIs & Services → Credentials
   - Para autenticação OAuth do Gmail

2. **Service Account** (`service-account.json`):
   - Baixe do Google Cloud Console → IAM & Admin → Service Accounts
   - Para autenticação do Pub/Sub

### 2. Configuração Local

1. Copie o arquivo de exemplo:
   ```bash
   cp src/main/resources/application-example.properties src/main/resources/application-local.properties
   ```

2. Edite `application-local.properties` com seus valores reais:
   ```properties
   oauth.credentials.file.path=/caminho/para/seu/credentials.json
   gcp.project.id=seu-projeto-id
   gcp.pubsub.credentials.path=/caminho/para/seu/service-account.json
   ```

### 3. Variáveis de Ambiente (Alternativa)

Você pode usar variáveis de ambiente em vez do arquivo local:

```bash
export OAUTH_CREDENTIALS_PATH="/caminho/para/credentials.json"
export GCP_PROJECT_ID="seu-projeto-id"
export GCP_PUBSUB_SUBSCRIPTION="gmail-notifications-sub"
export GCP_PUBSUB_CREDENTIALS_PATH="/caminho/para/service-account.json"
```

## Execução

### Desenvolvimento
```bash
mvn spring-boot:run
```

### Produção
```bash
mvn clean package
java -jar target/gmailreader-0.0.1-SNAPSHOT.jar
```

## Estrutura do Projeto

```
src/main/java/com/gmailreader/
├── config/                 # Configurações e credenciais
├── service/                # Serviços principais
│   ├── processing/         # Steps de processamento
│   └── ...
└── GmailreaderApplication.java
```

## Arquivos Gerados

- `emails/emails-YYYY-MM-DD.json` - Emails processados por data
- `tokens/` - Tokens OAuth (gerados automaticamente)

## Segurança

⚠️ **IMPORTANTE**: Nunca commite arquivos de credenciais!

Os seguintes arquivos/pastas são ignorados pelo Git:
- `*.json` (credenciais)
- `tokens/` (tokens OAuth)
- `emails/` (dados processados)
- `application-local.properties`

## Troubleshooting

### Erro de Credenciais
- Verifique se os caminhos nos arquivos de configuração estão corretos
- Confirme se as credenciais têm as permissões necessárias

### Pub/Sub não recebe mensagens
- Verifique se o Gmail Watch está ativo
- Confirme se o topic e subscription estão configurados corretamente

### Emails não são processados
- Verifique os logs para erros específicos
- Confirme se há emails novos (sistema só processa emails não lidos)
