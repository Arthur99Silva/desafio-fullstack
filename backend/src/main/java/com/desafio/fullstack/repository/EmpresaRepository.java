package com.desafio.fullstack.repository;

import com.desafio.fullstack.entity.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);

    @Query("SELECT e FROM Empresa e WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(e.nomeFantasia) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "e.cnpj LIKE CONCAT('%', :search, '%'))")
    Page<Empresa> findBySearch(@Param("search") String search, Pageable pageable);
}
