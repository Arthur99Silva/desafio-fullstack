package com.desafio.fullstack.repository;

import com.desafio.fullstack.entity.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    Optional<Fornecedor> findByCpfCnpj(String cpfCnpj);

    boolean existsByCpfCnpj(String cpfCnpj);

    boolean existsByCpfCnpjAndIdNot(String cpfCnpj, Long id);

    @Query("SELECT f FROM Fornecedor f WHERE " +
           "(:nome IS NULL OR :nome = '' OR LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
           "(:cpfCnpj IS NULL OR :cpfCnpj = '' OR f.cpfCnpj LIKE CONCAT('%', :cpfCnpj, '%'))")
    Page<Fornecedor> findByFilters(
        @Param("nome") String nome,
        @Param("cpfCnpj") String cpfCnpj,
        Pageable pageable
    );
}
