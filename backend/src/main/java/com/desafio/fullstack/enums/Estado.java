package com.desafio.fullstack.enums;

import java.util.Arrays;

public enum Estado {
    RO("Rondônia", true),
    AC("Acre", true),
    AM("Amazonas", true),
    RR("Roraima", true),
    PA("Pará", true),
    AP("Amapá", true),
    TO("Tocantins", true),
    MA("Maranhão", true),
    PI("Piauí", true),
    CE("Ceará", true),
    RN("Rio Grande do Norte", true),
    PB("Paraíba", true),
    PE("Pernambuco", true),
    AL("Alagoas", true),
    SE("Sergipe", true),
    BA("Bahia", true),
    MG("Minas Gerais", true),
    ES("Espírito Santo", true),
    RJ("Rio de Janeiro", true),
    SP("São Paulo", true),
    PR("Paraná", false),
    SC("Santa Catarina", true),
    RS("Rio Grande do Sul", true),
    MS("Mato Grosso do Sul", true),
    MT("Mato Grosso", true),
    GO("Goiás", true),
    DF("Distrito Federal", true);

    private final String nome;
    private final boolean permiteMenorIdade;

    Estado(String nome, boolean permiteMenorIdade) {
        this.nome = nome;
        this.permiteMenorIdade = permiteMenorIdade;
    }

    public boolean permiteMenorIdade() {
        return permiteMenorIdade;
    }

    public String getNome() {
        return nome;
    }

    public static Estado fromSigla(String sigla) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(sigla))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("UF inválida: " + sigla));
    }
}