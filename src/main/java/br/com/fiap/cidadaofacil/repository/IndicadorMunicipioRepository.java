package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.model.IndicadorMunicipio;
import br.com.fiap.cidadaofacil.model.IndicadorMunicipioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository: valores dos indicadores por município (busca com indicador e fonte em uma única consulta)
@Repository
public interface IndicadorMunicipioRepository extends JpaRepository<IndicadorMunicipio, IndicadorMunicipioId> {
    @Query("""
            SELECT v FROM IndicadorMunicipio v
            JOIN FETCH v.indicador i
            JOIN FETCH i.fonte
            WHERE v.municipio.codigoIbge = :codigo
            ORDER BY i.id, v.id.referencia DESC
            """)
    List<IndicadorMunicipio> buscarPorMunicipio(@Param("codigo") Long codigo);

    // Código IBGE e valor de um indicador em todos os municípios, do período mais antigo ao mais recente
    @Query("""
            SELECT v.municipio.codigoIbge, v.valor FROM IndicadorMunicipio v
            WHERE v.indicador.codigo = :indicador
            ORDER BY v.id.referencia
            """)
    List<Object[]> valoresDoIndicador(@Param("indicador") String indicador);
}
