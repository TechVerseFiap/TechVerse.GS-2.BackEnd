package br.com.fiap.cidadaofacil.dto;

import br.com.fiap.cidadaofacil.model.Fonte;
import br.com.fiap.cidadaofacil.model.TipoFonte;

import java.time.LocalDate;

// DTO de saída de uma fonte oficial
public final class FonteDTO {
    private FonteDTO() {
    }

    public record Response(Long id, String titulo, String url, TipoFonte tipo, LocalDate dataConsulta) {
        public static Response de(Fonte f) {
            return new Response(f.getId(), f.getTitulo(), f.getUrl(), f.getTipo(), f.getDataConsulta());
        }
    }
}
