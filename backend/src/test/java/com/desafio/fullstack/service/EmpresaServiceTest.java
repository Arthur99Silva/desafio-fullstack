package com.desafio.fullstack.service;

import com.desafio.fullstack.dto.CepDTO;
import com.desafio.fullstack.dto.EmpresaDTO;
import com.desafio.fullstack.entity.Empresa;
import com.desafio.fullstack.entity.Fornecedor;
import com.desafio.fullstack.enums.TipoPessoa;
import com.desafio.fullstack.exception.BusinessException;
import com.desafio.fullstack.exception.ResourceNotFoundException;
import com.desafio.fullstack.repository.EmpresaRepository;
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
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private CepService cepService;

    @InjectMocks
    private EmpresaService empresaService;

    private Empresa empresa;
    private EmpresaDTO.Request requestValido;
    private CepDTO cepValido;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .cnpj("12345678000199")
                .nomeFantasia("Empresa Teste")
                .cep("80000000")
                .uf("PR")
                .cidade("Curitiba")
                .bairro("Centro")
                .logradouro("Rua XV de Novembro")
                .criadoEm(LocalDateTime.now())
                .fornecedores(new HashSet<>())
                .build();

        requestValido = EmpresaDTO.Request.builder()
                .cnpj("12345678000199")
                .nomeFantasia("Empresa Teste")
                .cep("80000000")
                .build();

        cepValido = CepDTO.builder()
                .cep("80000000")
                .uf("PR")
                .cidade("Curitiba")
                .bairro("Centro")
                .logradouro("Rua XV de Novembro")
                .valido(true)
                .build();
    }

    // CRIAR EMPRESA
    @Nested
    @DisplayName("Criar Empresa")
    class CriarEmpresa {

        @Test
        void deveCriarEmpresaComSucesso() {
            when(empresaRepository.existsByCnpj(anyString())).thenReturn(false);
            when(cepService.consultarCep(anyString())).thenReturn(cepValido);
            when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

            EmpresaDTO.Response result = empresaService.create(requestValido);

            assertNotNull(result);
            assertEquals("12345678000199", result.getCnpj());
            assertEquals("Empresa Teste", result.getNomeFantasia());
            assertEquals("PR", result.getUf());
            verify(empresaRepository).save(any(Empresa.class));
        }

        @Test
        void deveLancarExcecaoQuandoCnpjDuplicado() {
            when(empresaRepository.existsByCnpj("12345678000199")).thenReturn(true);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> empresaService.create(requestValido)
            );

            assertTrue(ex.getMessage().contains("CNPJ já cadastrado"));
            verify(empresaRepository, never()).save(any());
        }

        @Test
        void deveLancarExcecaoQuandoCepInvalido() {
            when(empresaRepository.existsByCnpj(anyString())).thenReturn(false);
            when(cepService.consultarCep("80000000")).thenReturn(
                    CepDTO.builder().cep("80000000").valido(false).mensagem("CEP não encontrado").build()
            );

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> empresaService.create(requestValido)
            );

            assertTrue(ex.getMessage().contains("CEP inválido"));
            verify(empresaRepository, never()).save(any());
        }

        @Test
        void devePreencherEnderecoDoCep() {
            when(empresaRepository.existsByCnpj(anyString())).thenReturn(false);
            when(cepService.consultarCep(anyString())).thenReturn(cepValido);
            when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> {
                Empresa saved = inv.getArgument(0);
                saved.setId(1L);
                saved.setCriadoEm(LocalDateTime.now());
                return saved;
            });

            EmpresaDTO.Response result = empresaService.create(requestValido);

            assertEquals("Curitiba", result.getCidade());
            assertEquals("PR", result.getUf());
            assertEquals("Centro", result.getBairro());
        }
    }

    // ATUALIZAR EMPRESA
    @Nested
    @DisplayName("Atualizar Empresa")
    class AtualizarEmpresa {

        @Test
        void deveAtualizarEmpresaComSucesso() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(empresaRepository.existsByCnpjAndIdNot(anyString(), eq(1L))).thenReturn(false);
            when(cepService.consultarCep(anyString())).thenReturn(cepValido);
            when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

            EmpresaDTO.Request updateRequest = EmpresaDTO.Request.builder()
                    .cnpj("12345678000199")
                    .nomeFantasia("Empresa Atualizada")
                    .cep("80000000")
                    .build();

            EmpresaDTO.Response result = empresaService.update(1L, updateRequest);

            assertNotNull(result);
            verify(empresaRepository).save(any(Empresa.class));
        }

        @Test
        void deveLancarExcecaoQuandoEmpresaNaoExiste() {
            when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> empresaService.update(99L, requestValido)
            );
        }

        @Test
        void deveLancarExcecaoQuandoCnpjDeOutraEmpresa() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(empresaRepository.existsByCnpjAndIdNot("12345678000199", 1L)).thenReturn(true);

            assertThrows(
                    BusinessException.class,
                    () -> empresaService.update(1L, requestValido)
            );
        }
    }

    // BUSCAR EMPRESA
    @Nested
    @DisplayName("Buscar Empresa")
    class BuscarEmpresa {

        @Test
        void deveBuscarPorIdComSucesso() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

            EmpresaDTO.Response result = empresaService.findById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Empresa Teste", result.getNomeFantasia());
        }

        @Test
        void deveLancarExcecaoQuandoNaoEncontrada() {
            when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> empresaService.findById(99L)
            );
        }

        @Test
        void deveListarComPaginacao() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Empresa> page = new PageImpl<>(List.of(empresa), pageable, 1);
            when(empresaRepository.findBySearch(anyString(), any(Pageable.class))).thenReturn(page);

            var result = empresaService.findAll("", pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
        }
    }

    // EXCLUIR EMPRESA
    @Nested
    class ExcluirEmpresa {

        @Test
        void deveExcluirComSucesso() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

            assertDoesNotThrow(() -> empresaService.delete(1L));
            verify(empresaRepository).delete(empresa);
        }

        @Test
        @DisplayName("Deve lançar exceção ao excluir empresa inexistente")
        void deveLancarExcecaoAoExcluirInexistente() {
            when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> empresaService.delete(99L)
            );
        }
    }

    // VINCULAR / DESVINCULAR FORNECEDOR
    @Nested
    class VincularFornecedor {

        private Fornecedor fornecedorMaior;
        private Fornecedor fornecedorMenor;
        private Fornecedor fornecedorPJ;

        @BeforeEach
        void setUp() {
            fornecedorMaior = Fornecedor.builder()
                    .id(1L)
                    .cpfCnpj("12345678901")
                    .tipoPessoa(TipoPessoa.FISICA)
                    .nome("Fornecedor Maior")
                    .email("maior@email.com")
                    .cep("01001000")
                    .rg("123456789")
                    .dataNascimento(LocalDate.now().minusYears(25))
                    .empresas(new HashSet<>())
                    .build();

            fornecedorMenor = Fornecedor.builder()
                    .id(2L)
                    .cpfCnpj("98765432101")
                    .tipoPessoa(TipoPessoa.FISICA)
                    .nome("Fornecedor Menor")
                    .email("menor@email.com")
                    .cep("01001000")
                    .rg("987654321")
                    .dataNascimento(LocalDate.now().minusYears(16))
                    .empresas(new HashSet<>())
                    .build();

            fornecedorPJ = Fornecedor.builder()
                    .id(3L)
                    .cpfCnpj("98765432000188")
                    .tipoPessoa(TipoPessoa.JURIDICA)
                    .nome("Fornecedor PJ")
                    .email("pj@email.com")
                    .cep("01001000")
                    .empresas(new HashSet<>())
                    .build();
        }

        @Test
        void deveVincularFornecedorMaiorComSucesso() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedorMaior));
            when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

            EmpresaDTO.Response result = empresaService.vincularFornecedor(1L, 1L);

            assertNotNull(result);
            verify(empresaRepository).save(empresa);
        }

        @Test
        void deveBloquearMenorDeIdadeNoParana() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa)); // UF = PR
            when(fornecedorRepository.findById(2L)).thenReturn(Optional.of(fornecedorMenor));

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> empresaService.vincularFornecedor(1L, 2L)
            );

            assertTrue(ex.getMessage().contains("Paraná"));
            assertTrue(ex.getMessage().contains("menores de idade"));
            verify(empresaRepository, never()).save(any());
        }

        @Test
        void devePermitirMenorForaDoParana() {
            Empresa empresaSP = Empresa.builder()
                    .id(2L).cnpj("11111111000111").nomeFantasia("Empresa SP")
                    .cep("01001000").uf("SP").cidade("São Paulo")
                    .criadoEm(LocalDateTime.now()).fornecedores(new HashSet<>())
                    .build();

            when(empresaRepository.findById(2L)).thenReturn(Optional.of(empresaSP));
            when(fornecedorRepository.findById(2L)).thenReturn(Optional.of(fornecedorMenor));
            when(empresaRepository.save(any(Empresa.class))).thenReturn(empresaSP);

            EmpresaDTO.Response result = empresaService.vincularFornecedor(2L, 2L);

            assertNotNull(result);
            verify(empresaRepository).save(empresaSP);
        }

        @Test
        void devePermitirPJNoParana() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa)); // UF = PR
            when(fornecedorRepository.findById(3L)).thenReturn(Optional.of(fornecedorPJ));
            when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

            EmpresaDTO.Response result = empresaService.vincularFornecedor(1L, 3L);

            assertNotNull(result);
        }

        @Test
        void deveLancarExcecaoEmpresaNaoEncontrada() {
            when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> empresaService.vincularFornecedor(99L, 1L)
            );
        }

        @Test
        void deveLancarExcecaoFornecedorNaoEncontrado() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> empresaService.vincularFornecedor(1L, 99L)
            );
        }

        @Test
        void deveDesvincularFornecedorComSucesso() {
            empresa.getFornecedores().add(fornecedorMaior);

            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedorMaior));
            when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

            EmpresaDTO.Response result = empresaService.desvincularFornecedor(1L, 1L);

            assertNotNull(result);
            verify(empresaRepository).save(empresa);
        }
    }
}
