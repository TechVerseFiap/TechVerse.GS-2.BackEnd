package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.model.Indicador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

// Repository: acesso ao banco de indicadores (Spring Data JPA gera o SQL)
@Repository
public interface IndicadorRepository extends JpaRepository<Indicador, Long> {
    List<Indicador> findAllByOrderByIdAsc();

    long countByFonteId(Long fonteId);

    // Data da consulta mais antiga entre as fontes dos indicadores: diz se o panorama precisa ser atualizado
    @Query("SELECT MIN(i.fonte.dataConsulta) FROM Indicador i")
    LocalDate dataConsultaMaisAntiga();
}
