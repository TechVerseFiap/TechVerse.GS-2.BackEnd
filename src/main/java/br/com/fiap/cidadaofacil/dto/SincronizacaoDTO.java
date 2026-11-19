package br.com.fiap.cidadaofacil.dto;

import java.time.LocalDateTime;
import java.util.List;

// DTOs da sincronização: catálogo do gov.br e indicadores da Base dos Dados (BigQuery) -> Oracle
public final class SincronizacaoDTO {
    private SincronizacaoDTO() {
    }

    // Resultado de um indicador: período trazido e quantas linhas entraram, mudaram ou foram ignoradas
    public record IndicadorSincronizado(String codigo, String periodo, int linhasRecebidas, int novas,
                                        int atualizadas, int ignoradas) {
    }

    public record Resultado(String uf, LocalDateTime inicio, LocalDateTime fim, int municipiosNovos,
                            List<IndicadorSincronizado> indicadores) {
    }

    // Situação atual: Base dos Dados configurada, se está rodando (etapa e progresso) e o último resultado de cada parte
    public record Status(boolean configurada, boolean emAndamento, String etapaAtual, String progresso, String uf,
                         CatalogoGovBrDTO.Resultado ultimoCatalogo, String erroCatalogo,
                         Resultado ultimaSincronizacao, String ultimoErro) {
    }
}
