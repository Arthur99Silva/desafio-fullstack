package com.desafio.fullstack.controller;

import com.desafio.fullstack.dto.CepDTO;
import com.desafio.fullstack.service.CepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cep")
@RequiredArgsConstructor
@Tag(name = "CEP", description = "Consulta e validação de CEP")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class CepController {

    private final CepService cepService;

    @GetMapping("/{cep}")
    public ResponseEntity<CepDTO> consultar(@PathVariable String cep) {
        return ResponseEntity.ok(cepService.consultarCep(cep));
    }
}
