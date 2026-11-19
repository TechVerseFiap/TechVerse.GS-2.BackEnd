package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.dto.CatalogoGovBrDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Testa a leitura das páginas do gov.br com cópias salvas em src/test/resources/govbr (sem acessar a internet)
class GovBrRepositoryTest {

    private static Document pagina(String arquivo, String url) throws IOException {
        try (InputStream in = GovBrRepositoryTest.class.getResourceAsStream("/govbr/" + arquivo)) {
            return Jsoup.parse(in, "UTF-8", url);
        }
    }

    private static CatalogoGovBrDTO.ServicoLido servico(String slug) throws IOException {
        return GovBrRepository.lerServico(slug,
                pagina("servico-" + slug + ".html", GovBrRepository.PREFIXO_SERVICO + slug));
    }

    @Test
    void leSeguroDesempregoComRequisitosDocumentosEtapasECanais() throws IOException {
        CatalogoGovBrDTO.ServicoLido s = servico("solicitar-o-seguro-desemprego");

        assertEquals("Solicitar o Seguro-Desemprego", s.nome());
        assertEquals("Ministério do Trabalho e Emprego", s.orgao());
        assertEquals("https://www.gov.br/pt-br/servicos/solicitar-o-seguro-desemprego", s.url());
        assertTrue(s.publico().startsWith("Trabalhadores formais que foram demitidos"));
        assertEquals(6, s.requisitos().size());
        assertEquals(3, s.etapas().size());
        assertTrue(s.etapas().get(0).startsWith("Solicitar o seguro-desemprego:"));
        List<String> docs = s.documentos().stream().map(CatalogoGovBrDTO.DocumentoLido::nome).toList();
        assertTrue(docs.contains("Número do CPF"));
        assertTrue(s.documentos().stream().allMatch(CatalogoGovBrDTO.DocumentoLido::obrigatorio));
        assertTrue(s.linksDigitais().contains("https://portalcidadao.dataprev.gov.br/#/trabalho"));
    }

    @Test
    void leCnhDigitalComLinkDoAplicativoERequisitos() throws IOException {
        CatalogoGovBrDTO.ServicoLido s = servico("emitir-a-carteira-nacional-de-habilitacao-digital-cnh-e");

        assertEquals("Emitir a Carteira Nacional de Habilitação Digital", s.nome());
        assertEquals("Ministério dos Transportes", s.orgao());
        // O primeiro link digital é o botão "Iniciar" da página
        assertEquals("https://www.gov.br/pt-br/apps/carteira-digital-de-transito-1", s.linksDigitais().get(0));
        assertTrue(s.requisitos().stream().anyMatch(r -> r.contains("CNH válida com QR Code")));
        assertEquals(3, s.etapas().size());
        // Texto de ajuda da seção de documentação não pode virar documento
        assertTrue(s.documentos().stream().noneMatch(d -> d.nome().startsWith("Em caso")));
    }

    @Test
    void leCarteiraDeTrabalhoComPublicoDaSecaoQuemPodeUtilizar() throws IOException {
        CatalogoGovBrDTO.ServicoLido s = servico("obter-a-carteira-de-trabalho");

        assertEquals("Qualquer pessoa inscrita no Cadastro de Pessoas Físicas - CPF.", s.publico());
        assertFalse(s.etapas().isEmpty());
        assertFalse(s.linksDigitais().isEmpty());
    }

    @Test
    void separaSinonimosDoNomeEUsaComoPalavrasChave() throws IOException {
        CatalogoGovBrDTO.ServicoLido s = servico("alistar-se-no-servico-militar-no-exterior");

        assertEquals("Alistar-se no serviço militar no exterior", s.nome());
        assertTrue(s.palavrasChave().contains("Alistamento"));
        assertTrue(s.palavrasChave().contains("Alistar no Serviço Militar"));
    }

    @Test
    void listaServicosDaCategoriaSemOsLinksFixosDoMenu() throws IOException {
        List<String> slugs = GovBrRepository.lerSlugs(pagina("categoria-transito.html",
                "https://www.gov.br/pt-br/categorias/transito-e-transportes/terrestre/transito"));

        assertEquals(10, slugs.size());
        assertTrue(slugs.contains("validar-cnh"));
        assertFalse(slugs.contains("buscar-servicos-por"));
        assertFalse(slugs.contains("servicos-por-publico-alvo"));
    }
}
