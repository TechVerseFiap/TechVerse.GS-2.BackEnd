package br.com.fiap.cidadaofacil.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// DTOs do catálogo do gov.br: o que é lido de cada página de serviço e o resumo da sincronização
public final class CatalogoGovBrDTO {
    private CatalogoGovBrDTO() {
    }

    // Documento citado em uma etapa; "obrigatorio" = documentação em comum para todos os casos
    public record DocumentoLido(String nome, String descricao, boolean obrigatorio, String etapa) {
    }

    // Serviço lido de https://www.gov.br/pt-br/servicos/{slug}
    public record ServicoLido(String slug, String url, String nome, String descricao, String orgao, String publico,
                              List<String> requisitos, List<String> etapas, List<DocumentoLido> documentos,
                              List<String> linksDigitais, List<String> enderecosPresenciais,
                              List<String> palavrasChave) {
    }

    // Resumo da sincronização: quantos serviços foram encontrados, criados, atualizados, pulados ou com erro
    public record Resultado(LocalDateTime inicio, LocalDateTime fim, int encontrados, int novos, int atualizados,
                            int pulados, int erros, Map<String, Integer> porCategoria) {
    }
}
