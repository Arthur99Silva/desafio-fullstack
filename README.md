# Desafio FullStack

## Tecnologias

- **Backend:** Java 21 (Spring Boot) / Maven
- **Frontend:** Angular 17
- **Banco de Dados:** PostgreSQL
- **Infraestrutura:** Docker e Docker Compose

## Como Executar

### Pré-requisitos

[Git](https://git-scm.com), [Node.js](https://nodejs.org/en/) e o [Docker](https://www.docker.com/).

### Opção 1: Docker Compose
```bash
docker-compose up --build
```
- Frontend: http://localhost
- Backend API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

### Opção 2: Desenvolvimento Local

**Backend:**
```bash
cd backend
mvn spring-boot:run
```
> API em http://localhost:8080
> H2 Console em http://localhost:8080/h2-console

**Frontend:**
```bash
cd frontend
npm install
ng serve
```
> App em http://localhost:4200

## Endpoints da API

### Empresas
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/empresas?search=&page=0&size=10` | Listar com busca e paginação |
| GET | `/api/empresas/{id}` | Buscar por ID |
| POST | `/api/empresas` | Criar empresa |
| PUT | `/api/empresas/{id}` | Atualizar empresa |
| DELETE | `/api/empresas/{id}` | Excluir empresa |
| POST | `/api/empresas/{id}/fornecedores/{fId}` | Vincular fornecedor |
| DELETE | `/api/empresas/{id}/fornecedores/{fId}` | Desvincular fornecedor |

### Fornecedores
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/fornecedores?nome=&cpfCnpj=&page=0&size=10` | Listar com filtros |
| GET | `/api/fornecedores/{id}` | Buscar por ID |
| POST | `/api/fornecedores` | Criar fornecedor |
| PUT | `/api/fornecedores/{id}` | Atualizar fornecedor |
| DELETE | `/api/fornecedores/{id}` | Excluir fornecedor |

### CEP
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/cep/{cep}` | Consultar e validar CEP |

---

## Testes

```bash
cd backend
mvn test
```