# Itau - Desafio Técnico Backend

Este projeto implementa uma API REST para gestão de limite de contas.

## Requisitos

- Java 21
- Maven
- Docker e Docker Compose (para o banco de dados PostgreSQL)

## Como Executar o Projeto

Siga os passos abaixo para levantar a aplicação:

### 1. Iniciar o Banco de Dados PostgreSQL com Docker Compose

Certifique-se de ter o Docker e Docker Compose instalados. No diretório raiz do projeto, execute:

```bash
docker-compose up -d
```

Este comando irá iniciar um container PostgreSQL e criar o banco de dados `desafio_itau` com o usuário `postgres` e senha `postgres`, conforme configurado no `docker-compose.yml`.

### 2. Compilar e Rodar a Aplicação Spring Boot

Após o banco de dados estar em execução, você pode iniciar a aplicação Spring Boot. No diretório raiz do projeto, execute:

```bash
./mvnw spring-boot:run
```

A aplicação será iniciada na porta `8080` por padrão.

## Endpoints da API

A API oferece os seguintes endpoints para a gestão de contratos de limite:

### Contratos

-   **Criar Contrato**
    -   `POST /api/v1/contratos`
    -   **Body:**
        ```json
        {
            "accountId": "identificador da conta",
            "valorLimite": 1000.00
        }
        ```
    -   **Retorna:** Dados do contrato criado, incluindo `saldoDevedor` (inicialmente 0) e `limiteDisponivel`.

-   **Consultar Contrato**
    -   `GET /api/v1/contratos/{accountId}`
    -   **Retorna:** Dados do contrato, incluindo `valorLimite`, `saldoDevedor` e `limiteDisponivel`.

-   **Alterar Limite do Contrato**
    -   `PUT /api/v1/contratos/{accountId}/limite`
    -   **Body:**
        ```json
        {
            "valor": 1500.00
        }
        ```
    -   **Retorna:** Dados do contrato atualizado.
    -   **Regra:** O novo `valorLimite` não pode ser inferior ao `saldoDevedor` atual.

-   **Cancelar Contrato**
    -   `DELETE /api/v1/contratos/{accountId}`
    -   **Retorna:** Status 204 No Content.
    -   **Regra:** Não permite cancelamento se houver `saldoDevedor` maior que zero.

### Movimentações

-   **Registrar Débito**
    -   `POST /api/v1/contratos/{accountId}/debito`
    -   **Body:**
        ```json
        {
            "valor": 200.00
        }
        ```
    -   **Retorna:** Dados do contrato atualizado após o débito.
    -   **Regra:** Não permite débito que faça o `limiteDisponivel` ficar negativo.

-   **Registrar Crédito**
    -   `POST /api/v1/contratos/{accountId}/credito`
    -   **Body:**
        ```json
        {
            "valor": 100.00
        }
        ```
    -   **Retorna:** Dados do contrato atualizado após o crédito.
    -   **Regra:** Reduz o `saldoDevedor` até no máximo zerar; créditos não podem gerar `saldoDevedor` negativo.

## Regras de Negócio Importantes

-   `saldoDevedor` nunca pode ser negativo.
-   `limiteDisponivel` nunca pode ser maior que `valorLimite` nem negativo.
-   Cancelamento só é permitido quando `saldoDevedor` for zero.

## Dependências

As principais dependências do projeto são:
-   `spring-boot-starter-data-jpa`: Para persistência de dados com JPA e Hibernate.
-   `spring-boot-starter-web`: Para construir a API REST.
-   `postgresql`: Driver JDBC para conexão com PostgreSQL.
-   `lombok`: Para reduzir código boilerplate (getters, setters, construtores, etc.).

## Collection Bruno para Testes da API

Uma collection Bruno junto no projeto e documentada para facilitar os testes.
