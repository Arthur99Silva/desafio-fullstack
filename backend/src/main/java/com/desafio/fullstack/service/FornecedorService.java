package com.desafio.fullstack.service;

import com.desafio.fullstack.dto.CepDTO;
import com.desafio.fullstack.dto.EmpresaDTO;
import com.desafio.fullstack.dto.FornecedorDTO;
import com.desafio.fullstack.dto.PageResponse;
import com.desafio.fullstack.entity.Fornecedor;
import com.desafio.fullstack.enums.TipoPessoa;
import com.desafio.fullstack.exception.BusinessException;
import com.desafio.fullstack.exception.ResourceNotFoundException;
import com.desafio.fullstack.repository.FornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final CepService cepService;

    @Transactional(readOnly = true)
    public PageResponse<FornecedorDTO.Response> findAll(String nome, String cpfCnpj, Pageable pageable) {
        Page<Fornecedor> page = fornecedorRepository.findByFilters(nome, cpfCnpj, pageable);
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public FornecedorDTO.Response findById(Long id) {
        Fornecedor fornecedor = getFornecedorOrThrow(id);
        return toResponse(fornecedor);
    }

    @Transactional
    public FornecedorDTO.Response create(FornecedorDTO.Request request) {
        validarRequest(request, null);

        CepDTO cepInfo = cepService.consultarCep(request.getCep());
        if (!cepInfo.isValido()) {
            throw new BusinessException("CEP inválido: " + cepInfo.getMensagem());
        }

        Fornecedor fornecedor = Fornecedor.builder()
            .cpfCnpj(request.getCpfCnpj())
            .tipoPessoa(request.getTipoPessoa())
            .nome(request.getNome())
            .email(request.getEmail())
            .cep(request.getCep())
            .rg(request.getRg())
            .dataNascimento(request.getDataNascimento())
            .logradouro(cepInfo.getLogradouro())
            .bairro(cepInfo.getBairro())
            .cidade(cepInfo.getCidade())
            .uf(cepInfo.getUf())
            .build();

        fornecedor = fornecedorRepository.save(fornecedor);
        return toResponse(fornecedor);
    }

    @Transactional
    public FornecedorDTO.Response update(Long id, FornecedorDTO.Request request) {
        Fornecedor fornecedor = getFornecedorOrThrow(id);
        validarRequest(request, id);

        CepDTO cepInfo = cepService.consultarCep(request.getCep());
        if (!cepInfo.isValido()) {
            throw new BusinessException("CEP inválido: " + cepInfo.getMensagem());
        }

        fornecedor.setCpfCnpj(request.getCpfCnpj());
        fornecedor.setTipoPessoa(request.getTipoPessoa());
        fornecedor.setNome(request.getNome());
        fornecedor.setEmail(request.getEmail());
        fornecedor.setCep(request.getCep());
        fornecedor.setRg(request.getRg());
        fornecedor.setDataNascimento(request.getDataNascimento());
        fornecedor.setLogradouro(cepInfo.getLogradouro());
        fornecedor.setBairro(cepInfo.getBairro());
        fornecedor.setCidade(cepInfo.getCidade());
        fornecedor.setUf(cepInfo.getUf());

        fornecedor = fornecedorRepository.save(fornecedor);
        return toResponse(fornecedor);
    }

    @Transactional
    public void delete(Long id) {
        Fornecedor fornecedor = getFornecedorOrThrow(id);
        fornecedor.getEmpresas().forEach(e -> e.getFornecedores().remove(fornecedor));
        fornecedorRepository.delete(fornecedor);
    }

    private void validarRequest(FornecedorDTO.Request request, Long idAtual) {
        if (idAtual == null && fornecedorRepository.existsByCpfCnpj(request.getCpfCnpj())) {
            throw new BusinessException("CPF/CNPJ já cadastrado: " + request.getCpfCnpj());
        }
        if (idAtual != null && fornecedorRepository.existsByCpfCnpjAndIdNot(request.getCpfCnpj(), idAtual)) {
            throw new BusinessException("CPF/CNPJ já cadastrado por outro fornecedor: " + request.getCpfCnpj());
        }

        if (request.getTipoPessoa() == TipoPessoa.FISICA) {
            if (request.getCpfCnpj().length() != 11) {
                throw new BusinessException("Pessoa Física deve informar CPF com 11 dígitos");
            }
            if (request.getRg() == null || request.getRg().isBlank()) {
                throw new BusinessException("RG é obrigatório para Pessoa Física");
            }
            if (request.getDataNascimento() == null) {
                throw new BusinessException("Data de Nascimento é obrigatória para Pessoa Física");
            }
        } else {
            if (request.getCpfCnpj().length() != 14) {
                throw new BusinessException("Pessoa Jurídica deve informar CNPJ com 14 dígitos");
            }
        }
    }


    private Fornecedor getFornecedorOrThrow(Long id) {
        return fornecedorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id));
    }

    private FornecedorDTO.Response toResponse(Fornecedor f) {
        return FornecedorDTO.Response.builder()
            .id(f.getId())
            .cpfCnpj(f.getCpfCnpj())
            .tipoPessoa(f.getTipoPessoa())
            .nome(f.getNome())
            .email(f.getEmail())
            .cep(f.getCep())
            .rg(f.getRg())
            .dataNascimento(f.getDataNascimento())
            .logradouro(f.getLogradouro())
            .bairro(f.getBairro())
            .cidade(f.getCidade())
            .uf(f.getUf())
            .criadoEm(f.getCriadoEm())
            .atualizadoEm(f.getAtualizadoEm())
            .empresas(
                f.getEmpresas().stream()
                    .map(e -> EmpresaDTO.ResponseSimple.builder()
                        .id(e.getId())
                        .cnpj(e.getCnpj())
                        .nomeFantasia(e.getNomeFantasia())
                        .cep(e.getCep())
                        .cidade(e.getCidade())
                        .uf(e.getUf())
                        .build())
                    .collect(Collectors.toList())
            )
            .build();
    }

    private PageResponse<FornecedorDTO.Response> buildPageResponse(Page<Fornecedor> page) {
        return PageResponse.<FornecedorDTO.Response>builder()
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
