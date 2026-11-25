#!/bin/sh
# Compila e abre o menu (Linux/macOS). Uso: ./rodar.sh [pasta-de-dados]
set -e
mkdir -p out
javac -encoding UTF-8 -d out escola/*.java
java -Dfile.encoding=UTF-8 -cp out escola.Main "$@"
