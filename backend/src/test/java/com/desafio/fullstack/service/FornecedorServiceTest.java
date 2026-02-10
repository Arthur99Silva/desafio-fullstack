package com.desafio.fullstack.service;

import com.desafio.fullstack.dto.CepDTO;
import com.desafio.fullstack.dto.FornecedorDTO;
import com.desafio.fullstack.entity.Fornecedor;
import com.desafio.fullstack.enums.TipoPessoa;
import com.desafio.fullstack.exception.BusinessException;
import com.desafio.fullstack.exception.ResourceNotFoundException;
import com.desafio.fullstack.repository.FornecedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private CepService cepService;

    @InjectMocks
    private FornecedorService fornecedorService;

    private CepDTO cepValido;
    private Fornecedor fornecedorPJ;
    private Fornecedor fornecedorPF;

    @BeforeEach
    void setUp() {
        cepValido = CepDTO.builder()
                .cep("01001000").uf("SP").cidade("São Paulo")
                .bairro("Sé").logradouro("Praça da Sé").valido(true)
                .build();

        fornecedorPJ = Fornecedor.builder()
                .id(1L)
                .cpfCnpj("12345678000199")
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Fornecedor PJ Ltda")
                .email("pj@email.com")
                .cep("01001000")
                .uf("SP").cidade("São Paulo")
                .criadoEm(LocalDateTime.now())
                .empresas(new HashSet<>())
                .build();

        fornecedorPF = Fornecedor.builder()
                .id(2L)
                .cpfCnpj("12345678901")
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("João Silva")
                .email("joao@email.com")
                .cep("01001000")
                .rg("123456789")
                .dataNascimento(LocalDate.of(1990, 5, 15))
                .uf("SP").cidade("São Paulo")
                .criadoEm(LocalDateTime.now())
                .empresas(new HashSet<>())
                .build();
    }

    // CRIAR FORNECEDOR - PESSOA JURÍDICA
    @Nested
    @DisplayName("Criar Fornecedor PJ")
    class CriarFornecedorPJ {

        @Test
        @DisplayName("Deve criar fornecedor PJ com sucesso")
        void deveCriarPJComSucesso() {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678000199")
                    .tipoPessoa(TipoPessoa.JURIDICA)
                    .nome("Fornecedor PJ Ltda")
                    .email("pj@email.com")
                    .cep("01001000")
                    .build();

            when(fornecedorRepository.existsByCpfCnpj(anyString())).thenReturn(false);
            when(cepService.consultarCep(anyString())).thenReturn(cepValido);
            when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedorPJ);

            FornecedorDTO.Response result = fornecedorService.create(request);

            assertNotNull(result);
            assertEquals(TipoPessoa.JURIDICA, result.getTipoPessoa());
            assertEquals("12345678000199", result.getCpfCnpj());
            verify(fornecedorRepository).save(any(Fornecedor.class));
        }

        @Test
        @DisplayName("Deve rejeitar PJ com CPF (11 dígitos ao invés de 14)")
        void deveRejeitarPJComCpf() {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678901") // 11 dígitos
                    .tipoPessoa(TipoPessoa.JURIDICA)
                    .nome("PJ com CPF")
                    .email("pj@email.com")
                    .cep("01001000")
                    .build();

            when(fornecedorRepository.existsByCpfCnpj(anyString())).thenReturn(false);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> fornecedorService.create(request)
            );

            assertTrue(ex.getMessage().contains("14 dígitos"));
        }
    }

    // CRIAR FORNECEDOR - PESSOA FÍSICA
    @Nested
    @DisplayName("Criar Fornecedor PF")
    class CriarFornecedorPF {

        @Test
        @DisplayName("Deve criar fornecedor PF com RG e Data de Nascimento")
        void deveCriarPFComSucesso() {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678901")
                    .tipoPessoa(TipoPessoa.FISICA)
                    .nome("João Silva")
                    .email("joao@email.com")
                    .cep("01001000")
                    .rg("123456789")
                    .dataNascimento(LocalDate.of(1990, 5, 15))
                    .build();

            when(fornecedorRepository.existsByCpfCnpj(anyString())).thenReturn(false);
            when(cepService.consultarCep(anyString())).thenReturn(cepValido);
            when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedorPF);

            FornecedorDTO.Response result = fornecedorService.create(request);

            assertNotNull(result);
            assertEquals(TipoPessoa.FISICA, result.getTipoPessoa());
            assertEquals("123456789", result.getRg());
            assertNotNull(result.getDataNascimento());
        }

        @Test
        @DisplayName("Deve EXIGIR RG para Pessoa Física")
        void deveExigirRgParaPF() {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678901")
                    .tipoPessoa(TipoPessoa.FISICA)
                    .nome("Sem RG")
                    .email("pf@email.com")
                    .cep("01001000")
                    .dataNascimento(LocalDate.of(1990, 1, 1))
                    // RG ausente
                    .build();

            when(fornecedorRepository.existsByCpfCnpj(anyString())).thenReturn(false);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> fornecedorService.create(request)
            );

            assertTrue(ex.getMessage().contains("RG"));
        }

        @Test
        @DisplayName("Deve EXIGIR Data de Nascimento para Pessoa Física")
        void deveExigirDataNascimentoParaPF() {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678901")
                    .tipoPessoa(TipoPessoa.FISICA)
                    .nome("Sem DN")
                    .email("pf@email.com")
                    .cep("01001000")
                    .rg("123456789")
                    // dataNascimento ausente
                    .build();

            when(fornecedorRepository.existsByCpfCnpj(anyString())).thenReturn(false);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> fornecedorService.create(request)
            );

            assertTrue(ex.getMessage().contains("Data de Nascimento"));
        }

        @Test
        @DisplayName("Deve rejeitar PF com CNPJ (14 dígitos ao invés de 11)")
        void deveRejeitarPFComCnpj() {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678000199") // 14 dígitos
                    .tipoPessoa(TipoPessoa.FISICA)
                    .nome("PF com CNPJ")
                    .email("pf@email.com")
                    .cep("01001000")
                    .rg("123456789")
                    .dataNascimento(LocalDate.of(1990, 1, 1))
                    .build();

            when(fornecedorRepository.existsByCpfCnpj(anyString())).thenReturn(false);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> fornecedorService.create(request)
            );

            assertTrue(ex.getMessage().contains("11 dígitos"));
        }

        @Test
        @DisplayName("Deve rejeitar RG em branco para Pessoa Física")
        void deveRejeitarRgEmBranco() {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678901")
                    .tipoPessoa(TipoPessoa.FISICA)
                    .nome("RG Branco")
                    .email("pf@email.com")
                    .cep("01001000")
                    .rg("   ") // branco
                    .dataNascimento(LocalDate.of(1990, 1, 1))
                    .build();

            when(fornecedorRepository.existsByCpfCnpj(anyString())).thenReturn(false);

            assertThrows(BusinessException.class, () -> fornecedorService.create(request));
        }
    }

    // UNICIDADE CPF/CNPJ
    @Nested
    @DisplayName("Unicidade CPF/CNPJ")
    class UnicidadeCpfCnpj {

        @Test
        @DisplayName("Deve rejeitar CPF/CNPJ duplicado na criação")
        void deveRejeitarDocumentoDuplicadoNaCriacao() {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678000199")
                    .tipoPessoa(TipoPessoa.JURIDICA)
                    .nome("Duplicado")
                    .email("dup@email.com")
                    .cep("01001000")
                    .build();

            when(fornecedorRepository.existsByCpfCnpj("12345678000199")).thenReturn(true);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> fornecedorService.create(request)
            );

            assertTrue(ex.getMessage().contains("já cadastrado"));
        }

        @Test
        @DisplayName("Deve rejeitar CPF/CNPJ que pertence a outro fornecedor na atualização")
        void deveRejeitarDocumentoDeOutroNaAtualizacao() {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("98765432000188")
                    .tipoPessoa(TipoPessoa.JURIDICA)
                    .nome("Update")
                    .email("up@email.com")
                    .cep("01001000")
                    .build();

            when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedorPJ));
            when(fornecedorRepository.existsByCpfCnpjAndIdNot("98765432000188", 1L)).thenReturn(true);

            assertThrows(
                    BusinessException.class,
                    () -> fornecedorService.update(1L, request)
            );
        }

        @Test
        @DisplayName("Deve permitir manter mesmo CPF/CNPJ ao atualizar")
        void devePermitirManterMesmoDocumentoAoAtualizar() {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678000199")
                    .tipoPessoa(TipoPessoa.JURIDICA)
                    .nome("Mesmo CNPJ")
                    .email("mesmo@email.com")
                    .cep("01001000")
                    .build();

            when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedorPJ));
            when(fornecedorRepository.existsByCpfCnpjAndIdNot("12345678000199", 1L)).thenReturn(false);
            when(cepService.consultarCep(anyString())).thenReturn(cepValido);
            when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedorPJ);

            FornecedorDTO.Response result = fornecedorService.update(1L, request);

            assertNotNull(result);
        }
    }

    // VALIDAÇÃO DE CEP
    @Nested
    @DisplayName("Validação de CEP")
    class ValidacaoCep {

        @Test
        @DisplayName("Deve rejeitar fornecedor com CEP inválido")
        void deveRejeitarCepInvalido() {
            var request = FornecedorDTO.Request.builder()
                    .cpfCnpj("12345678000199")
                    .tipoPessoa(TipoPessoa.JURIDICA)
                    .nome("CEP Inválido")
                    .email("cep@email.com")
                    .cep("00000000")
                    .build();

            when(fornecedorRepository.existsByCpfCnpj(anyString())).thenReturn(false);
            when(cepService.consultarCep("00000000")).thenReturn(
                    CepDTO.builder().cep("00000000").valido(false).mensagem("CEP não encontrado").build()
            );

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> fornecedorService.create(request)
            );

            assertTrue(ex.getMessage().contains("CEP inválido"));
        }
    }

    // BUSCAR E FILTRAR
    @Nested
    @DisplayName("Buscar e Filtrar")
    class BuscarFiltrar {

        @Test
        @DisplayName("Deve buscar fornecedor por ID")
        void deveBuscarPorId() {
            when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedorPJ));

            FornecedorDTO.Response result = fornecedorService.findById(1L);

            assertNotNull(result);
            assertEquals("Fornecedor PJ Ltda", result.getNome());
        }

        @Test
        @DisplayName("Deve lançar exceção quando fornecedor não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> fornecedorService.findById(99L)
            );
        }

        @Test
        @DisplayName("Deve listar fornecedores com filtros e paginação")
        void deveListarComFiltros() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Fornecedor> page = new PageImpl<>(List.of(fornecedorPJ, fornecedorPF), pageable, 2);
            when(fornecedorRepository.findByFilters(anyString(), anyString(), any(Pageable.class)))
                    .thenReturn(page);

            var result = fornecedorService.findAll("", "", pageable);

            assertEquals(2, result.getTotalElements());
            assertEquals(2, result.getContent().size());
        }

        @Test
        @DisplayName("Deve filtrar por nome")
        void deveFiltrarPorNome() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Fornecedor> page = new PageImpl<>(List.of(fornecedorPF), pageable, 1);
            when(fornecedorRepository.findByFilters(eq("João"), anyString(), any(Pageable.class)))
                    .thenReturn(page);

            var result = fornecedorService.findAll("João", "", pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("João Silva", result.getContent().get(0).getNome());
        }
    }

    // EXCLUIR FORNECEDOR
    @Nested
    @DisplayName("Excluir Fornecedor")
    class ExcluirFornecedor {

        @Test
        @DisplayName("Deve excluir fornecedor com sucesso")
        void deveExcluirComSucesso() {
            when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedorPJ));

            assertDoesNotThrow(() -> fornecedorService.delete(1L));
            verify(fornecedorRepository).delete(fornecedorPJ);
        }

        @Test
        @DisplayName("Deve lançar exceção ao excluir fornecedor inexistente")
        void deveLancarExcecaoAoExcluirInexistente() {
            when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> fornecedorService.delete(99L)
            );
        }
    }
}
