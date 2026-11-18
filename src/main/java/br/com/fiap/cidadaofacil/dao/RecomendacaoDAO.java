package br.com.fiap.cidadaofacil.dao;

import br.com.fiap.cidadaofacil.model.Recomendacao;
import br.com.fiap.cidadaofacil.model.RecomendacaoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// DAO: acesso ao banco de recomendações (Spring Data JPA gera o SQL)
@Repository
public interface RecomendacaoDAO extends JpaRepository<Recomendacao, RecomendacaoId> {
    boolean existsByServicoId(Long servicoId);
}
