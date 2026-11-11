package br.com.fiap.cidadaofacil.dto;

import br.com.fiap.cidadaofacil.model.Etapa;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// DTOs de etapa: Request (entrada validada) e Response (saída)
public final class EtapaDTO {
    private EtapaDTO() {
    }

    public record Request(
            @NotNull(message = "A ordem é obrigatória.")
            @Min(value = 1, message = "A ordem começa em 1.")
            @Max(value = 50, message = "A ordem deve ser no máximo 50.")
            Integer ordem,

            @NotBlank(message = "A descrição da etapa é obrigatória.")
            @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres.")
            String descricao
    ) {
    }

    public record Response(Long id, Long servicoId, Integer ordem, String descricao) {
        public static Response de(Etapa e) {
            return new Response(e.getId(), e.getServico().getId(), e.getOrdem(), e.getDescricao());
        }
    }
}
