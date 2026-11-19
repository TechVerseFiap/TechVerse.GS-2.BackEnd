package br.com.fiap.cidadaofacil.service;

import br.com.fiap.cidadaofacil.dto.AssistenteDTO;
import br.com.fiap.cidadaofacil.dto.CatalogoGovBrDTO;
import br.com.fiap.cidadaofacil.dto.ServicoDTO;
import br.com.fiap.cidadaofacil.repository.GovBrRepository;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// Testa a gravação do catálogo no H2 (perfil dev) com páginas do gov.br salvas em src/test/resources/govbr
@SpringBootTest(properties = "govbr.sincronizar-ao-iniciar=false")
@ActiveProfiles("dev")
class CatalogoGovBrServiceTest {
    private static final String CNH = "emitir-a-carteira-nacional-de-habilitacao-digital-cnh-e";

    @Autowired
    private CatalogoGovBrService catalogoService;

    @Autowired
    private ServicoService servicoService;

    @Autowired
    private AssistenteService assistenteService;

    @MockitoBean
    private GovBrRepository govBr;

    private static Optional<CatalogoGovBrDTO.ServicoLido> lido(String slug) {
        try (InputStream in = CatalogoGovBrServiceTest.class.getResourceAsStream("/govbr/servico-" + slug + ".html")) {
            return Optional.of(GovBrRepository.lerServico(slug, Jsoup.parse(in, "UTF-8", GovBrRepository.PREFIXO_SERVICO + slug)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void montaOCatalogoAPartirDoGovBrEAtualizaSemDuplicar() {
        when(govBr.listarServicos(anyString(), anyInt())).thenReturn(List.of());
        when(govBr.listarServicos("trabalho-e-previdencia/trabalho-e-emprego/para-o-cidadao", 0))
                .thenReturn(List.of("solicitar-o-seguro-desemprego", "obter-a-carteira-de-trabalho"));
        when(govBr.listarServicos("transito-e-transportes/terrestre/transito", 0)).thenReturn(List.of(CNH));
        for (String slug : List.of("solicitar-o-seguro-desemprego", "obter-a-carteira-de-trabalho", CNH)) {
            when(govBr.buscarServico(slug)).thenReturn(lido(slug));
        }

        CatalogoGovBrDTO.Resultado r = catalogoService.sincronizar(p -> { });

        // Seguro-desemprego e Carteira de Trabalho já existiam na carga (mesma URL oficial): são atualizados
        assertEquals(3, r.encontrados());
        assertEquals(1, r.novos());
        assertEquals(2, r.atualizados());
        assertEquals(0, r.erros());

        // Serviço que já existia mantém o texto da equipe e recebe documentos e etapas do gov.br
        ServicoDTO.Detalhe seguro = servicoService.detalhar(2L);
        assertEquals("Seguro-Desemprego", seguro.nome());
        assertTrue(seguro.documentos().stream().anyMatch(d -> d.nome().equals("Número do CPF")));
        assertEquals(3, seguro.etapas().size());

        // CNH Digital é criada na categoria Documentos, com o órgão do gov.br e o link do botão "Iniciar"
        ServicoDTO.Resumo cnh = servicoService.listar(null, null).stream()
                .filter(s -> s.nome().startsWith("Emitir a Carteira Nacional")).findFirst().orElseThrow();
        ServicoDTO.Detalhe detalhe = servicoService.detalhar(cnh.id());
        assertEquals("Documentos", detalhe.categoria().nome());
        assertEquals("Ministério dos Transportes", detalhe.orgaoResponsavel());
        assertEquals(GovBrRepository.PREFIXO_SERVICO + CNH, detalhe.urlFonte());
        assertTrue(detalhe.requisitos().stream().anyMatch(q -> q.descricao().contains("QR Code")));

        // O assistente passa a encontrar o serviço novo pela sigla que estava só no endereço
        AssistenteDTO.Resposta resposta = assistenteService.responder(new AssistenteDTO.Pergunta("quero tirar a cnh digital"));
        assertTrue(resposta.recomendacoes().stream().anyMatch(s -> s.servicoId().equals(cnh.id())));

        // Segunda execução no mesmo dia: tudo foi conferido agora, nada é lido de novo
        CatalogoGovBrDTO.Resultado segunda = catalogoService.sincronizar(p -> { });
        assertEquals(3, segunda.pulados());
        assertEquals(0, segunda.novos() + segunda.atualizados());
    }
}
