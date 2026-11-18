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
            @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres.")
            String nome,

            @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres.")
            String descricao
    ) {
    }

    public record Response(Long id, String nome, String descricao) {
        public static Response de(Categoria c) {
            return new Response(c.getId(), c.getNome(), c.getDescricao());
        }
    }
}
