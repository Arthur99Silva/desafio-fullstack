package com.desafio.fullstack.entity;

import com.desafio.fullstack.enums.TipoPessoa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    @DisplayName("Empresa deve manter relacionamento bidirecional N:N com Fornecedor")
    void empresaDeveManterRelacionamentoComFornecedor() {
        Empresa empresa = Empresa.builder()
                .id(1L)
                .cnpj("12345678000199")
                .nomeFantasia("Empresa Teste")
                .cep("80000000")
                .fornecedores(new HashSet<>())
                .build();

        Fornecedor fornecedor = Fornecedor.builder()
                .id(1L)
                .cpfCnpj("12345678901")
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("Fornecedor")
                .email("f@email.com")
                .cep("01001000")
                .empresas(new HashSet<>())
                .build();

        empresa.getFornecedores().add(fornecedor);

        assertTrue(empresa.getFornecedores().contains(fornecedor));
        assertEquals(1, empresa.getFornecedores().size());
    }

    @Test
    @DisplayName("Empresa pode ter múltiplos fornecedores")
    void empresaPodeTerMultiplosFornecedores() {
        Empresa empresa = Empresa.builder()
                .id(1L).cnpj("12345678000199").nomeFantasia("Teste")
                .cep("80000000").fornecedores(new HashSet<>()).build();

        for (int i = 1; i <= 5; i++) {
            empresa.getFornecedores().add(
                    Fornecedor.builder()
                            .id((long) i)
                            .cpfCnpj(String.format("%011d", i))
                            .tipoPessoa(TipoPessoa.FISICA)
                            .nome("Fornecedor " + i)
                            .email("f" + i + "@email.com")
                            .cep("01001000")
                            .empresas(new HashSet<>())
                            .build()
            );
        }

        assertEquals(5, empresa.getFornecedores().size());
    }

    @Test
    @DisplayName("Fornecedor PF deve ter RG e Data de Nascimento preenchidos")
    void fornecedorPFDeveTerRgEDataNascimento() {
        Fornecedor pf = Fornecedor.builder()
                .id(1L)
                .cpfCnpj("12345678901")
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("Pessoa Física")
                .email("pf@email.com")
                .cep("01001000")
                .rg("123456789")
                .dataNascimento(LocalDate.of(1990, 5, 15))
                .empresas(new HashSet<>())
                .build();

        assertNotNull(pf.getRg());
        assertNotNull(pf.getDataNascimento());
        assertEquals(TipoPessoa.FISICA, pf.getTipoPessoa());
    }

    @Test
    @DisplayName("Deve calcular idade corretamente a partir da data de nascimento")
    void deveCalcularIdadeCorretamente() {
        LocalDate nascimento16anos = LocalDate.now().minusYears(16);
        LocalDate nascimento18anos = LocalDate.now().minusYears(18);
        LocalDate nascimento25anos = LocalDate.now().minusYears(25);

        assertEquals(16, Period.between(nascimento16anos, LocalDate.now()).getYears());
        assertEquals(18, Period.between(nascimento18anos, LocalDate.now()).getYears());
        assertEquals(25, Period.between(nascimento25anos, LocalDate.now()).getYears());
    }

    @Test
    @DisplayName("Fornecedor PJ não precisa de RG e Data de Nascimento")
    void fornecedorPJNaoPrecisaDeRgENascimento() {
        Fornecedor pj = Fornecedor.builder()
                .id(1L)
                .cpfCnpj("12345678000199")
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Empresa PJ")
                .email("pj@email.com")
                .cep("01001000")
                .empresas(new HashSet<>())
                .build();

        assertNull(pj.getRg());
        assertNull(pj.getDataNascimento());
        assertEquals(TipoPessoa.JURIDICA, pj.getTipoPessoa());
    }

    @Test
    @DisplayName("TipoPessoa enum deve ter FISICA e JURIDICA")
    void tipoPessoaDeveConterValoresCorretos() {
        assertEquals(2, TipoPessoa.values().length);
        assertNotNull(TipoPessoa.valueOf("FISICA"));
        assertNotNull(TipoPessoa.valueOf("JURIDICA"));
    }
}
