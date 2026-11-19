package br.com.fiap.cidadaofacil.service;

import br.com.fiap.cidadaofacil.repository.FonteRepository;
import br.com.fiap.cidadaofacil.repository.IndicadorRepository;
import br.com.fiap.cidadaofacil.repository.ServicoRepository;
import br.com.fiap.cidadaofacil.dto.FonteDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Fonte;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service: consulta e limpeza das fontes oficiais (são criadas junto com serviços e indicadores)
@Service
public class FonteService {
    private final FonteRepository fonteRepository;
    private final ServicoRepository servicoRepository;
    private final IndicadorRepository indicadorRepository;

    public FonteService(FonteRepository fonteRepository, ServicoRepository servicoRepository, IndicadorRepository indicadorRepository) {
        this.fonteRepository = fonteRepository;
        this.servicoRepository = servicoRepository;
        this.indicadorRepository = indicadorRepository;
    }

    @Transactional(readOnly = true)
    public List<FonteDTO.Response> listar() {
        return fonteRepository.findAllByOrderByTituloAsc().stream().map(FonteDTO.Response::de).toList();
    }

    // Regra: só remove a fonte se nenhum serviço ou indicador ainda depender dela
    @Transactional
    public void excluir(Long id) {
        Fonte fonte = fonteRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Fonte", id));
        long usos = servicoRepository.countByFonteId(id) + indicadorRepository.countByFonteId(id);
        if (usos > 0) {
            throw new ConflitoException("A fonte '" + fonte.getTitulo() + "' ainda é usada por " + usos
                    + " registro(s) e não pode ser excluída.");
        }
        fonteRepository.delete(fonte);
    }
}
