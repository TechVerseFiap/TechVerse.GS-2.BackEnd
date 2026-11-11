package br.com.fiap.cidadaofacil.dto;

import br.com.fiap.cidadaofacil.model.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTOs de categoria: Request (entrada validada) e Response (saída)
public final class CategoriaDTO {
    private CategoriaDTO() {
    }

    public record Request(
            @NotBlank(message = "O nome é obrigatório.")
            @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres.")
            String nome,

            @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
            String descricao
    ) {
    }

    public record Response(Long id, String nome, String descricao) {
        public static Response de(Categoria c) {
            return new Response(c.getId(), c.getNome(), c.getDescricao());
        }
    }
}
