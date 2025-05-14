# Autores
- Filipe Ferreira
- Otávio Nardini

# Fluxo Geral:
1. Cliente conecta ao servidor.
2. Envia nickname.
3. Lê e envia mensagens com ajuda da `InputHandler`.
4. Mostra mensagens recebidas do servidor no terminal.

Conceito de Redes Aplicado: Comunicação confiável com múltiplos clientes via TCP.

# 🚀 Chat Server & Client - Java Socket Application

Uma simples aplicação de chat multi-client desenvolvida com Java Sockets
A simple multi-client chat application built with Java sockets.

## 📋 Pré-requisitos

- Java JDK 17+
- Firewall desabilitado ou configurado para permitir conexões na porta 9999
- Máquinas virtuais/máquinas físicas na mesma rede com rota IPv4 configurada

## ⚠️ Configuração Importante

Antes de executar, **desative o firewall temporariamente** ou configure as regras para permitir tráfego na porta 9999.

Para testar entre VMs:
1. Verifique que as VMs têm rota de rede entre si
2. Confira os endereços IPv4 com `ipconfig` (Windows) ou `ifconfig` (Linux/Mac)
3. No cliente, altere `127.0.0.1` para o IP do servidor

## 🏗️ Estrutura do Projeto

ChatApplication/
├── Server.java # Implementação do servidor
└── Client.java # Implementação do cliente


## 🖥️ Como Executar

1. **Servidor**:
```bash
javac Server.java
java Server
javac Client.java
java Client
