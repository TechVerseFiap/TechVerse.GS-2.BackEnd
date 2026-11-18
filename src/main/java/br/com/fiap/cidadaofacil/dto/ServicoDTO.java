package br.com.fiap.cidadaofacil.dto;

import br.com.fiap.cidadaofacil.model.Canal;
import br.com.fiap.cidadaofacil.model.CanalAtendimento;
import br.com.fiap.cidadaofacil.model.Servico;
import br.com.fiap.cidadaofacil.model.TipoCanal;
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

    // Órgão: informe orgaoId (preferível) ou o nome/sigla de um órgão já cadastrado em orgaoResponsavel
    public record Request(
            @NotNull(message = "A categoria é obrigatória.")
            Long categoriaId,

            @NotBlank(message = "O nome é obrigatório.")
            @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
            String nome,

            @NotBlank(message = "A descrição é obrigatória.")
            @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
            String descricao,

            @Size(max = 300, message = "O público-alvo deve ter no máximo 300 caracteres.")
            String publicoAlvo,

            Long orgaoId,

            @Size(max = 150, message = "O órgão deve ter no máximo 150 caracteres.")
            String orgaoResponsavel,

            @NotNull(message = "O canal é obrigatório (DIGITAL, PRESENCIAL ou DIGITAL_E_PRESENCIAL).")
            CanalAtendimento canal,

            @NotBlank(message = "A URL da fonte oficial é obrigatória.")
            @Size(max = 500, message = "A URL deve ter no máximo 500 caracteres.")
            String urlFonte,

            @NotNull(message = "A data de verificação da fonte é obrigatória.")
            @PastOrPresent(message = "A data de verificação não pode estar no futuro.")
            LocalDate dataVerificacao,

            @Size(max = 400, message = "As palavras-chave devem ter no máximo 400 caracteres.")
            String palavrasChave,

            Boolean ativo
    ) {
    }

    public record CanalResponse(TipoCanal tipo, String endereco) {
        public static CanalResponse de(Canal c) {
            return new CanalResponse(c.getTipo(), c.getEndereco());
        }
    }

    // Versão resumida para listagens
    public record Resumo(
            Long id,
            String nome,
            String descricao,
            Long categoriaId,
            String categoria,
            Long orgaoId,
            String orgaoResponsavel,
            CanalAtendimento canal,
            boolean precisaReverificacao,
            boolean ativo
    ) {
        public static Resumo de(Servico s) {
            return new Resumo(
                    s.getId(), s.getNome(), s.getDescricao(),
                    s.getCategoria().getId(), s.getCategoria().getNome(),
                    s.getOrgao().getId(), s.getOrgao().getNome(),
                    s.getCanal(), s.precisaReverificacao(), s.isAtivo());
        }
    }

    // Versão completa para a tela de detalhe
    public record Detalhe(
            Long id,
            String nome,
            String descricao,
            String publicoAlvo,
            CategoriaDTO.Response categoria,
            OrgaoDTO.Response orgao,
            String orgaoResponsavel,
            CanalAtendimento canal,
            List<CanalResponse> canais,
            FonteDTO.Response fonte,
            String urlFonte,
            LocalDate dataVerificacao,
            boolean precisaReverificacao,
            boolean ativo,
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
                    OrgaoDTO.Response.de(s.getOrgao()), s.getOrgao().getNome(),
                    s.getCanal(), s.getCanais().stream().map(CanalResponse::de).toList(),
                    FonteDTO.Response.de(s.getFonte()), s.getUrlFonte(), s.getDataVerificacao(),
                    s.precisaReverificacao(), s.isAtivo(), s.getPalavrasChave(),
                    s.getRequisitos().stream().map(RequisitoDTO.Response::de).toList(),
                    s.getEtapas().stream().map(EtapaDTO.Response::de).toList(),
                    s.getDocumentos().stream().map(DocumentoDTO.DoServico::de).toList(),
                    AVISO);
        }
    }
}
