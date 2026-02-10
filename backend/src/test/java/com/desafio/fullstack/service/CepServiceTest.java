package com.desafio.fullstack.service;

import com.desafio.fullstack.dto.CepDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CepServiceTest {

    private final CepService cepService = new CepService();

    @Test
    @DisplayName("Deve rejeitar CEP com menos de 8 dígitos")
    void deveRejeitarCepCurto() {
        CepDTO result = cepService.consultarCep("1234");

        assertFalse(result.isValido());
        assertTrue(result.getMensagem().contains("8 dígitos"));
    }

    @Test
    @DisplayName("Deve rejeitar CEP com letras (limpar e validar tamanho)")
    void deveRejeitarCepComLetras() {
        CepDTO result = cepService.consultarCep("ABCDEFGH");

        assertFalse(result.isValido());
    }

    @Test
    @DisplayName("Deve limpar caracteres não numéricos antes de validar")
    void deveLimparCaracteres() {
        // CEP com máscara: 01001-000 => limpa para 01001000
        CepDTO result = cepService.consultarCep("01001-000");

        // dependendo da rede, mas não deve dar erro de formato
        assertNotNull(result);
        assertEquals("01001000", result.getCep());
    }

    @Test
    @DisplayName("Deve rejeitar CEP vazio")
    void deveRejeitarCepVazio() {
        CepDTO result = cepService.consultarCep("");

        assertFalse(result.isValido());
    }
}
