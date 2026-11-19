package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.model.Fonte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository: acesso ao banco de fontes oficiais (Spring Data JPA gera o SQL)
@Repository
public interface FonteRepository extends JpaRepository<Fonte, Long> {
    Optional<Fonte> findByUrl(String url);

    List<Fonte> findAllByOrderByTituloAsc();
}
