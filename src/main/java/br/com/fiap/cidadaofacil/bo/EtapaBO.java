package br.com.fiap.cidadaofacil.bo;

import br.com.fiap.cidadaofacil.dao.EtapaDAO;
import br.com.fiap.cidadaofacil.dto.EtapaDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Etapa;
import br.com.fiap.cidadaofacil.model.Servico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// BO: regras de negócio de etapas (ordem única por serviço)
@Service
public class EtapaBO {
    private final EtapaDAO etapaDAO;
    private final ServicoBO servicoBO;

    public EtapaBO(EtapaDAO etapaDAO, ServicoBO servicoBO) {
        this.etapaDAO = etapaDAO;
        this.servicoBO = servicoBO;
    }

    @Transactional(readOnly = true)
    public List<EtapaDTO.Response> listarDoServico(Long servicoId) {
        servicoBO.buscarEntidade(servicoId);
        return etapaDAO.findByServicoIdOrderByOrdemAsc(servicoId).stream()
                .map(EtapaDTO.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EtapaDTO.Response buscarPorId(Long id) {
        return EtapaDTO.Response.de(buscarEntidade(id));
    }

    @Transactional
    public EtapaDTO.Response adicionar(Long servicoId, EtapaDTO.Request req) {
        Servico servico = servicoBO.buscarEntidade(servicoId);
        if (etapaDAO.existsByServicoIdAndOrdem(servicoId, req.ordem())) {
            throw new ConflitoException("Já existe uma etapa de ordem " + req.ordem() + " neste serviço.");
        }
        Etapa etapa = new Etapa(req.ordem(), req.descricao().trim());
        servico.adicionarEtapa(etapa);
        return EtapaDTO.Response.de(etapaDAO.save(etapa));
    }

    @Transactional
    public EtapaDTO.Response atualizar(Long id, EtapaDTO.Request req) {
        Etapa etapa = buscarEntidade(id);
        Long servicoId = etapa.getServico().getId();
        if (etapaDAO.existsByServicoIdAndOrdemAndIdNot(servicoId, req.ordem(), id)) {
            throw new ConflitoException("Já existe outra etapa de ordem " + req.ordem() + " neste serviço.");
        }
        etapa.setOrdem(req.ordem());
        etapa.setDescricao(req.descricao().trim());
        return EtapaDTO.Response.de(etapaDAO.save(etapa));
    }

    @Transactional
    public void excluir(Long id) {
        Etapa etapa = buscarEntidade(id);
        etapa.getServico().getEtapas().remove(etapa);
        etapaDAO.delete(etapa);
    }

    private Etapa buscarEntidade(Long id) {
        return etapaDAO.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Etapa", id));
    }
}
