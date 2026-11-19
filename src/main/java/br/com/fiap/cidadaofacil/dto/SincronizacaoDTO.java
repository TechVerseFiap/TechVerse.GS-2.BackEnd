package br.com.fiap.cidadaofacil.dto;

import java.time.LocalDateTime;
import java.util.List;

// DTOs da sincronização com a Base dos Dados (BigQuery -> Oracle)
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

    // Situação atual: se está configurada, se está rodando e o resultado da última execução
    public record Status(boolean configurada, boolean emAndamento, String uf, Resultado ultimaSincronizacao,
                         String ultimoErro) {
    }
}
