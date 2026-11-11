package br.com.fiap.cidadaofacil.config;

import br.com.fiap.cidadaofacil.dao.CategoriaDAO;
import br.com.fiap.cidadaofacil.dao.DocumentoDAO;
import br.com.fiap.cidadaofacil.dao.ServicoDAO;
import br.com.fiap.cidadaofacil.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Insere o catálogo demonstrativo (fontes gov.br, 10/11/2026) quando o banco está vazio
@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final LocalDate VERIFICADO_EM = LocalDate.of(2026, 11, 10);

    private final CategoriaDAO categoriaDAO;
    private final DocumentoDAO documentoDAO;
    private final ServicoDAO servicoDAO;
    private final boolean habilitado;

    public DataSeeder(CategoriaDAO categoriaDAO, DocumentoDAO documentoDAO, ServicoDAO servicoDAO,
                      @Value("${app.seed.enabled:true}") boolean habilitado) {
        this.categoriaDAO = categoriaDAO;
        this.documentoDAO = documentoDAO;
        this.servicoDAO = servicoDAO;
        this.habilitado = habilitado;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!habilitado || categoriaDAO.count() > 0) {
            return;
        }
        log.info("Banco vazio: inserindo catálogo demonstrativo do Cidadão Fácil...");

        // Categorias
        Categoria trabalho = categoriaDAO.save(new Categoria("Trabalho",
                "Emprego, carteira de trabalho e apoio a quem perdeu o emprego."));
        Categoria educacao = categoriaDAO.save(new Categoria("Educação",
                "Cursos gratuitos e qualificação profissional."));
        Categoria documentos = categoriaDAO.save(new Categoria("Documentos",
                "Emissão de documentos pessoais."));
        Categoria beneficios = categoriaDAO.save(new Categoria("Benefícios",
                "Programas sociais e cadastros para acesso a benefícios."));
        categoriaDAO.save(new Categoria("Saúde",
                "Categoria reservada para a próxima versão do catálogo."));

        // Documentos
        Map<String, Documento> doc = documentoDAO.saveAll(List.of(
                new Documento("CPF", "Cadastro de Pessoa Física. Número usado para identificar você nos serviços públicos."),
                new Documento("Documento de identidade com foto", "RG, Carteira de Identidade Nacional (CIN) ou outro documento oficial com foto."),
                new Documento("Comprovante de residência", "Conta de luz, água ou similar, para mostrar onde você mora."),
                new Documento("Certidão de nascimento ou casamento", "Registro civil emitido pelo cartório."),
                new Documento("Requerimento do Seguro-Desemprego", "Formulário entregue pelo empregador no momento da demissão."),
                new Documento("Conta gov.br", "Login único do governo federal, criado com o CPF. Necessário para serviços digitais.")
        )).stream().collect(Collectors.toMap(Documento::getNome, Function.identity()));

        // Serviços com requisitos, etapas e documentos
        Servico ctps = servico(trabalho, "Carteira de Trabalho Digital",
                "Versão digital da carteira de trabalho. Mostra seus contratos e permite consultar vagas do Sine.",
                "Qualquer pessoa com CPF", "Ministério do Trabalho e Emprego", CanalAtendimento.DIGITAL,
                "https://www.gov.br/pt-br/servicos/obter-a-carteira-de-trabalho",
                "carteira de trabalho,ctps,carteira digital,registro em carteira,contrato de trabalho");
        ctps.adicionarRequisito(new Requisito("Ter CPF."));
        ctps.adicionarRequisito(new Requisito("Ter conta gov.br (nível bronze, prata ou ouro)."));
        ctps.adicionarEtapa(new Etapa(1, "Baixe o aplicativo Carteira de Trabalho Digital ou acesse pela web."));
        ctps.adicionarEtapa(new Etapa(2, "Entre com seu CPF e a senha da conta gov.br."));
        ctps.adicionarEtapa(new Etapa(3, "Confira seus contratos. Se algo estiver errado, procure a empresa ou o canal 158."));
        ctps.adicionarDocumento(doc.get("CPF"), null);
        ctps.adicionarDocumento(doc.get("Conta gov.br"), null);

        Servico seguro = servico(trabalho, "Seguro-Desemprego",
                "Ajuda financeira temporária para quem foi demitido sem justa causa.",
                "Trabalhadores com carteira assinada demitidos sem justa causa",
                "Ministério do Trabalho e Emprego", CanalAtendimento.DIGITAL_E_PRESENCIAL,
                "https://www.gov.br/pt-br/servicos/solicitar-o-seguro-desemprego",
                "seguro desemprego,desempregado,desempregada,demitido,demitida,demissao,perdi o emprego,mandado embora,sem justa causa");
        seguro.adicionarRequisito(new Requisito("Ter sido demitido sem justa causa."));
        seguro.adicionarRequisito(new Requisito("Ter recebido salário pelo tempo mínimo exigido (muda se é a 1ª, 2ª ou demais solicitações)."));
        seguro.adicionarRequisito(new Requisito("Não ter renda própria suficiente para o sustento da família."));
        seguro.adicionarEtapa(new Etapa(1, "Guarde o Requerimento do Seguro-Desemprego entregue pela empresa."));
        seguro.adicionarEtapa(new Etapa(2, "Solicite pelo app Carteira de Trabalho Digital, pelo gov.br ou com atendimento agendado."));
        seguro.adicionarEtapa(new Etapa(3, "Acompanhe a liberação das parcelas pelo app ou pelo gov.br."));
        seguro.adicionarDocumento(doc.get("Requerimento do Seguro-Desemprego"), "Entregue pelo empregador na demissão.");
        seguro.adicionarDocumento(doc.get("CPF"), null);
        seguro.adicionarDocumento(doc.get("Conta gov.br"), "Necessária para pedir pela internet.");

        Servico sine = servico(trabalho, "Buscar emprego no Sine",
                "Cadastro no Sistema Nacional de Emprego para receber e consultar vagas.",
                "Pessoas procurando trabalho com carteira assinada", "Ministério do Trabalho e Emprego",
                CanalAtendimento.DIGITAL_E_PRESENCIAL,
                "https://www.gov.br/pt-br/servicos/buscar-emprego-no-sistema-nacional-de-emprego-sine",
                "emprego,vaga,vagas,trabalho,sine,procurar emprego,curriculo,desempregado,desempregada");
        sine.adicionarRequisito(new Requisito("Ter CPF e conta gov.br para usar os canais digitais."));
        sine.adicionarEtapa(new Etapa(1, "Cadastre-se no Sine pelo Portal Emprega Brasil, app Carteira de Trabalho Digital ou posto do Sine."));
        sine.adicionarEtapa(new Etapa(2, "Mantenha seu currículo e perfil profissional atualizados."));
        sine.adicionarEtapa(new Etapa(3, "Consulte as vagas compatíveis com seu cadastro e candidate-se."));
        sine.adicionarDocumento(doc.get("CPF"), null);
        sine.adicionarDocumento(doc.get("Documento de identidade com foto"), "Para atendimento presencial.");

        Servico cursos = servico(educacao, "Cursos gratuitos - Escola do Trabalhador 4.0",
                "Cursos online e gratuitos de tecnologia e produtividade do programa Caminho Digital.",
                "Trabalhadores que querem se qualificar", "Ministério do Trabalho e Emprego", CanalAtendimento.DIGITAL,
                "https://www.gov.br/trabalho-e-emprego/pt-br/servicos/trabalhador/qualificacao-profissional/caminho-digital",
                "curso,gratuito,gratis,online,qualificacao,capacitacao,aprender,tecnologia,informatica,certificado");
        cursos.adicionarRequisito(new Requisito("Ter acesso à internet."));
        cursos.adicionarRequisito(new Requisito("Fazer cadastro na plataforma da Escola do Trabalhador 4.0."));
        cursos.adicionarEtapa(new Etapa(1, "Acesse a página do Caminho Digital e clique em 'Faça seu cadastro'."));
        cursos.adicionarEtapa(new Etapa(2, "Escolha um curso e estude no seu ritmo."));
        cursos.adicionarDocumento(doc.get("CPF"), null);

        Servico cin = servico(documentos, "Carteira de Identidade Nacional (CIN)",
                "Nova carteira de identidade, que usa o CPF como número. A primeira via é gratuita.",
                "Todos os brasileiros", "Institutos de identificação dos estados", CanalAtendimento.DIGITAL_E_PRESENCIAL,
                "https://www.gov.br/identidade",
                "rg,identidade,cin,carteira de identidade,documento de identidade,segunda via,tirar documento");
        cin.adicionarRequisito(new Requisito("Comparecer ao posto no dia agendado (coleta de biometria é presencial)."));
        cin.adicionarEtapa(new Etapa(1, "Acesse gov.br/identidade, escolha seu estado e agende o atendimento."));
        cin.adicionarEtapa(new Etapa(2, "Leve a certidão de nascimento ou casamento original no dia marcado."));
        cin.adicionarEtapa(new Etapa(3, "Depois de emitida, adicione a CIN digital no aplicativo gov.br."));
        cin.adicionarDocumento(doc.get("Certidão de nascimento ou casamento"), "Original.");
        cin.adicionarDocumento(doc.get("CPF"), null);

        Servico cadunico = servico(beneficios, "Inscrição no Cadastro Único (CadÚnico)",
                "Cadastro que dá acesso a vários programas sociais do governo.",
                "Famílias com renda de até meio salário mínimo por pessoa",
                "Prefeitura (CRAS / posto do Cadastro Único)", CanalAtendimento.PRESENCIAL,
                "https://www.gov.br/pt-br/servicos/inscrever-se-no-cadastro-unico-para-programas-sociais-do-governo-federal",
                "cadunico,cadastro unico,beneficio,bolsa familia,programa social,baixa renda,auxilio,cras");
        cadunico.adicionarRequisito(new Requisito("Renda familiar de até meio salário mínimo por pessoa (há exceções para programas específicos)."));
        cadunico.adicionarEtapa(new Etapa(1, "Encontre o CRAS ou posto do Cadastro Único mais próximo."));
        cadunico.adicionarEtapa(new Etapa(2, "Leve os documentos do responsável e de cada pessoa da família."));
        cadunico.adicionarEtapa(new Etapa(3, "Faça a entrevista (cerca de 1 hora, gratuita)."));
        cadunico.adicionarDocumento(doc.get("Documento de identidade com foto"), "Do responsável pela família.");
        cadunico.adicionarDocumento(doc.get("CPF"), "Preferencialmente de todos da família.");
        cadunico.adicionarDocumento(doc.get("Comprovante de residência"), "Ou declaração de residência.");

        servicoDAO.saveAll(List.of(ctps, seguro, sine, cursos, cin, cadunico));
        log.info("Catálogo demonstrativo inserido: {} serviços.", servicoDAO.count());
    }

    private Servico servico(Categoria categoria, String nome, String descricao, String publico, String orgao,
                            CanalAtendimento canal, String url, String palavras) {
        Servico s = new Servico();
        s.setCategoria(categoria);
        s.setNome(nome);
        s.setDescricao(descricao);
        s.setPublicoAlvo(publico);
        s.setOrgaoResponsavel(orgao);
        s.setCanal(canal);
        s.setUrlFonte(url);
        s.setDataVerificacao(VERIFICADO_EM);
        s.setPalavrasChave(palavras);
        return s;
    }
}
