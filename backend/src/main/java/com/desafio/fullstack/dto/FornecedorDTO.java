package com.desafio.fullstack.dto;

import com.desafio.fullstack.enums.TipoPessoa;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FornecedorDTO {

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "CPF/CNPJ é obrigatório")
        @Pattern(regexp = "\\d{11}|\\d{14}", message = "CPF deve ter 11 dígitos ou CNPJ 14 dígitos")
        private String cpfCnpj;

        @NotNull(message = "Tipo de pessoa é obrigatório")
        private TipoPessoa tipoPessoa;

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
        private String nome;

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        private String email;

        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos numéricos")
        private String cep;

        // Dados pessoa fisica
        private String rg;
        private LocalDate dataNascimento;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String cpfCnpj;
        private TipoPessoa tipoPessoa;
        private String nome;
        private String email;
        private String cep;
        private String rg;
        private LocalDate dataNascimento;
        private String logradouro;
        private String bairro;
        private String cidade;
        private String uf;
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
        private List<EmpresaDTO.ResponseSimple> empresas;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class ResponseSimple {
        private Long id;
        private String cpfCnpj;
        private TipoPessoa tipoPessoa;
        private String nome;
        private String email;
    }
}
