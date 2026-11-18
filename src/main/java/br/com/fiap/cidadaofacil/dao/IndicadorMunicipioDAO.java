package br.com.fiap.cidadaofacil.dao;

import br.com.fiap.cidadaofacil.model.IndicadorMunicipio;
import br.com.fiap.cidadaofacil.model.IndicadorMunicipioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// DAO: valores dos indicadores por município (busca com indicador e fonte em uma única consulta)
@Repository
public interface IndicadorMunicipioDAO extends JpaRepository<IndicadorMunicipio, IndicadorMunicipioId> {
    @Query("""
            SELECT v FROM IndicadorMunicipio v
            JOIN FETCH v.indicador i
            JOIN FETCH i.fonte
            WHERE v.municipio.codigoIbge = :codigo
            ORDER BY i.id, v.id.referencia DESC
            """)
    List<IndicadorMunicipio> buscarPorMunicipio(@Param("codigo") Long codigo);
}
