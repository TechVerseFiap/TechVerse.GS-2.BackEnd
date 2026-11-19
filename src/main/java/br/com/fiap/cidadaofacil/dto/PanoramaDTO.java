package br.com.fiap.cidadaofacil.dto;

import br.com.fiap.cidadaofacil.model.Indicador;
import br.com.fiap.cidadaofacil.model.IndicadorMunicipio;
import br.com.fiap.cidadaofacil.model.Municipio;
import br.com.fiap.cidadaofacil.model.Periodicidade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// DTOs do panorama do município (dados públicos agregados da Base dos Dados)
public final class PanoramaDTO {
    private static final DateTimeFormatter MES_ANO = DateTimeFormatter.ofPattern("MM/yyyy");

    private PanoramaDTO() {
    }

    // Período para exibição: "2025" (anual) ou "03/2026" (mensal)
    public static String formatarPeriodo(Periodicidade periodicidade, LocalDate referencia) {
        return periodicidade == Periodicidade.ANUAL ? String.valueOf(referencia.getYear()) : referencia.format(MES_ANO);
    }

    public record MunicipioResponse(Long codigoIbge, String nome, String uf) {
        public static MunicipioResponse de(Municipio m) {
            return new MunicipioResponse(m.getCodigoIbge(), m.getNome(), m.getUf());
        }
    }

    public record IndicadorResponse(Long id, String codigo, String nome, String descricao, String unidade,
                                    Periodicidade periodicidade, FonteDTO.Response fonte) {
        public static IndicadorResponse de(Indicador i) {
            return new IndicadorResponse(i.getId(), i.getCodigo(), i.getNome(), i.getDescricao(), i.getUnidade(),
                    i.getPeriodicidade(), FonteDTO.Response.de(i.getFonte()));
        }
    }

    // Um valor do panorama, com o período já formatado para exibição (ex.: "2025" ou "03/2026")
    public record Valor(String codigo, String nome, String descricao, BigDecimal valor, String unidade,
                        LocalDate referencia, String periodo, String fonte, String urlFonte, LocalDate dataConsulta) {
        public static Valor de(IndicadorMunicipio v) {
            Indicador i = v.getIndicador();
            LocalDate ref = v.getId().getReferencia();
            String periodo = formatarPeriodo(i.getPeriodicidade(), ref);
            return new Valor(i.getCodigo(), i.getNome(), i.getDescricao(), v.getValor(), i.getUnidade(), ref, periodo,
                    i.getFonte().getTitulo(), i.getFonte().getUrl(), i.getFonte().getDataConsulta());
        }
    }

    public record Panorama(MunicipioResponse municipio, List<Valor> indicadores, String aviso) {
        public static final String AVISO = "Números públicos e agregados, sem dados pessoais. Eles ajudam a entender "
                + "o território, mas não indicam se você tem direito a algum benefício.";
    }
}
