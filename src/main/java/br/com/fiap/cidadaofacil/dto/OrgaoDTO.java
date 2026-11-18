package br.com.fiap.cidadaofacil.dto;

import br.com.fiap.cidadaofacil.model.EsferaGoverno;
import br.com.fiap.cidadaofacil.model.Orgao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// DTOs de órgão responsável: Request (entrada validada) e Response (saída)
public final class OrgaoDTO {
    private OrgaoDTO() {
    }

    public record Request(
            @NotBlank(message = "O nome do órgão é obrigatório.")
            @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
            String nome,

            @NotBlank(message = "A sigla é obrigatória.")
            @Size(max = 20, message = "A sigla deve ter no máximo 20 caracteres.")
            String sigla,

            @NotNull(message = "A esfera é obrigatória (FEDERAL, ESTADUAL ou MUNICIPAL).")
            EsferaGoverno esfera
    ) {
    }

    public record Response(Long id, String nome, String sigla, EsferaGoverno esfera) {
        public static Response de(Orgao o) {
            return new Response(o.getId(), o.getNome(), o.getSigla(), o.getEsfera());
        }
    }
}
