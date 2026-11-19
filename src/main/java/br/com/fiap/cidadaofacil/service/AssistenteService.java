package br.com.fiap.cidadaofacil.service;

import br.com.fiap.cidadaofacil.repository.CategoriaRepository;
import br.com.fiap.cidadaofacil.repository.InteracaoRepository;
import br.com.fiap.cidadaofacil.repository.ServicoRepository;
import br.com.fiap.cidadaofacil.dto.AssistenteDTO;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Categoria;
import br.com.fiap.cidadaofacil.model.Interacao;
import br.com.fiap.cidadaofacil.model.Recomendacao;
import br.com.fiap.cidadaofacil.model.ResultadoInteracao;
import br.com.fiap.cidadaofacil.model.Servico;
import br.com.fiap.cidadaofacil.model.StatusServico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Service do assistente: identifica a necessidade por palavras-chave e recomenda serviços do catálogo, sem inventar respostas
@Service
public class AssistenteService {
    public static final String AVISO = "O Cidadão Fácil orienta, mas não confirma direitos nem concede benefícios. "
            + "Requisitos e decisões finais dependem do órgão responsável: confirme no canal oficial.";

    private static final int MAX_RECOMENDACOES = 3;
    private static final int PESO_PALAVRA_CHAVE = 2;
    private static final int PESO_CATEGORIA = 1;
    private static final String ASSUNTO_NAO_IDENTIFICADO = "NAO_IDENTIFICADO";

    private final ServicoRepository servicoRepository;
    private final CategoriaRepository categoriaRepository;
    private final InteracaoRepository interacaoRepository;

    public AssistenteService(ServicoRepository servicoRepository, CategoriaRepository categoriaRepository, InteracaoRepository interacaoRepository) {
        this.servicoRepository = servicoRepository;
        this.categoriaRepository = categoriaRepository;
        this.interacaoRepository = interacaoRepository;
    }

    @Transactional
    public AssistenteDTO.Resposta responder(AssistenteDTO.Pergunta pergunta) {
        String texto = " " + normalizar(pergunta.mensagem()) + " ";

        List<Categoria> categorias = categoriaRepository.findAllByOrderByNomeAsc();
        List<Categoria> categoriasCitadas = categorias.stream()
                .filter(c -> citaCategoria(texto, c))
                .toList();

        // 1. Pontua cada serviço ativo pelas palavras-chave encontradas no texto
        List<Candidato> candidatos = new ArrayList<>();
        for (Servico servico : servicoRepository.findByStatus(StatusServico.ATIVO)) {
            List<String> encontradas = servico.listaPalavrasChave().stream()
                    .filter(p -> contemTermo(texto, normalizar(p)))
                    .toList();
            if (!encontradas.isEmpty()) {
                int pontos = encontradas.size() * PESO_PALAVRA_CHAVE;
                Long idCategoria = servico.getCategoria().getId();
                if (categoriasCitadas.stream().anyMatch(c -> c.getId().equals(idCategoria))) {
                    pontos += PESO_CATEGORIA;
                }
                candidatos.add(new Candidato(servico, encontradas, pontos));
            }
        }

        // 2. Encontrou serviços: devolve os 3 melhores com justificativa
        if (!candidatos.isEmpty()) {
            candidatos.sort(Comparator.comparingInt(Candidato::pontos).reversed()
                    .thenComparing(c -> c.servico().getNome()));
            List<Candidato> melhores = candidatos.stream().limit(MAX_RECOMENDACOES).toList();

            Interacao interacao = new Interacao(
                    melhores.get(0).servico().getCategoria().getNome(), ResultadoInteracao.ENCONTRADO);
            for (Candidato c : melhores) {
                String justificativa = "Pode ser relevante porque você mencionou: "
                        + String.join(", ", c.palavras()) + ".";
                interacao.adicionarRecomendacao(new Recomendacao(c.servico(), justificativa, c.pontos()));
            }
            interacaoRepository.save(interacao);

            return montarResposta(interacao,
                    "Encontrei " + melhores.size() + " caminho(s) que podem ajudar. "
                            + "Abra o serviço para ver requisitos, documentos e etapas.",
                    List.of());
        }

        // 3. Só reconheceu a categoria: pede esclarecimento
        if (!categoriasCitadas.isEmpty()) {
            Categoria categoria = categoriasCitadas.get(0);
            List<Servico> opcoes = servicoRepository.findByCategoriaIdAndStatusOrderByNomeAsc(categoria.getId(), StatusServico.ATIVO);

            Interacao interacao = new Interacao(categoria.getNome(), ResultadoInteracao.PEDIU_ESCLARECIMENTO);
            opcoes.stream().limit(5).forEach(s -> interacao.adicionarRecomendacao(new Recomendacao(
                    s, "Serviço da categoria " + categoria.getNome() + ".", PESO_CATEGORIA)));
            interacaoRepository.save(interacao);

            String mensagem = opcoes.isEmpty()
                    ? "Entendi que o assunto é " + categoria.getNome()
                    + ", mas ainda não há serviços cadastrados nessa categoria."
                    : "Entendi que o assunto é " + categoria.getNome()
                    + ". Qual destes serviços você procura? Se não for nenhum, conte com outras palavras.";
            return montarResposta(interacao, mensagem, List.of());
        }

        // 4. Nada encontrado: admite e sugere categorias
        Interacao interacao = new Interacao(ASSUNTO_NAO_IDENTIFICADO, ResultadoInteracao.NAO_ENCONTRADO);
        interacaoRepository.save(interacao);
        return montarResposta(interacao,
                "Não encontrei no catálogo um serviço para o que você descreveu. "
                        + "Tente explicar com outras palavras ou escolha uma das categorias abaixo.",
                categorias.stream().map(Categoria::getNome).toList());
    }

    // Histórico de interações e indicador de impacto
    @Transactional(readOnly = true)
    public List<AssistenteDTO.InteracaoResponse> listarInteracoes() {
        return interacaoRepository.findAllByOrderByDataHoraDesc().stream()
                .map(AssistenteDTO.InteracaoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssistenteDTO.InteracaoResponse buscarInteracao(Long id) {
        return AssistenteDTO.InteracaoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public void excluirInteracao(Long id) {
        interacaoRepository.delete(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public AssistenteDTO.Indicadores indicadores() {
        long total = interacaoRepository.count();
        long encontradas = interacaoRepository.countByResultado(ResultadoInteracao.ENCONTRADO);
        long esclarecimento = interacaoRepository.countByResultado(ResultadoInteracao.PEDIU_ESCLARECIMENTO);
        long naoEncontradas = interacaoRepository.countByResultado(ResultadoInteracao.NAO_ENCONTRADO);
        double taxa = total == 0 ? 0.0 : Math.round(encontradas * 1000.0 / total) / 10.0;
        return new AssistenteDTO.Indicadores(total, encontradas, esclarecimento, naoEncontradas, taxa);
    }

    // Apoio: montagem da resposta e tratamento do texto
    private Interacao buscarEntidade(Long id) {
        return interacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Interação", id));
    }

    private AssistenteDTO.Resposta montarResposta(Interacao interacao, String mensagem, List<String> sugestoes) {
        return new AssistenteDTO.Resposta(
                interacao.getId(),
                interacao.getResultado(),
                interacao.getAssunto(),
                mensagem,
                interacao.getRecomendacoes().stream().map(AssistenteDTO.ServicoRecomendado::de).toList(),
                sugestoes,
                AVISO);
    }

    private boolean citaCategoria(String texto, Categoria categoria) {
        String nome = normalizar(categoria.getNome());
        String singular = nome.endsWith("s") ? nome.substring(0, nome.length() - 1) : nome;
        return contemTermo(texto, nome) || contemTermo(texto, singular);
    }

    private boolean contemTermo(String textoComEspacos, String termo) {
        if (termo.isBlank()) {
            return false;
        }
        return textoComEspacos.contains(" " + termo + " ") || textoComEspacos.contains(" " + termo + "s ");
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record Candidato(Servico servico, List<String> palavras, int pontos) {
    }
}
