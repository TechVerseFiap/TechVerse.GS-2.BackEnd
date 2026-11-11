package br.com.fiap.cidadaofacil.dto;

import br.com.fiap.cidadaofacil.model.Interacao;
import br.com.fiap.cidadaofacil.model.Recomendacao;
import br.com.fiap.cidadaofacil.model.ResultadoInteracao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

// DTOs de assistente (pergunta, resposta, histórico e indicadores): Request (entrada validada) e Response (saída)
public final class AssistenteDTO {
    private AssistenteDTO() {
    }

    public record Pergunta(
            @NotBlank(message = "Escreva o que você precisa.")
            @Size(min = 3, max = 500, message = "A mensagem deve ter entre 3 e 500 caracteres.")
            String mensagem
    ) {
    }

    public record ServicoRecomendado(
            Long servicoId,
            String nome,
            String categoria,
            String orgaoResponsavel,
            String urlFonte,
            String justificativa,
            int pontuacao
    ) {
        public static ServicoRecomendado de(Recomendacao r) {
            return new ServicoRecomendado(
                    r.getServico().getId(), r.getServico().getNome(),
                    r.getServico().getCategoria().getNome(),
                    r.getServico().getOrgaoResponsavel(), r.getServico().getUrlFonte(),
                    r.getJustificativa(), r.getPontuacao());
        }
    }

    public record Resposta(
            Long interacaoId,
            ResultadoInteracao resultado,
            String assuntoIdentificado,
            String mensagem,
            List<ServicoRecomendado> recomendacoes,
            List<String> sugestoesDeCategoria,
            String aviso
    ) {
    }

    public record InteracaoResponse(
            Long id,
            String assunto,
            LocalDateTime dataHora,
            ResultadoInteracao resultado,
            List<ServicoRecomendado> recomendacoes
    ) {
        public static InteracaoResponse de(Interacao i) {
            return new InteracaoResponse(i.getId(), i.getAssunto(), i.getDataHora(), i.getResultado(),
                    i.getRecomendacoes().stream().map(ServicoRecomendado::de).toList());
        }
    }

    public record Indicadores(
            long totalInteracoes,
            long encontradas,
            long pediramEsclarecimento,
            long naoEncontradas,
            double taxaOrientacaoEncontrada
    ) {
    }
}
