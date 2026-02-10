# Desafio Full-Stack — Empresas & Fornecedores

Sistema completo de gerenciamento de **Empresas** e **Fornecedores** com relacionamento N:N, validação de CEP via API externa, e regras de negócio específicas.

## Stack Tecnológica

### Backend
- **Java 21** + **Spring Boot 3.2**
- **Spring Data JPA** (Hibernate)
- **H2** (desenvolvimento) / **PostgreSQL** (produção)
- **Lombok** + **MapStruct**
- **SpringDoc OpenAPI** (Swagger UI)
- **JUnit 5** + **Mockito** (testes)

### Frontend
- **Angular 17** (standalone components, new control flow)
- **TypeScript**
- **SCSS** (design system próprio)
- Lazy loading de rotas

### Infraestrutura
- **Docker** + **Docker Compose**
- **Nginx** (servindo frontend + proxy reverso)

---

## Arquitetura

```
┌─────────────┐     ┌──────────────────┐     ┌──────────────┐
│   Angular    │────▶│  Spring Boot API  │────▶│  PostgreSQL  │
│  (Nginx:80)  │     │   (Java:8080)     │     │   (:5432)    │
└─────────────┘     └──────────────────┘     └──────────────┘
                           │
                    ┌──────┴──────┐
                    │  cep.la API │
                    │  ViaCEP API │
                    └─────────────┘
```

---

## Como Executar

### Opção 1: Docker Compose (Recomendado)
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

---

## Regras de Negócio Implementadas

| # | Regra | Onde |
|---|-------|------|
| a | CRUD completo de Empresas e Fornecedores | Controllers + Services |
| b | Empresa pode ter múltiplos fornecedores (N:N) | `@ManyToMany` JPA |
| c | Fornecedor pode trabalhar para múltiplas empresas | Relacionamento bidirecional |
| d | CNPJ e CPF únicos | `unique=true` + validação no Service |
| e | PF exige RG e Data de Nascimento | `FornecedorService.validarRequest()` |
| f | Empresa do PR bloqueia PF menor de idade | `EmpresaService.validarFornecedorMenorParana()` |
| g | Filtros por Nome e CPF/CNPJ na listagem | `FornecedorRepository.findByFilters()` |
| h | Validação de CEP (cep.la + ViaCEP fallback) | `CepService` backend + `CepService` frontend |
| i | Campos extras: endereço completo, timestamps | Entidades enriquecidas |
| j | Testes unitários | `EmpresaServiceTest` + `FornecedorServiceTest` |
| k | Dockerfile | Backend + Frontend + docker-compose |

---

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

## Estrutura do Projeto

```
desafio-fullstack/
├── docker-compose.yml
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/desafio/fullstack/
│       ├── config/          # CORS, OpenAPI
│       ├── controller/      # REST endpoints
│       ├── dto/             # Request/Response DTOs
│       ├── entity/          # JPA entities
│       ├── enums/           # TipoPessoa
│       ├── exception/       # Global handler
│       ├── repository/      # Spring Data repos
│       └── service/         # Business logic
└── frontend/
    ├── Dockerfile
    ├── nginx.conf
    └── src/app/
        ├── components/      # Angular components
        │   ├── empresa/
        │   └── fornecedor/
        ├── models/          # TypeScript interfaces
        ├── pipes/           # CPF/CNPJ, CEP formatters
        └── services/        # HTTP services
```

---

## Testes

```bash
cd backend
mvn test
```

Testes cobrem:
- Criação de empresa com validação de CNPJ duplicado e CEP
- Regra de bloqueio de PF menor de idade para empresas do Paraná
- Validação de campos obrigatórios de Pessoa Física (RG, Data Nascimento)
- Consistência entre tipo de pessoa e documento
