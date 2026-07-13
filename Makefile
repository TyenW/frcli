# Makefile para o RPG Character Sheet & Management System (FRCLI)

.PHONY: all build run test package clean help

# Comando padrão
all: build

# Compila o projeto
build:
	mvn clean compile

# Executa o painel CLI interativo
run:
	mvn exec:java -Dexec.mainClass="br.com.frcli.Main"

# Executa os testes unitários JUnit
test:
	mvn test

# Gera o arquivo JAR empacotado na pasta target
package:
	mvn clean package

# Limpa os arquivos temporários de compilação
clean:
	mvn clean

# Exibe ajuda sobre os comandos do Makefile
help:
	@echo "Comandos disponíveis:"
	@echo "  make build   - Compila o código fonte Java usando Maven"
	@echo "  make run     - Compila e inicia o painel CLI interativo"
	@echo "  make test    - Executa a suite de testes unitários JUnit"
	@echo "  make package - Empacota a aplicação em um arquivo JAR executável"
	@echo "  make clean   - Limpa a pasta 'target' do Maven"
