package com.desafio.fullstack.controller;

import com.desafio.fullstack.dto.EmpresaDTO;
import com.desafio.fullstack.dto.FornecedorDTO;
import com.desafio.fullstack.dto.PageResponse;
import com.desafio.fullstack.enums.TipoPessoa;
import com.desafio.fullstack.exception.BusinessException;
import com.desafio.fullstack.exception.GlobalExceptionHandler;
import com.desafio.fullstack.exception.ResourceNotFoundException;
import com.desafio.fullstack.service.EmpresaService;
import com.desafio.fullstack.service.FornecedorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ControllerTest {

    @Mock
    private EmpresaService empresaService;

    @Mock
    private FornecedorService fornecedorService;

    @InjectMocks
    private EmpresaController empresaController;

    @InjectMocks
    private FornecedorController fornecedorController;

    private MockMvc empresaMockMvc;
    private MockMvc fornecedorMockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        empresaMockMvc = MockMvcBuilders.standaloneSetup(empresaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .addPlaceholderValue("app.cors.allowed-origins", "http://localhost:4200")
                .build();

        fornecedorMockMvc = MockMvcBuilders.standaloneSetup(fornecedorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .addPlaceholderValue("app.cors.allowed-origins", "http://localhost:4200")
                .build();
    }

    // EMPRESA CONTROLLER
    @Nested
    @DisplayName("EmpresaController")
    class EmpresaControllerTests {

        private EmpresaDTO.Response empresaResponse() {
            return EmpresaDTO.Response.builder()
                    .id(1L)
                    .cnpj("12345678000199")
                    .nomeFantasia("Empresa Teste")
                    .cep("80000000")
                    .uf("PR")
                    .cidade("Curitiba")
                    .criadoEm(LocalDateTime.now())
                    .fornecedores(List.of())
                    .build();
        }

        @Test
        @DisplayName("GET /api/empresas - Deve retornar lista paginada")
        void deveListarEmpresas() throws Exception {
            var page = PageResponse.<EmpresaDTO.Response>builder()
                    .content(List.of(empresaResponse()))
                    .pageNumber(0).pageSize(10).totalElements(1).totalPages(1)
                    .first(true).last(true).build();

            when(empresaService.findAll(anyString(), any())).thenReturn(page);

            empresaMockMvc.perform(get("/api/empresas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].cnpj").value("12345678000199"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("GET /api/empresas/{id} - Deve retornar empresa por ID")
        void deveBuscarEmpresaPorId() throws Exception {
            when(empresaService.findById(1L)).thenReturn(empresaResponse());

            empresaMockMvc.perform(get("/api/empresas/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nomeFantasia").value("Empresa Teste"))
                    .andExpect(jsonPath("$.uf").value("PR"));
        }

        @Test
        @DisplayName("GET /api/empresas/{id} - Deve retornar 404 quando não encontrada")
        void deveRetornar404QuandoNaoEncontrada() throws Exception {
            when(empresaService.findById(99L))
                    .thenThrow(new ResourceNotFoundException("Empresa", 99L));

            empresaMockMvc.perform(get("/api/empresas/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/empresas - Deve criar empresa e retornar 201")
        void deveCriarEmpresa() throws Exception {
            var request = EmpresaDTO.Request.builder()
                    .cnpj("12345678000199")
                    .nomeFantasia("Empresa Nova")
                    .cep("80000000")
                    .build();

            when(empresaService.create(any())).thenReturn(empresaResponse());

            empresaMockMvc.perform(post("/api/empresas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("DELETE /api/empresas/{id} - Deve excluir e retornar 204")
        void deveExcluirEmpresa() throws Exception {
            doNothing().when(empresaService).delete(1L);

            empresaMockMvc.perform(delete("/api/empresas/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("POST /api/empresas/{id}/fornecedores/{fId} - Deve vincular")
        void deveVincularFornecedor() throws Exception {
            when(empresaService.vincularFornecedor(1L, 1L)).thenReturn(empresaResponse());

            empresaMockMvc.perform(post("/api/empresas/1/fornecedores/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST vincular - Deve retornar 400 quando menor no PR")
        void deveRetornar400QuandoMenorNoPR() throws Exception {
            when(empresaService.vincularFornecedor(1L, 2L))
                    .thenThrow(new BusinessException(
                            "Empresas do Paraná não podem cadastrar fornecedor pessoa física menor de idade."));

            empresaMockMvc.perform(post("/api/empresas/1/fornecedores/2"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /api/empresas/{id}/fornecedores/{fId} - Deve desvincular")
        void deveDesvincularFornecedor() throws Exception {
            when(empresaService.desvincularFornecedor(1L, 1L)).thenReturn(empresaResponse());

            empresaMockMvc.perform(delete("/api/empresas/1/fornecedores/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT /api/empresas/{id} - Deve atualizar empresa")
        void deveAtualizarEmpresa() throws Exception {
            var request = EmpresaDTO.Request.builder()
                    .cnpj("12345678000199")
                    .nomeFantasia("Atualizada")
                    .cep("80000000")
                    .build();

            when(empresaService.update(eq(1L), any())).thenReturn(empresaResponse());

            empresaMockMvc.perform(put("/api/empresas/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    // FORNECEDOR CONTROLLER
    @Nested
    @DisplayName("FornecedorController")
    class FornecedorControllerTests {

        private FornecedorDTO.Response fornecedorResponse() {
            return FornecedorDTO.Response.builder()
                    .id(1L)
                    .cpfCnpj("12345678901")
                    .tipoPessoa(TipoPessoa.FISICA)
                    .nome("João Silva")
                    .email("joao@email.com")
                    .cep("01001000")
                    .rg("123456789")
                    .dataNascimento(LocalDate.of(1990, 5, 15))
                    .uf("SP").cidade("São Paulo")
                    .criadoEm(LocalDateTime.now())
                    .empresas(List.of())
                    .build();
        }

        @Test
        @DisplayName("GET /api/fornecedores - Deve retornar lista com filtros")
        void deveListarFornecedores() throws Exception {
            var page = PageResponse.<FornecedorDTO.Response>builder()
                    .content(List.of(fornecedorResponse()))
                    .pageNumber(0).pageSize(10).totalElements(1).totalPages(1)
                    .first(true).last(true).build();

            when(fornecedorService.findAll(anyString(), anyString(), any())).thenReturn(page);

            fornecedorMockMvc.perform(get("/api/fornecedores")
                            .param("nome", "João")
                            .param("cpfCnpj", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].nome").value("João Silva"))
                    .andExpect(jsonPath("$.content[0].tipoPessoa").value("FISICA"));
        }

        @Test
        @DisplayName("POST /api/fornecedores - Deve criar PF e retornar 201")
        void deveCriarFornecedorPF() throws Exception {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678901")
                    .tipoPessoa(TipoPessoa.FISICA)
                    .nome("João Silva")
                    .email("joao@email.com")
                    .cep("01001000")
                    .rg("123456789")
                    .dataNascimento(LocalDate.of(1990, 5, 15))
                    .build();

            when(fornecedorService.create(any())).thenReturn(fornecedorResponse());

            fornecedorMockMvc.perform(post("/api/fornecedores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.rg").value("123456789"));
        }

        @Test
        @DisplayName("PUT /api/fornecedores/{id} - Deve atualizar fornecedor")
        void deveAtualizarFornecedor() throws Exception {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678901")
                    .tipoPessoa(TipoPessoa.FISICA)
                    .nome("João Atualizado")
                    .email("joao.novo@email.com")
                    .cep("01001000")
                    .rg("123456789")
                    .dataNascimento(LocalDate.of(1990, 5, 15))
                    .build();

            when(fornecedorService.update(eq(1L), any())).thenReturn(fornecedorResponse());

            fornecedorMockMvc.perform(put("/api/fornecedores/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /api/fornecedores/{id} - Deve excluir e retornar 204")
        void deveExcluirFornecedor() throws Exception {
            doNothing().when(fornecedorService).delete(1L);

            fornecedorMockMvc.perform(delete("/api/fornecedores/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("GET /api/fornecedores/{id} - Deve retornar 404")
        void deveRetornar404FornecedorNaoEncontrado() throws Exception {
            when(fornecedorService.findById(99L))
                    .thenThrow(new ResourceNotFoundException("Fornecedor", 99L));

            fornecedorMockMvc.perform(get("/api/fornecedores/99"))
                    .andExpect(status().isNotFound());
        }
    }
}
