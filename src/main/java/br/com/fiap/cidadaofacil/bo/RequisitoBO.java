package br.com.fiap.cidadaofacil.bo;

import br.com.fiap.cidadaofacil.dao.RequisitoDAO;
import br.com.fiap.cidadaofacil.dto.RequisitoDTO;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Requisito;
import br.com.fiap.cidadaofacil.model.Servico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// BO: regras de negócio de requisitos de um serviço
@Service
public class RequisitoBO {
    private final RequisitoDAO requisitoDAO;
    private final ServicoBO servicoBO;

    public RequisitoBO(RequisitoDAO requisitoDAO, ServicoBO servicoBO) {
        this.requisitoDAO = requisitoDAO;
        this.servicoBO = servicoBO;
    }

    @Transactional(readOnly = true)
    public List<RequisitoDTO.Response> listarDoServico(Long servicoId) {
        servicoBO.buscarEntidade(servicoId);
        return requisitoDAO.findByServicoIdOrderByIdAsc(servicoId).stream()
                .map(RequisitoDTO.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public RequisitoDTO.Response buscarPorId(Long id) {
        return RequisitoDTO.Response.de(buscarEntidade(id));
    }

    @Transactional
    public RequisitoDTO.Response adicionar(Long servicoId, RequisitoDTO.Request req) {
        Servico servico = servicoBO.buscarEntidade(servicoId);
        Requisito requisito = new Requisito(req.descricao().trim());
        servico.adicionarRequisito(requisito);
        return RequisitoDTO.Response.de(requisitoDAO.save(requisito));
    }

    @Transactional
    public RequisitoDTO.Response atualizar(Long id, RequisitoDTO.Request req) {
        Requisito requisito = buscarEntidade(id);
        requisito.setDescricao(req.descricao().trim());
        return RequisitoDTO.Response.de(requisitoDAO.save(requisito));
    }

    @Transactional
    public void excluir(Long id) {
        Requisito requisito = buscarEntidade(id);
        requisito.getServico().getRequisitos().remove(requisito);
        requisitoDAO.delete(requisito);
    }

    private Requisito buscarEntidade(Long id) {
        return requisitoDAO.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Requisito", id));
    }
}
