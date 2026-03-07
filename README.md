# Desafio Arphoenix - Sistema de Processamento de Pagamentos

## 📋 Descrição do Projeto

Este projeto é uma solução desenvolvida como resposta a um desafio de programação, implementando um sistema distribuído de processamento de pagamentos. O sistema é composto por dois microsserviços independentes que se comunicam de forma assíncrona através de mensageria, simulando um fluxo real de autorização de transações financeiras.

### 🎯 Objetivo do Desafio

O desafio consiste em criar uma arquitetura de microsserviços capaz de processar solicitações de pagamento de maneira eficiente e escalável. O sistema deve:

- Receber e validar solicitações de pagamento via API REST
- Persistir transações em um banco de dados
- Enviar solicitações para autorização através de mensageria
- Processar autorizações de forma assíncrona
- Retornar respostas com status atualizado das transações
- Suportar operações de consulta e estorno de transações

### 🏗️ Arquitetura do Sistema


<img width="6013" height="2138" alt="Untitled-2026-03-05-0011" src="https://github.com/user-attachments/assets/a5e8b72b-ba30-4341-ae9a-1633afb36609" />


O projeto segue uma arquitetura de microsserviços com comunicação assíncrona:

1. **Serviço de Pagamentos (toolschallange)**: Responsável por receber solicitações de pagamento, validar dados, persistir transações e coordenar o fluxo de autorização.

2. **Serviço Autorizador (toolschallangeAutorizador)**: Simula um sistema externo de autorização bancária, processando solicitações e retornando decisões de aprovação ou recusa.

A comunicação entre os serviços é realizada através do Apache Kafka, utilizando tópicos específicos para cada etapa do processo.

## 🛠️ Tecnologias e Dependências Utilizadas

### Linguagem e Framework Principal
- **Java 21**: Versão LTS mais recente, oferecendo recursos modernos da linguagem
- **Spring Boot 3.5.11**: Framework para desenvolvimento de aplicações Java, com starters para facilitar a configuração

### Dependências Core
- **Spring Web**: Para criação de APIs REST
- **Spring Data JPA**: Para persistência de dados com Hibernate
- **Spring Kafka**: Para integração com Apache Kafka
- **Spring Validation**: Para validação de dados de entrada
- **H2 Database**: Banco de dados em memória para desenvolvimento e testes

### Ferramentas de Desenvolvimento
- **Lombok**: Redução de código boilerplate através de anotações
- **MapStruct**: Mapeamento automático entre objetos (DTOs e entidades)
- **Maven**: Gerenciamento de dependências e build do projeto
- **Docker**: Containerização do ambiente Kafka

### Infraestrutura
- **Apache Kafka**: Plataforma de mensageria para comunicação assíncrona entre serviços
- **Docker Compose**: Orquestração de containers para ambiente de desenvolvimento

## 📁 Estrutura do Projeto

```
desafio-arphoenix/
├── toolschallange/                    # Microsserviço de Pagamentos
│   ├── src/main/java/com/arphoenix/toolschallange/
│   │   ├── controller/                # Controladores REST
│   │   ├── domain/                    # Camada de domínio
│   │   │   ├── entities/              # Entidades JPA
│   │   │   ├── enums/                 # Enums do domínio
│   │   │   ├── mappers/               # Mapeadores MapStruct
│   │   │   ├── records/               # Records para DTOs
│   │   │   └── repositories/          # Repositórios JPA
│   │   ├── exception/                 # Tratamento de exceções
│   │   ├── messaging/                 # Configuração Kafka
│   │   └── service/                   # Lógica de negócio
│   └── pom.xml                        # Dependências Maven
│
├── toolschallangeAutorizador/         # Microsserviço Autorizador
│   ├── src/main/java/com/arphoenix/toolschallangeAutorizador/
│   │   ├── domain/                    # Camada de domínio
│   │   ├── messaging/                 # Configuração Kafka
│   │   ├── service/                   # Lógica de autorização
│   │   └── util/                      # Utilitários
│   └── pom.xml                        # Dependências Maven
│
├── docker-compose.yml                 # Configuração Kafka
└── README.md                          # Este arquivo
```

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Java 21 instalado
- Maven 3.6+ instalado
- Docker e Docker Compose instalados

### Passos para Execução

1. **Clonar o repositório**:
   ```bash
   git clone <url-do-repositorio>
   cd desafio-arphoenix
   ```

2. **Iniciar o Kafka**:
   ```bash
   cd toolschallange
   docker-compose up -d
   ```

3. **Executar o Serviço Autorizador**:
   ```bash
   cd ../toolschallangeAutorizador
   mvn spring-boot:run
   ```

4. **Executar o Serviço de Pagamentos** (em outro terminal):
   ```bash
   cd ../toolschallange
   mvn spring-boot:run
   ```

### Verificação da Execução

- **Serviço de Pagamentos**: http://localhost:8080
- **Console H2**: http://localhost:8080/h2-console
- **Kafka**: localhost:9092

## 📡 API Endpoints

### Serviço de Pagamentos (porta 8080)

#### Processar Pagamento
```http
POST /pagamentos
Content-Type: application/json

{
  "transacao": {
    "id": "123456",
    "cartao": "1234567890123456",
    "valor": 100.50,
    "descricao": {
      "valor": "100.50",
      "dataHora": "2023-12-01T10:00:00",
      "estabelecimento": "Loja Exemplo"
    },
    "formaPagamento": {
      "tipo": "AVISTA",
      "parcelas": 1
    }
  }
}
```

#### Consultar Todos os Pagamentos
```http
GET /pagamentos
```

#### Consultar Pagamento por ID
```http
GET /pagamentos/{id}
```

#### Estornar Pagamento
```http
GET /pagamentos/estorno/{id}
```

## 🔄 Fluxo de Processamento


<img width="6346" height="4523" alt="Untitled-2026-03-05-0011-2" src="https://github.com/user-attachments/assets/748251f3-3182-4697-ab6f-954f1843a6a6" />

1. **Recebimento**: O serviço de pagamentos recebe uma solicitação via API REST
2. **Validação**: Dados são validados e uma transação é criada com status PENDENTE
3. **Mensageria**: Solicitação é enviada para o tópico "vendas-pendentes" no Kafka
4. **Autorização**: Serviço autorizador consome a mensagem e simula processamento
5. **Decisão**: Autorização é aprovada ou negada aleatoriamente
6. **Resposta**: Resultado é enviado para o tópico "vendas-finalizadas"
7. **Finalização**: Serviço de pagamentos atualiza os dados da transacao com base no processamento recebido
8. **Persistência**: Transação é salva no banco de dados H2 
9. **Retorno**: Resposta é retornada para o cliente

## 🧪 Testes

Para executar os testes:
```bash
# No diretório de cada serviço
mvn test
```

Os testes incluem:
- Testes unitários de serviços
- Testes de integração com Kafka
- Testes de repositórios JPA

## 📊 Decisões Técnicas

### Por que Java 21 e Spring Boot 3?
- Java 21 oferece recursos modernos como records e pattern matching
- Spring Boot 3 é compatível com Java 17+ e oferece melhorias de performance

### Por que Kafka?
- Comunicação assíncrona e desacoplada entre serviços
- Escalabilidade horizontal
- Tolerância a falhas

### Por que H2 Database?
- Banco em memória para desenvolvimento
- Não requer instalação adicional
- Fácil reset para testes

### Por que Arquitetura de Microsserviços?
- Separação de responsabilidades
- Escalabilidade independente
- Facilita manutenção e evolução

## 🤝 Contribuição

Este projeto foi desenvolvido como solução para um desafio técnico. Para sugestões ou melhorias, sinta-se à vontade para abrir issues ou pull requests.

## 📄 Licença

Este projeto é distribuído sob a licença MIT. Consulte o arquivo LICENSE para mais detalhes.
