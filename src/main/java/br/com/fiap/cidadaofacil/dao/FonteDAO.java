package br.com.fiap.cidadaofacil.dao;

import br.com.fiap.cidadaofacil.model.Fonte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// DAO: acesso ao banco de fontes oficiais (Spring Data JPA gera o SQL)
@Repository
public interface FonteDAO extends JpaRepository<Fonte, Long> {
    Optional<Fonte> findByUrl(String url);

    List<Fonte> findAllByOrderByTituloAsc();
}
