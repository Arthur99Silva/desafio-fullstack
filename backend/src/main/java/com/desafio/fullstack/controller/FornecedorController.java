package com.desafio.fullstack.controller;

import com.desafio.fullstack.dto.FornecedorDTO;
import com.desafio.fullstack.dto.PageResponse;
import com.desafio.fullstack.service.FornecedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
@Tag(name = "Fornecedores", description = "CRUD de Fornecedores")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @GetMapping
    public ResponseEntity<PageResponse<FornecedorDTO.Response>> findAll(
            @RequestParam(required = false, defaultValue = "") String nome,
            @RequestParam(required = false, defaultValue = "") String cpfCnpj,
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(fornecedorService.findAll(nome, cpfCnpj, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FornecedorDTO.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(fornecedorService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FornecedorDTO.Response> create(@Valid @RequestBody FornecedorDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fornecedorService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FornecedorDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody FornecedorDTO.Request request) {
        return ResponseEntity.ok(fornecedorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fornecedorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
