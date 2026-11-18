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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// BO do panorama: busca de municípios (sem acento e sem diferenciar maiúsculas) e indicadores públicos
@Service
public class PanoramaBO {
    private static final int MAX_RESULTADOS = 20;

    private final MunicipioDAO municipioDAO;
    private final IndicadorDAO indicadorDAO;
    private final IndicadorMunicipioDAO valorDAO;

    // A lista de municípios muda só com nova carga: fica em memória para a busca ser rápida
    private volatile List<Municipio> cacheMunicipios;

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
        List<Municipio> comecam = new ArrayList<>();
        List<Municipio> contem = new ArrayList<>();
        for (Municipio m : municipios()) {
            String nome = AssistenteBO.normalizar(m.getNome());
            if (nome.startsWith(termo)) {
                comecam.add(m);
            } else if (nome.contains(termo)) {
                contem.add(m);
            }
        }
        comecam.addAll(contem);
        return comecam.stream().limit(MAX_RESULTADOS).map(PanoramaDTO.MunicipioResponse::de).toList();
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

    private List<Municipio> municipios() {
        List<Municipio> lista = cacheMunicipios;
        if (lista == null) {
            lista = List.copyOf(municipioDAO.findAllByOrderByNomeAsc());
            cacheMunicipios = lista;
        }
        return lista;
    }
}
