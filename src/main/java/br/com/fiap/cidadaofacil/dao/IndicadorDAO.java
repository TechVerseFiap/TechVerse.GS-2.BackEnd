package br.com.fiap.cidadaofacil.dao;

import br.com.fiap.cidadaofacil.model.Indicador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// DAO: acesso ao banco de indicadores (Spring Data JPA gera o SQL)
@Repository
public interface IndicadorDAO extends JpaRepository<Indicador, Long> {
    List<Indicador> findAllByOrderByIdAsc();

    long countByFonteId(Long fonteId);
}
