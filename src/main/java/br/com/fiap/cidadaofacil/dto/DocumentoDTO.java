package br.com.fiap.cidadaofacil.dto;

import br.com.fiap.cidadaofacil.model.Documento;
import br.com.fiap.cidadaofacil.model.ServicoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// DTOs de documento e vínculo com serviço: Request (entrada validada) e Response (saída)
public final class DocumentoDTO {
    private DocumentoDTO() {
    }

    public record Request(
            @NotBlank(message = "O nome do documento é obrigatório.")
            @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
            String nome,

            @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres.")
            String descricao
    ) {
    }

    public record Response(Long id, String nome, String descricao) {
        public static Response de(Documento d) {
            return new Response(d.getId(), d.getNome(), d.getDescricao());
        }
    }

    public record Vinculo(
            @NotNull(message = "O id do documento é obrigatório.")
            Long documentoId,

            @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres.")
            String observacao
    ) {
    }

    public record DoServico(Long documentoId, String nome, String descricao, String observacao) {
        public static DoServico de(ServicoDocumento sd) {
            Documento d = sd.getDocumento();
            return new DoServico(d.getId(), d.getNome(), d.getDescricao(), sd.getObservacao());
        }
    }
}
