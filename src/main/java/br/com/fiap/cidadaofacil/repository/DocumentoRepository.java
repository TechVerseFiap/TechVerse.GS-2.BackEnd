package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository: acesso ao banco de documentos (Spring Data JPA gera o SQL)
@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<Documento> findByNomeIgnoreCase(String nome);

    List<Documento> findAllByOrderByNomeAsc();
}
