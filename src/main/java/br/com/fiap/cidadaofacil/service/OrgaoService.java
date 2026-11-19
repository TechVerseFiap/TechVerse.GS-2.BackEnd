package br.com.fiap.cidadaofacil.service;

import br.com.fiap.cidadaofacil.repository.OrgaoRepository;
import br.com.fiap.cidadaofacil.repository.ServicoRepository;
import br.com.fiap.cidadaofacil.dto.OrgaoDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Orgao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service: regras de negócio de órgãos responsáveis (sigla única, exclusão só sem serviços)
@Service
public class OrgaoService {
    private final OrgaoRepository orgaoRepository;
    private final ServicoRepository servicoRepository;

    public OrgaoService(OrgaoRepository orgaoRepository, ServicoRepository servicoRepository) {
        this.orgaoRepository = orgaoRepository;
        this.servicoRepository = servicoRepository;
    }

    @Transactional(readOnly = true)
    public List<OrgaoDTO.Response> listar() {
        return orgaoRepository.findAllByOrderByNomeAsc().stream().map(OrgaoDTO.Response::de).toList();
    }

    @Transactional(readOnly = true)
    public OrgaoDTO.Response buscarPorId(Long id) {
        return OrgaoDTO.Response.de(buscarEntidade(id));
    }

    @Transactional
    public OrgaoDTO.Response criar(OrgaoDTO.Request req) {
        String sigla = req.sigla().trim().toUpperCase();
        if (orgaoRepository.existsBySiglaIgnoreCase(sigla)) {
            throw new ConflitoException("Já existe um órgão com a sigla '" + sigla + "'.");
        }
        return OrgaoDTO.Response.de(orgaoRepository.save(new Orgao(req.nome().trim(), sigla, req.esfera())));
    }

    @Transactional
    public OrgaoDTO.Response atualizar(Long id, OrgaoDTO.Request req) {
        Orgao orgao = buscarEntidade(id);
        String sigla = req.sigla().trim().toUpperCase();
        if (orgaoRepository.existsBySiglaIgnoreCaseAndIdNot(sigla, id)) {
            throw new ConflitoException("Já existe outro órgão com a sigla '" + sigla + "'.");
        }
        orgao.setNome(req.nome().trim());
        orgao.setSigla(sigla);
        orgao.setEsfera(req.esfera());
        return OrgaoDTO.Response.de(orgaoRepository.save(orgao));
    }

    @Transactional
    public void excluir(Long id) {
        Orgao orgao = buscarEntidade(id);
        long qtd = servicoRepository.countByOrgaoId(id);
        if (qtd > 0) {
            throw new ConflitoException("O órgão '" + orgao.getNome() + "' é responsável por " + qtd
                    + " serviço(s). Troque o órgão desses serviços antes de excluir.");
        }
        orgaoRepository.delete(orgao);
    }

    @Transactional(readOnly = true)
    public Orgao buscarEntidade(Long id) {
        return orgaoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Órgão", id));
    }
}
