#!/bin/bash

# Script para executar a aplicação Gmail Reader
# Configura as variáveis de ambiente necessárias

echo "Configurando variáveis de ambiente..."

# Configurações OAuth e Pub/Sub
export OAUTH_CREDENTIALS_PATH="/Users/andersoasant/gmail/credentials.json"
export GCP_PROJECT_ID="gmailteste10"
export GCP_PUBSUB_SUBSCRIPTION="gmail-notifications-sub"
export GCP_PUBSUB_CREDENTIALS_PATH="/Users/andersoasant/gmail/gmailteste10-e43825a05a78.json"

echo "Variáveis configuradas:"
echo "  OAUTH_CREDENTIALS_PATH: $OAUTH_CREDENTIALS_PATH"
echo "  GCP_PROJECT_ID: $GCP_PROJECT_ID"
echo "  GCP_PUBSUB_SUBSCRIPTION: $GCP_PUBSUB_SUBSCRIPTION"
echo "  GCP_PUBSUB_CREDENTIALS_PATH: $GCP_PUBSUB_CREDENTIALS_PATH"

echo ""
echo "Verificando se os arquivos de credenciais existem..."

if [ ! -f "$OAUTH_CREDENTIALS_PATH" ]; then
    echo "❌ ERRO: Arquivo OAuth não encontrado: $OAUTH_CREDENTIALS_PATH"
    exit 1
fi

if [ ! -f "$GCP_PUBSUB_CREDENTIALS_PATH" ]; then
    echo "❌ ERRO: Arquivo Pub/Sub não encontrado: $GCP_PUBSUB_CREDENTIALS_PATH"
    exit 1
fi

echo "✅ Todos os arquivos de credenciais encontrados!"
echo ""
echo "Iniciando Gmail Reader..."

# Configurações JVM para suprimir warnings específicos
JVM_ARGS="--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens=java.base/java.io=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --enable-native-access=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS -XX:+IgnoreUnrecognizedVMOptions"

# Executa a aplicação com Maven
mvn spring-boot:run -Dspring-boot.run.jvmArguments="$JVM_ARGS"
