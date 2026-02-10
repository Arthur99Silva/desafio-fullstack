package com.desafio.fullstack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class EmpresaDTO {

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "CNPJ é obrigatório")
        @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos numéricos")
        private String cnpj;

        @NotBlank(message = "Nome Fantasia é obrigatório")
        @Size(max = 200, message = "Nome Fantasia deve ter no máximo 200 caracteres")
        private String nomeFantasia;

        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos numéricos")
        private String cep;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String cnpj;
        private String nomeFantasia;
        private String cep;
        private String logradouro;
        private String bairro;
        private String cidade;
        private String uf;
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
        private List<FornecedorDTO.ResponseSimple> fornecedores;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class ResponseSimple {
        private Long id;
        private String cnpj;
        private String nomeFantasia;
        private String cep;
        private String cidade;
        private String uf;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class VincularFornecedor {
        private Long fornecedorId;
    }
}
