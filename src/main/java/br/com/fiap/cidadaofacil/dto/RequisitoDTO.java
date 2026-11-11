package br.com.fiap.cidadaofacil.dto;

import br.com.fiap.cidadaofacil.model.Requisito;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTOs de requisito: Request (entrada validada) e Response (saída)
public final class RequisitoDTO {
    private RequisitoDTO() {
    }

    public record Request(
            @NotBlank(message = "A descrição do requisito é obrigatória.")
            @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres.")
            String descricao
    ) {
    }

    public record Response(Long id, Long servicoId, String descricao) {
        public static Response de(Requisito r) {
            return new Response(r.getId(), r.getServico().getId(), r.getDescricao());
        }
    }
}
