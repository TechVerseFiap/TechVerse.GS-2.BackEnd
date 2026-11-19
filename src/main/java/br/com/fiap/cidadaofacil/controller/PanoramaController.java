package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.service.PanoramaService;
import br.com.fiap.cidadaofacil.dto.PanoramaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints do panorama do município: dados públicos agregados da Base dos Dados (somente leitura)
@RestController
@RequestMapping("/api")
@Tag(name = "Panorama do município", description = "Indicadores públicos por município (IBGE, CAGED, Bolsa Família, CNES)")
public class PanoramaController {
    private final PanoramaService panoramaService;

    public PanoramaController(PanoramaService panoramaService) {
        this.panoramaService = panoramaService;
    }

    @GetMapping("/municipios")
    @Operation(summary = "Busca municípios pelo nome (?busca=caieiras), sem diferenciar acentos")
    public List<PanoramaDTO.MunicipioResponse> buscar(@RequestParam(name = "busca") String busca) {
        return panoramaService.buscarMunicipios(busca);
    }

    @GetMapping("/municipios/{codigoIbge}")
    @Operation(summary = "Dados do município pelo código IBGE")
    public PanoramaDTO.MunicipioResponse municipio(@PathVariable Long codigoIbge) {
        return panoramaService.buscarMunicipio(codigoIbge);
    }

    @GetMapping("/municipios/{codigoIbge}/panorama")
    @Operation(summary = "Panorama do município: indicadores mais recentes, com fonte e período")
    public PanoramaDTO.Panorama panorama(@PathVariable Long codigoIbge) {
        return panoramaService.panorama(codigoIbge);
    }

    @GetMapping("/indicadores")
    @Operation(summary = "Lista os indicadores disponíveis no panorama")
    public List<PanoramaDTO.IndicadorResponse> indicadores() {
        return panoramaService.listarIndicadores();
    }
}
