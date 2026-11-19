package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.model.Servico;
import br.com.fiap.cidadaofacil.model.StatusServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository: acesso ao banco de serviços (Spring Data JPA gera o SQL)
@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByCategoriaIdOrderByNomeAsc(Long categoriaId);

    List<Servico> findByCategoriaIdAndStatusOrderByNomeAsc(Long categoriaId, StatusServico status);

    List<Servico> findAllByOrderByNomeAsc();

    List<Servico> findByStatus(StatusServico status);

    long countByCategoriaId(Long categoriaId);

    long countByOrgaoId(Long orgaoId);

    long countByFonteId(Long fonteId);

    // Busca por texto no nome/descrição, com filtro opcional de categoria
    @Query("""
            SELECT s FROM Servico s
            WHERE (:categoriaId IS NULL OR s.categoria.id = :categoriaId)
              AND (LOWER(s.nome) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(s.descricao) LIKE LOWER(CONCAT('%', :texto, '%')))
            ORDER BY s.nome
            """)
    List<Servico> buscarPorTexto(@Param("texto") String texto, @Param("categoriaId") Long categoriaId);
}
