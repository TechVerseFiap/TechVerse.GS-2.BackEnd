package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.model.Orgao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository: acesso ao banco de órgãos (Spring Data JPA gera o SQL)
@Repository
public interface OrgaoRepository extends JpaRepository<Orgao, Long> {
    List<Orgao> findAllByOrderByNomeAsc();

    Optional<Orgao> findFirstByNomeIgnoreCaseOrSiglaIgnoreCase(String nome, String sigla);

    boolean existsBySiglaIgnoreCase(String sigla);

    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);
}
