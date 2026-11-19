package br.com.fiap.cidadaofacil.service;

import br.com.fiap.cidadaofacil.repository.EtapaRepository;
import br.com.fiap.cidadaofacil.dto.EtapaDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Etapa;
import br.com.fiap.cidadaofacil.model.Servico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service: regras de negócio de etapas (ordem única por serviço)
@Service
public class EtapaService {
    private final EtapaRepository etapaRepository;
    private final ServicoService servicoService;

    public EtapaService(EtapaRepository etapaRepository, ServicoService servicoService) {
        this.etapaRepository = etapaRepository;
        this.servicoService = servicoService;
    }

    @Transactional(readOnly = true)
    public List<EtapaDTO.Response> listarDoServico(Long servicoId) {
        servicoService.buscarEntidade(servicoId);
        return etapaRepository.findByServicoIdOrderByOrdemAsc(servicoId).stream()
                .map(EtapaDTO.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EtapaDTO.Response buscarPorId(Long id) {
        return EtapaDTO.Response.de(buscarEntidade(id));
    }

    @Transactional
    public EtapaDTO.Response adicionar(Long servicoId, EtapaDTO.Request req) {
        Servico servico = servicoService.buscarEntidade(servicoId);
        if (etapaRepository.existsByServicoIdAndOrdem(servicoId, req.ordem())) {
            throw new ConflitoException("Já existe uma etapa de ordem " + req.ordem() + " neste serviço.");
        }
        Etapa etapa = new Etapa(req.ordem(), req.descricao().trim());
        servico.adicionarEtapa(etapa);
        return EtapaDTO.Response.de(etapaRepository.save(etapa));
    }

    @Transactional
    public EtapaDTO.Response atualizar(Long id, EtapaDTO.Request req) {
        Etapa etapa = buscarEntidade(id);
        Long servicoId = etapa.getServico().getId();
        if (etapaRepository.existsByServicoIdAndOrdemAndIdNot(servicoId, req.ordem(), id)) {
            throw new ConflitoException("Já existe outra etapa de ordem " + req.ordem() + " neste serviço.");
        }
        etapa.setOrdem(req.ordem());
        etapa.setDescricao(req.descricao().trim());
        return EtapaDTO.Response.de(etapaRepository.save(etapa));
    }

    @Transactional
    public void excluir(Long id) {
        Etapa etapa = buscarEntidade(id);
        etapa.getServico().getEtapas().remove(etapa);
        etapaRepository.delete(etapa);
    }

    private Etapa buscarEntidade(Long id) {
        return etapaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Etapa", id));
    }
}
