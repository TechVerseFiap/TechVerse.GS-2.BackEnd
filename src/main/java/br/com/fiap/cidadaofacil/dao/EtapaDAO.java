package br.com.fiap.cidadaofacil.dao;

import br.com.fiap.cidadaofacil.model.Etapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// DAO: acesso ao banco de etapas (Spring Data JPA gera o SQL)
@Repository
public interface EtapaDAO extends JpaRepository<Etapa, Long> {
    List<Etapa> findByServicoIdOrderByOrdemAsc(Long servicoId);

    boolean existsByServicoIdAndOrdem(Long servicoId, Integer ordem);

    boolean existsByServicoIdAndOrdemAndIdNot(Long servicoId, Integer ordem, Long id);
}
