package br.com.fiap.cidadaofacil.dao;

import br.com.fiap.cidadaofacil.model.Interacao;
import br.com.fiap.cidadaofacil.model.ResultadoInteracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// DAO: acesso ao banco de interações do assistente (Spring Data JPA gera o SQL)
@Repository
public interface InteracaoDAO extends JpaRepository<Interacao, Long> {
    List<Interacao> findAllByOrderByDataHoraDesc();

    long countByResultado(ResultadoInteracao resultado);
}
