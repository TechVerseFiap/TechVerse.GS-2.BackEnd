package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.model.Fonte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Repository: acesso ao banco de fontes oficiais (Spring Data JPA gera o SQL)
@Repository
public interface FonteRepository extends JpaRepository<Fonte, Long> {
    Optional<Fonte> findByUrl(String url);

    List<Fonte> findAllByOrderByTituloAsc();

    // Fontes do catálogo do gov.br: quantas são e a data de consulta mais antiga (diz se o catálogo precisa ser atualizado)
    long countByUrlStartingWith(String prefixo);

    @Query("SELECT MIN(f.dataConsulta) FROM Fonte f WHERE f.url LIKE CONCAT(:prefixo, '%')")
    LocalDate dataConsultaMaisAntiga(@Param("prefixo") String prefixo);
}
