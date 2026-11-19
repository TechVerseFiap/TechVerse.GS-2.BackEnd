package br.com.fiap.cidadaofacil.service;

import br.com.fiap.cidadaofacil.dto.PanoramaDTO;
import br.com.fiap.cidadaofacil.dto.SincronizacaoDTO;
import br.com.fiap.cidadaofacil.exception.ServicoIndisponivelException;
import br.com.fiap.cidadaofacil.repository.BaseDosDadosRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// Testa a gravação da sincronização no banco H2 (perfil dev), trocando o BigQuery por respostas fixas
@SpringBootTest
@ActiveProfiles("dev")
class SincronizacaoServiceTest {
    private static final long CAIEIRAS = 3509007L;
    private static final long MUNICIPIO_NOVO = 3599999L;

    @Autowired
    private SincronizacaoService sincronizacaoService;

    @Autowired
    private PanoramaService panoramaService;

    @MockitoBean
    private BaseDosDadosRepository baseDosDados;

    @Test
    void gravaPeriodoNovoIgnoraMunicipioDesconhecidoECadastraMunicipioNovo() {
        when(baseDosDados.estaConfigurado()).thenReturn(true);
        when(baseDosDados.consultar(anyString(), anyMap())).thenAnswer(chamada -> {
            String sql = chamada.getArgument(0);
            if (sql.contains("diretorios_brasil")) {
                return List.of(List.of(String.valueOf(CAIEIRAS), "Caieiras", "SP"),
                        List.of(String.valueOf(MUNICIPIO_NOVO), "Município Teste", "SP"));
            }
            if (sql.contains("LIMIT 1") || sql.startsWith("SELECT MAX")) {
                return List.of(List.of("2026", sql.contains("MAX") ? "1" : "6"));
            }
            // Valores: Caieiras, o município novo e uma linha sem município (deve ser ignorada)
            return List.of(List.of(String.valueOf(CAIEIRAS), "123.45"),
                    List.of(String.valueOf(MUNICIPIO_NOVO), "10"),
                    java.util.Arrays.asList(null, "99"));
        });

        SincronizacaoDTO.Resultado resultado = sincronizacaoService.sincronizar();

        assertEquals(1, resultado.municipiosNovos());
        assertEquals(5, resultado.indicadores().size());
        for (SincronizacaoDTO.IndicadorSincronizado item : resultado.indicadores()) {
            assertEquals(3, item.linhasRecebidas());
            assertEquals(2, item.novas());
            assertEquals(1, item.ignoradas());
        }

        // O panorama passa a mostrar o período novo (2026 / 06/2026) com o valor da Base dos Dados
        PanoramaDTO.Panorama panorama = panoramaService.panorama(CAIEIRAS);
        PanoramaDTO.Valor populacao = panorama.indicadores().stream()
                .filter(v -> v.codigo().equals("POPULACAO")).findFirst().orElseThrow();
        assertEquals("2026", populacao.periodo());
        assertEquals(0, new BigDecimal("123.45").compareTo(populacao.valor()));
        PanoramaDTO.Valor sus = panorama.indicadores().stream()
                .filter(v -> v.codigo().equals("ESTABELECIMENTOS_SUS")).findFirst().orElseThrow();
        assertEquals("06/2026", sus.periodo());

        // Município novo aparece na busca (o cache foi limpo)
        assertTrue(panoramaService.buscarMunicipios("municipio teste").stream()
                .anyMatch(m -> m.codigoIbge() == MUNICIPIO_NOVO));

        // Rodar de novo com valores diferentes atualiza em vez de duplicar
        when(baseDosDados.consultar(anyString(), anyMap())).thenAnswer(chamada -> {
            String sql = chamada.getArgument(0);
            if (sql.contains("diretorios_brasil")) {
                return List.of(List.of(String.valueOf(CAIEIRAS), "Caieiras", "SP"));
            }
            if (sql.contains("LIMIT 1") || sql.startsWith("SELECT MAX")) {
                return List.of(List.of("2026", sql.contains("MAX") ? "1" : "6"));
            }
            return List.of(List.of(String.valueOf(CAIEIRAS), "200"));
        });
        SincronizacaoDTO.Resultado segunda = sincronizacaoService.sincronizar();
        assertEquals(0, segunda.municipiosNovos());
        segunda.indicadores().forEach(i -> {
            assertEquals(0, i.novas());
            assertEquals(1, i.atualizadas());
        });
    }

    @Test
    void semCredenciaisRespondeServicoIndisponivel() {
        when(baseDosDados.consultar(anyString(), anyMap()))
                .thenThrow(new ServicoIndisponivelException("não configurada"));
        assertThrows(ServicoIndisponivelException.class, () -> sincronizacaoService.sincronizar());
        assertEquals("não configurada", sincronizacaoService.status().ultimoErro());
    }
}
