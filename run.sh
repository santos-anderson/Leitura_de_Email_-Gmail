#!/bin/bash

# Script para executar a aplicação Gmail Reader sem warnings do Java 17+

echo "Iniciando Gmail Reader..."

# Configurações JVM para suprimir warnings específicos
JVM_ARGS="--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --add-opens=java.base/java.io=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS --enable-native-access=ALL-UNNAMED"
JVM_ARGS="$JVM_ARGS -XX:+IgnoreUnrecognizedVMOptions"

# Executa a aplicação com Maven
mvn spring-boot:run -Dspring-boot.run.jvmArguments="$JVM_ARGS"
