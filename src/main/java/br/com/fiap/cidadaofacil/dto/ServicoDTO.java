package br.com.fiap.cidadaofacil.dto;

import br.com.fiap.cidadaofacil.model.CanalAtendimento;
import br.com.fiap.cidadaofacil.model.Servico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

// DTOs de serviço: Request (entrada validada) e Response (saída)
public final class ServicoDTO {
    private ServicoDTO() {
    }

    public record Request(
            @NotNull(message = "A categoria é obrigatória.")
            Long categoriaId,

            @NotBlank(message = "O nome é obrigatório.")
            @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
            String nome,

            @NotBlank(message = "A descrição é obrigatória.")
            @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
            String descricao,

            @Size(max = 255, message = "O público-alvo deve ter no máximo 255 caracteres.")
            String publicoAlvo,

            @NotBlank(message = "O órgão responsável é obrigatório.")
            @Size(max = 120, message = "O órgão deve ter no máximo 120 caracteres.")
            String orgaoResponsavel,

            @NotNull(message = "O canal é obrigatório (DIGITAL, PRESENCIAL ou DIGITAL_E_PRESENCIAL).")
            CanalAtendimento canal,

            @NotBlank(message = "A URL da fonte oficial é obrigatória.")
            @Size(max = 255, message = "A URL deve ter no máximo 255 caracteres.")
            String urlFonte,

            @NotNull(message = "A data de verificação da fonte é obrigatória.")
            @PastOrPresent(message = "A data de verificação não pode estar no futuro.")
            LocalDate dataVerificacao,

            @Size(max = 400, message = "As palavras-chave devem ter no máximo 400 caracteres.")
            String palavrasChave
    ) {
    }

    // Versão resumida para listagens
    public record Resumo(
            Long id,
            String nome,
            String descricao,
            Long categoriaId,
            String categoria,
            String orgaoResponsavel,
            CanalAtendimento canal,
            boolean precisaReverificacao
    ) {
        public static Resumo de(Servico s) {
            return new Resumo(
                    s.getId(), s.getNome(), s.getDescricao(),
                    s.getCategoria().getId(), s.getCategoria().getNome(),
                    s.getOrgaoResponsavel(), s.getCanal(), s.precisaReverificacao());
        }
    }

    // Versão completa para a tela de detalhe
    public record Detalhe(
            Long id,
            String nome,
            String descricao,
            String publicoAlvo,
            CategoriaDTO.Response categoria,
            String orgaoResponsavel,
            CanalAtendimento canal,
            String urlFonte,
            LocalDate dataVerificacao,
            boolean precisaReverificacao,
            String palavrasChave,
            List<RequisitoDTO.Response> requisitos,
            List<EtapaDTO.Response> etapas,
            List<DocumentoDTO.DoServico> documentos,
            String aviso
    ) {
        public static final String AVISO = "Informação de orientação. Requisitos, documentos e prazos podem mudar "
                + "e dependem do órgão responsável. Confirme sempre no canal oficial.";

        public static Detalhe de(Servico s) {
            return new Detalhe(
                    s.getId(), s.getNome(), s.getDescricao(), s.getPublicoAlvo(),
                    CategoriaDTO.Response.de(s.getCategoria()),
                    s.getOrgaoResponsavel(), s.getCanal(), s.getUrlFonte(), s.getDataVerificacao(),
                    s.precisaReverificacao(), s.getPalavrasChave(),
                    s.getRequisitos().stream().map(RequisitoDTO.Response::de).toList(),
                    s.getEtapas().stream().map(EtapaDTO.Response::de).toList(),
                    s.getDocumentos().stream().map(DocumentoDTO.DoServico::de).toList(),
                    AVISO);
        }
    }
}
