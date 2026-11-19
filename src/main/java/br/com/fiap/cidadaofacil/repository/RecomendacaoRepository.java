package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.model.Recomendacao;
import br.com.fiap.cidadaofacil.model.RecomendacaoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repository: acesso ao banco de recomendações (Spring Data JPA gera o SQL)
@Repository
public interface RecomendacaoRepository extends JpaRepository<Recomendacao, RecomendacaoId> {
    boolean existsByServicoId(Long servicoId);
}
