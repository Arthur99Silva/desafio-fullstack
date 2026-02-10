package com.desafio.fullstack.service;

import com.desafio.fullstack.dto.CepDTO;
import com.desafio.fullstack.dto.EmpresaDTO;
import com.desafio.fullstack.dto.FornecedorDTO;
import com.desafio.fullstack.dto.PageResponse;
import com.desafio.fullstack.entity.Empresa;
import com.desafio.fullstack.entity.Fornecedor;
import com.desafio.fullstack.exception.BusinessException;
import com.desafio.fullstack.exception.ResourceNotFoundException;
import com.desafio.fullstack.repository.EmpresaRepository;
import com.desafio.fullstack.repository.FornecedorRepository;
import com.desafio.fullstack.enums.TipoPessoa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final CepService cepService;

    @Transactional(readOnly = true)
    public PageResponse<EmpresaDTO.Response> findAll(String search, Pageable pageable) {
        Page<Empresa> page = empresaRepository.findBySearch(search, pageable);
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public EmpresaDTO.Response findById(Long id) {
        Empresa empresa = getEmpresaOrThrow(id);
        return toResponse(empresa);
    }

    @Transactional
    public EmpresaDTO.Response create(EmpresaDTO.Request request) {
        // Validar CNPJ único
        if (empresaRepository.existsByCnpj(request.getCnpj())) {
            throw new BusinessException("CNPJ já cadastrado: " + request.getCnpj());
        }

        // Validar e consultar CEP
        CepDTO cepInfo = cepService.consultarCep(request.getCep());
        if (!cepInfo.isValido()) {
            throw new BusinessException("CEP inválido: " + cepInfo.getMensagem());
        }

        Empresa empresa = Empresa.builder()
            .cnpj(request.getCnpj())
            .nomeFantasia(request.getNomeFantasia())
            .cep(request.getCep())
            .logradouro(cepInfo.getLogradouro())
            .bairro(cepInfo.getBairro())
            .cidade(cepInfo.getCidade())
            .uf(cepInfo.getUf())
            .build();

        empresa = empresaRepository.save(empresa);
        return toResponse(empresa);
    }

    @Transactional
    public EmpresaDTO.Response update(Long id, EmpresaDTO.Request request) {
        Empresa empresa = getEmpresaOrThrow(id);

        // Validar CNPJ único (exceto o próprio)
        if (empresaRepository.existsByCnpjAndIdNot(request.getCnpj(), id)) {
            throw new BusinessException("CNPJ já cadastrado por outra empresa: " + request.getCnpj());
        }

        // Validar CEP
        CepDTO cepInfo = cepService.consultarCep(request.getCep());
        if (!cepInfo.isValido()) {
            throw new BusinessException("CEP inválido: " + cepInfo.getMensagem());
        }

        empresa.setCnpj(request.getCnpj());
        empresa.setNomeFantasia(request.getNomeFantasia());
        empresa.setCep(request.getCep());
        empresa.setLogradouro(cepInfo.getLogradouro());
        empresa.setBairro(cepInfo.getBairro());
        empresa.setCidade(cepInfo.getCidade());
        empresa.setUf(cepInfo.getUf());

        empresa = empresaRepository.save(empresa);
        return toResponse(empresa);
    }

    @Transactional
    public void delete(Long id) {
        Empresa empresa = getEmpresaOrThrow(id);
        empresa.getFornecedores().clear();
        empresaRepository.delete(empresa);
    }

    @Transactional
    public EmpresaDTO.Response vincularFornecedor(Long empresaId, Long fornecedorId) {
        Empresa empresa = getEmpresaOrThrow(empresaId);
        Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
            .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", fornecedorId));

        // Regra: empresa do PR não pode vincular PF menor de idade
        validarFornecedorMenorParana(empresa, fornecedor);

        empresa.getFornecedores().add(fornecedor);
        empresa = empresaRepository.save(empresa);
        return toResponse(empresa);
    }

    @Transactional
    public EmpresaDTO.Response desvincularFornecedor(Long empresaId, Long fornecedorId) {
        Empresa empresa = getEmpresaOrThrow(empresaId);
        Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
            .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", fornecedorId));

        empresa.getFornecedores().remove(fornecedor);
        empresa = empresaRepository.save(empresa);
        return toResponse(empresa);
    }

    // === Regras de negócio ===

    /**
     * Se a empresa é do Paraná (UF = "PR"), não pode vincular fornecedor
     * pessoa física menor de 18 anos.
     */
    private void validarFornecedorMenorParana(Empresa empresa, Fornecedor fornecedor) {
        if ("PR".equalsIgnoreCase(empresa.getUf())
            && fornecedor.getTipoPessoa() == TipoPessoa.FISICA
            && fornecedor.getDataNascimento() != null) {

            int idade = Period.between(fornecedor.getDataNascimento(), LocalDate.now()).getYears();
            if (idade < 18) {
                throw new BusinessException(
                    "Empresas do Paraná não podem cadastrar fornecedor pessoa física menor de idade. " +
                    "Idade do fornecedor: " + idade + " anos."
                );
            }
        }
    }

    // === Helpers ===

    private Empresa getEmpresaOrThrow(Long id) {
        return empresaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Empresa", id));
    }

    private EmpresaDTO.Response toResponse(Empresa empresa) {
        return EmpresaDTO.Response.builder()
            .id(empresa.getId())
            .cnpj(empresa.getCnpj())
            .nomeFantasia(empresa.getNomeFantasia())
            .cep(empresa.getCep())
            .logradouro(empresa.getLogradouro())
            .bairro(empresa.getBairro())
            .cidade(empresa.getCidade())
            .uf(empresa.getUf())
            .criadoEm(empresa.getCriadoEm())
            .atualizadoEm(empresa.getAtualizadoEm())
            .fornecedores(
                empresa.getFornecedores().stream()
                    .map(f -> FornecedorDTO.ResponseSimple.builder()
                        .id(f.getId())
                        .cpfCnpj(f.getCpfCnpj())
                        .tipoPessoa(f.getTipoPessoa())
                        .nome(f.getNome())
                        .email(f.getEmail())
                        .build())
                    .collect(Collectors.toList())
            )
            .build();
    }

    private PageResponse<EmpresaDTO.Response> buildPageResponse(Page<Empresa> page) {
        return PageResponse.<EmpresaDTO.Response>builder()
            .content(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
    }
}
