#!/bin/sh
# Compila e roda os testes automáticos (Linux/macOS).
set -e
mkdir -p out
javac -encoding UTF-8 -d out escola/*.java test/escola/*.java
java -Dfile.encoding=UTF-8 -cp out escola.TestesEscola
