package com.desafio.fullstack.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CepDTO {
    private String cep;
    private String uf;
    private String cidade;
    private String bairro;
    private String logradouro;
    private boolean valido;
    private String mensagem;
}
