package br.com.fiap.cidadaofacil.service;

import br.com.fiap.cidadaofacil.repository.RequisitoRepository;
import br.com.fiap.cidadaofacil.dto.RequisitoDTO;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Requisito;
import br.com.fiap.cidadaofacil.model.Servico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service: regras de negócio de requisitos de um serviço
@Service
public class RequisitoService {
    private final RequisitoRepository requisitoRepository;
    private final ServicoService servicoService;

    public RequisitoService(RequisitoRepository requisitoRepository, ServicoService servicoService) {
        this.requisitoRepository = requisitoRepository;
        this.servicoService = servicoService;
    }

    @Transactional(readOnly = true)
    public List<RequisitoDTO.Response> listarDoServico(Long servicoId) {
        servicoService.buscarEntidade(servicoId);
        return requisitoRepository.findByServicoIdOrderByIdAsc(servicoId).stream()
                .map(RequisitoDTO.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public RequisitoDTO.Response buscarPorId(Long id) {
        return RequisitoDTO.Response.de(buscarEntidade(id));
    }

    @Transactional
    public RequisitoDTO.Response adicionar(Long servicoId, RequisitoDTO.Request req) {
        Servico servico = servicoService.buscarEntidade(servicoId);
        Requisito requisito = new Requisito(req.descricao().trim());
        servico.adicionarRequisito(requisito);
        return RequisitoDTO.Response.de(requisitoRepository.save(requisito));
    }

    @Transactional
    public RequisitoDTO.Response atualizar(Long id, RequisitoDTO.Request req) {
        Requisito requisito = buscarEntidade(id);
        requisito.setDescricao(req.descricao().trim());
        return RequisitoDTO.Response.de(requisitoRepository.save(requisito));
    }

    @Transactional
    public void excluir(Long id) {
        Requisito requisito = buscarEntidade(id);
        requisito.getServico().getRequisitos().remove(requisito);
        requisitoRepository.delete(requisito);
    }

    private Requisito buscarEntidade(Long id) {
        return requisitoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Requisito", id));
    }
}
