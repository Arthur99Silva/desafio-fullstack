package com.desafio.fullstack.service;

import com.desafio.fullstack.dto.CepDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class CepService {

    private static final String CEP_API_URL = "https://viacep.com.br/ws/%s/json/";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CepService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public CepDTO consultarCep(String cep) {
        String cepLimpo = cep.replaceAll("\\D", "");

        if (cepLimpo.length() != 8) {
            return CepDTO.builder()
                .cep(cep)
                .valido(false)
                .mensagem("CEP deve conter 8 dígitos")
                .build();
        }

        try {
            CepDTO resultado = consultarCepLa(cepLimpo);
            if (resultado.isValido()) {
                return resultado;
            }

            return consultarViaCep(cepLimpo);
        } catch (Exception e) {
            try {
                return consultarViaCep(cepLimpo);
            } catch (Exception ex) {
                return CepDTO.builder()
                    .cep(cepLimpo)
                    .valido(false)
                    .mensagem("Não foi possível validar o CEP: " + ex.getMessage())
                    .build();
            }
        }
    }

    private CepDTO consultarCepLa(String cep) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://cep.la/" + cep))
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 && !response.body().isBlank()) {
            JsonNode node = objectMapper.readTree(response.body());
            if (node.isArray() && node.size() > 0) {
                node = node.get(0);
            }
            if (node.has("uf") && !node.get("uf").asText().isBlank()) {
                return CepDTO.builder()
                    .cep(cep)
                    .uf(node.path("uf").asText())
                    .cidade(node.path("cidade").asText())
                    .bairro(node.path("bairro").asText())
                    .logradouro(node.path("logradouro").asText())
                    .valido(true)
                    .build();
            }
        }

        return CepDTO.builder().cep(cep).valido(false).mensagem("CEP não encontrado na API cep.la").build();
    }

    private CepDTO consultarViaCep(String cep) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(String.format(CEP_API_URL, cep)))
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode node = objectMapper.readTree(response.body());

            if (node.has("erro") && node.get("erro").asBoolean()) {
                return CepDTO.builder()
                    .cep(cep)
                    .valido(false)
                    .mensagem("CEP não encontrado")
                    .build();
            }

            return CepDTO.builder()
                .cep(cep)
                .uf(node.path("uf").asText())
                .cidade(node.path("localidade").asText())
                .bairro(node.path("bairro").asText())
                .logradouro(node.path("logradouro").asText())
                .valido(true)
                .build();
        }

        return CepDTO.builder()
            .cep(cep)
            .valido(false)
            .mensagem("Erro ao consultar ViaCEP: HTTP " + response.statusCode())
            .build();
    }
}
