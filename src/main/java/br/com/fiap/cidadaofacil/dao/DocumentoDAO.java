package br.com.fiap.cidadaofacil.dao;

import br.com.fiap.cidadaofacil.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// DAO: acesso ao banco de documentos (Spring Data JPA gera o SQL)
@Repository
public interface DocumentoDAO extends JpaRepository<Documento, Long> {
    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<Documento> findByNomeIgnoreCase(String nome);

    List<Documento> findAllByOrderByNomeAsc();
}
