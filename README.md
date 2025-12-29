# Itau - Desafio Técnico Backend

Este projeto implementa uma API REST para gestão de limite de contas, conforme o desafio técnico proposto. A API é protegida usando OAuth 2.0 e JWT, com o Keycloak como provedor de identidade.

## Requisitos

- Java 21
- Maven
- Docker e Docker Compose
- Bruno (cliente HTTP para testes da API)

## Como Executar o Projeto

Siga os passos abaixo para levantar a aplicação e o ambiente de segurança.

### 1. Iniciar os Serviços de Infraestrutura

O ambiente de desenvolvimento depende de um banco de dados PostgreSQL e de um servidor de autenticação Keycloak. Ambos são gerenciados pelo Docker Compose.

No diretório raiz do projeto, execute:

```bash
docker-compose up -d --build
```

Este comando irá iniciar:
- Um contêiner PostgreSQL na porta `5432` (banco de dados: `desafio_itau`, usuário: `postgres`, senha: `postgres`).
- Um contêiner Keycloak na porta `8081` com importação automática do realm configurado.

### 2. Keycloak (Configuração Automática e Obtenção do Client Secret)

O Keycloak será configurado automaticamente com o `realm` `desafio-itau` e o `client` `api-contratos` na primeira inicialização (ou sempre que o volume de dados do Keycloak for recriado), através da importação do arquivo `keycloak-config/desafio-itau-realm.json` que está no projeto.

**Configuração do Client OAuth 2.0:**

Para que você possa usar as requisições do Bruno autenticadas, você precisará obter o `client_secret` do client `api-contratos`:

1.  **Acesse o Console do Keycloak:** Vá para `http://localhost:8081` e entre na "Administration Console" com o usuário `admin` e senha `admin`.
2.  **Selecione o Realm:** No canto superior esquerdo, certifique-se de que o realm `desafio-itau` está selecionado (se não estiver, selecione-o).
3.  **Vá para Clients:** No menu à esquerda, clique em **"Clients"**.
4.  **Encontre o Client `api-contratos`:** Clique no client com o ID `api-contratos`.
5.  **Copie o Client Secret:** Vá para a aba **"Credentials"** e copie o "Client secret" gerado.
6.  **Configure no Bruno:** No Bruno, abra o ambiente/environment "local" e cole o valor copiado na variável secreta `client_secret` (que representa o `client_secret`) e salve as alterações.

### 3. Rodar a Aplicação Spring Boot

Após os contêineres estarem no ar (e o Keycloak configurado automaticamente), você pode iniciar a aplicação Spring Boot pela sua IDE ou via terminal:

```bash
./mvnw spring-boot:run
```

A aplicação será iniciada na porta `8080`.

## Segurança e Autenticação (OAuth 2.0)

Todos os endpoints em `/api/v1/contratos/**` são protegidos por OAuth 2.0 com JWT Bearer Token. A aplicação atua como Resource Server, validando tokens emitidos pelo Keycloak.

### Fluxo de Autenticação

1. **Obter Token de Acesso:** Use a requisição **"pegar token"** na collection do Bruno
2. **Fluxo:** Client Credentials Grant (OAuth 2.0)
3. **Endpoint do Keycloak:** `POST http://localhost:8081/realms/desafio-itau/protocol/openid-connect/token`
4. **Parâmetros necessários:**
   - `grant_type`: `client_credentials`
   - `client_id`: `api-contratos`
   - `client_secret`: (valor obtido do console do Keycloak)

5. **Token Gerado:** O script de teste na requisição "pegar token" automaticamente extrai o `access_token` e salva na variável `bearer_token` do Bruno
6. **Uso do Token:** Todas as outras requisições da collection usam automaticamente `{{bearer_token}}` no header `Authorization: Bearer`

### Proteção dos Endpoints

- Todos os endpoints `/api/v1/contratos/**` requerem autenticação
- Tokens JWT são validados pelo Spring Security OAuth2 Resource Server
- Em caso de token inválido, expirado ou ausente vai retornar `401 Unauthorized`

### 4. Executar os Testes e Gerar Relatório de Cobertura

Para executar os testes unitários do projeto, utilize o seguinte comando:

```bash
./mvnw test
```

Para gerar o relatório de cobertura de código com JaCoCo, execute:

```bash
./mvnw clean test
```

Após a execução, o relatório de cobertura será gerado em formato HTML no diretório:

```
target/site/jacoco/index.html
```

Abra este arquivo em um navegador para visualizar a cobertura de código detalhada por pacote, classe e método.

**Nota:** O projeto está configurado com uma meta mínima de cobertura de 80% por pacote. Caso a cobertura fique abaixo desse valor, o build falhará na verificação do JaCoCo.

## Endpoints da API

A API oferece os seguintes endpoints para a gestão de contratos de limite. **Todos eles requerem autenticação via Bearer Token.**

### Contratos

-   **Criar Contrato**
    -   `POST /api/v1/contratos`
    -   **Autenticação:** Bearer Token obrigatório
    -   **Body:**
        ```json
        {
            "accountId": "1234567",
            "valorLimite": 1000.00
        }
        ```
    -   **Retorna (201 Created):** Dados do contrato criado, incluindo `saldoDevedor` (inicialmente 0) e `limiteDisponivel`.
    -   **Exceções:**
        - `401 Unauthorized`: Token inválido ou ausente
        - `400 Bad Request`: Dados inválidos

-   **Consultar Contrato**
    -   `GET /api/v1/contratos/{accountId}`
    -   **Autenticação:** Bearer Token obrigatório
    -   **Parâmetro:** `accountId` - Identificador único da conta (ex: "1234567")
    -   **Retorna (200 OK):** Dados do contrato, incluindo `valorLimite`, `saldoDevedor` e `limiteDisponivel`.
    -   **Exceções:**
        - `401 Unauthorized`: Token inválido ou ausente
        - `404 Not Found`: Contrato não encontrado

-   **Alterar Limite do Contrato**
    -   `PUT /api/v1/contratos/{accountId}/limite`
    -   **Autenticação:** Bearer Token obrigatório
    -   **Parâmetro:** `accountId` - Identificador único da conta
    -   **Body:**
        ```json
        {
            "valor": 20000.00
        }
        ```
    -   **Retorna (200 OK):** Dados do contrato atualizado.
    -   **Regra:** O novo `valorLimite` não pode ser inferior ao `saldoDevedor` atual.
    -   **Exceções:**
        - `401 Unauthorized`: Token inválido ou ausente
        - `404 Not Found`: Contrato não encontrado
        - `400 Bad Request`: Novo limite menor que saldo devedor

-   **Cancelar Contrato**
    -   `DELETE /api/v1/contratos/{accountId}`
    -   **Autenticação:** Bearer Token obrigatório
    -   **Parâmetro:** `accountId` - Identificador único da conta
    -   **Retorna:** Status 204 No Content.
    -   **Regra:** Não permite cancelamento se houver `saldoDevedor` maior que zero.
    -   **Exceções:**
        - `401 Unauthorized`: Token inválido ou ausente
        - `404 Not Found`: Contrato não encontrado
        - `400 Bad Request`: Contrato possui saldo devedor

### Movimentações

-   **Registrar Débito**
    -   `POST /api/v1/contratos/{accountId}/debito`
    -   **Autenticação:** Bearer Token obrigatório
    -   **Parâmetro:** `accountId` - Identificador único da conta
    -   **Body:**
        ```json
        {
            "valor": 500.00
        }
        ```
    -   **Retorna (200 OK):** Dados do contrato atualizado após o débito.
    -   **Regra:** Não permite débito que faça o `limiteDisponivel` ficar negativo.
    -   **Exceções:**
        - `401 Unauthorized`: Token inválido ou ausente
        - `404 Not Found`: Contrato não encontrado
        - `400 Bad Request`: Débito excede limite disponível

-   **Registrar Crédito**
    -   `POST /api/v1/contratos/{accountId}/credito`
    -   **Autenticação:** Bearer Token obrigatório
    -   **Parâmetro:** `accountId` - Identificador único da conta
    -   **Body:**
        ```json
        {
            "valor": 500.00
        }
        ```
    -   **Retorna (200 OK):** Dados do contrato atualizado após o crédito.
    -   **Regra:** Reduz o `saldoDevedor` até no máximo zerar; créditos não podem gerar `saldoDevedor` negativo.
    -   **Exceções:**
        - `401 Unauthorized`: Token inválido ou ausente
        - `404 Not Found`: Contrato não encontrado

## Collection Bruno para Testes da API

O projeto inclui uma collection completa do Bruno na pasta `bruno/` com todas as requisições necessárias para testar a API.

### Como Usar a Collection

1. **Importe a Collection:** Abra o Bruno e importe a pasta `bruno/`
2. **Selecione o Ambiente:** Escolha o ambiente "local"
3. **Configure o Client Secret:** 
   - Obtenha o client secret do Keycloak
   - Cole o valor na variável secreta `client_secret` do ambiente
4. **Obtenha o Token:** Execute a requisição "pegar token" **primeiro** para gerar e armazenar o Bearer Token
5. **Use os Endpoints:** Execute as demais requisições conforme necessário

## Regras de Negócio Importantes

-   `saldoDevedor` nunca pode ser negativo.
-   `limiteDisponivel` nunca pode ser maior que `valorLimite` nem negativo.
-   Cancelamento só é permitido quando `saldoDevedor` for zero.
-   Todos os endpoints requerem autenticação via OAuth 2.0 Bearer Token.

## Dependências

As principais dependências do projeto são:
-   `spring-boot-starter-data-jpa`: Para persistência de dados com JPA e Hibernate.
-   `postgresql`: Driver JDBC para conexão com PostgreSQL.
-   `lombok`: Para reduzir código boilerplate (getters, setters, construtores, etc.).
-   `jacoco-maven-plugin`: Para análise de cobertura de testes.
-   `spring-boot-starter-web`: Para construir a API REST.
-   `spring-boot-starter-oauth2-resource-server`: Para segurança OAuth 2.0 e validação de JWT.
-   `spring-boot-starter-security`: Para configuração de segurança.
-   `spring-security-oauth2-jose`: Suporte para JWT
