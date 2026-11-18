package br.com.fiap.cidadaofacil.bo;

import br.com.fiap.cidadaofacil.dao.IndicadorDAO;
import br.com.fiap.cidadaofacil.dao.IndicadorMunicipioDAO;
import br.com.fiap.cidadaofacil.dao.MunicipioDAO;
import br.com.fiap.cidadaofacil.dto.PanoramaDTO;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.exception.RegraNegocioException;
import br.com.fiap.cidadaofacil.model.IndicadorMunicipio;
import br.com.fiap.cidadaofacil.model.Municipio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// BO do panorama: busca de municípios (sem acento e sem diferenciar maiúsculas) e indicadores públicos
@Service
public class PanoramaBO {
    private static final int MAX_RESULTADOS = 20;

    private final MunicipioDAO municipioDAO;
    private final IndicadorDAO indicadorDAO;
    private final IndicadorMunicipioDAO valorDAO;

    // Município com o nome já normalizado e a população, usada para ordenar a busca
    private record Item(Municipio municipio, String nome, long populacao) {
    }

    // A lista de municípios muda só com nova carga: fica em memória para a busca ser rápida
    private volatile List<Item> cacheMunicipios;

    public PanoramaBO(MunicipioDAO municipioDAO, IndicadorDAO indicadorDAO, IndicadorMunicipioDAO valorDAO) {
        this.municipioDAO = municipioDAO;
        this.indicadorDAO = indicadorDAO;
        this.valorDAO = valorDAO;
    }

    @Transactional(readOnly = true)
    public List<PanoramaDTO.MunicipioResponse> buscarMunicipios(String busca) {
        String termo = AssistenteBO.normalizar(busca);
        if (termo.length() < 2) {
            throw new RegraNegocioException("Digite pelo menos 2 letras do nome do município.");
        }
        // Ordem: nome igual, começa com o termo, palavra que começa com o termo, contém. Empate: cidade maior primeiro
        record Achado(Item item, int nivel) {
        }
        List<Achado> achados = new ArrayList<>();
        for (Item item : municipios()) {
            int nivel = item.nome().equals(termo) ? 0
                    : item.nome().startsWith(termo) ? 1
                    : item.nome().contains(" " + termo) ? 2
                    : item.nome().contains(termo) ? 3 : -1;
            if (nivel >= 0) {
                achados.add(new Achado(item, nivel));
            }
        }
        achados.sort(Comparator.comparingInt(Achado::nivel)
                .thenComparing(a -> a.item().populacao(), Comparator.reverseOrder())
                .thenComparing(a -> a.item().nome()));
        return achados.stream().limit(MAX_RESULTADOS)
                .map(a -> PanoramaDTO.MunicipioResponse.de(a.item().municipio())).toList();
    }

    @Transactional(readOnly = true)
    public PanoramaDTO.MunicipioResponse buscarMunicipio(Long codigoIbge) {
        return PanoramaDTO.MunicipioResponse.de(buscarEntidade(codigoIbge));
    }

    // Panorama: valor mais recente de cada indicador, com fonte e período
    @Transactional(readOnly = true)
    public PanoramaDTO.Panorama panorama(Long codigoIbge) {
        Municipio municipio = buscarEntidade(codigoIbge);
        Set<Long> jaIncluidos = new HashSet<>();
        List<PanoramaDTO.Valor> valores = new ArrayList<>();
        for (IndicadorMunicipio v : valorDAO.buscarPorMunicipio(codigoIbge)) {
            if (jaIncluidos.add(v.getIndicador().getId())) {
                valores.add(PanoramaDTO.Valor.de(v));
            }
        }
        return new PanoramaDTO.Panorama(PanoramaDTO.MunicipioResponse.de(municipio), valores,
                PanoramaDTO.Panorama.AVISO);
    }

    @Transactional(readOnly = true)
    public List<PanoramaDTO.IndicadorResponse> listarIndicadores() {
        return indicadorDAO.findAllByOrderByIdAsc().stream().map(PanoramaDTO.IndicadorResponse::de).toList();
    }

    private Municipio buscarEntidade(Long codigoIbge) {
        return municipioDAO.findById(codigoIbge)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Município", codigoIbge));
    }

    private List<Item> municipios() {
        List<Item> lista = cacheMunicipios;
        if (lista == null) {
            Map<Long, Long> populacao = new HashMap<>();
            for (Object[] linha : valorDAO.valoresDoIndicador("POPULACAO")) {
                populacao.put(((Number) linha[0]).longValue(), ((BigDecimal) linha[1]).longValue());
            }
            lista = municipioDAO.findAllByOrderByNomeAsc().stream()
                    .map(m -> new Item(m, AssistenteBO.normalizar(m.getNome()),
                            populacao.getOrDefault(m.getCodigoIbge(), 0L)))
                    .toList();
            cacheMunicipios = lista;
        }
        return lista;
    }
}
