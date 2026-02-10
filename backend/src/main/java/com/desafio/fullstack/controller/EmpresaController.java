package com.desafio.fullstack.controller;

import com.desafio.fullstack.dto.EmpresaDTO;
import com.desafio.fullstack.dto.PageResponse;
import com.desafio.fullstack.service.EmpresaService;
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
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "CRUD de Empresas")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public ResponseEntity<PageResponse<EmpresaDTO.Response>> findAll(
            @RequestParam(required = false, defaultValue = "") String search,
            @PageableDefault(size = 10, sort = "nomeFantasia", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(empresaService.findAll(search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaDTO.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EmpresaDTO.Response> create(@Valid @RequestBody EmpresaDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaDTO.Request request) {
        return ResponseEntity.ok(empresaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        empresaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{empresaId}/fornecedores/{fornecedorId}")
    public ResponseEntity<EmpresaDTO.Response> vincularFornecedor(
            @PathVariable Long empresaId,
            @PathVariable Long fornecedorId) {
        return ResponseEntity.ok(empresaService.vincularFornecedor(empresaId, fornecedorId));
    }

    @DeleteMapping("/{empresaId}/fornecedores/{fornecedorId}")
    public ResponseEntity<EmpresaDTO.Response> desvincularFornecedor(
            @PathVariable Long empresaId,
            @PathVariable Long fornecedorId) {
        return ResponseEntity.ok(empresaService.desvincularFornecedor(empresaId, fornecedorId));
    }
}
