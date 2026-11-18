package br.com.fiap.cidadaofacil.bo;

import br.com.fiap.cidadaofacil.dao.FonteDAO;
import br.com.fiap.cidadaofacil.dao.IndicadorDAO;
import br.com.fiap.cidadaofacil.dao.ServicoDAO;
import br.com.fiap.cidadaofacil.dto.FonteDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Fonte;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// BO: consulta e limpeza das fontes oficiais (são criadas junto com serviços e indicadores)
@Service
public class FonteBO {
    private final FonteDAO fonteDAO;
    private final ServicoDAO servicoDAO;
    private final IndicadorDAO indicadorDAO;

    public FonteBO(FonteDAO fonteDAO, ServicoDAO servicoDAO, IndicadorDAO indicadorDAO) {
        this.fonteDAO = fonteDAO;
        this.servicoDAO = servicoDAO;
        this.indicadorDAO = indicadorDAO;
    }

    @Transactional(readOnly = true)
    public List<FonteDTO.Response> listar() {
        return fonteDAO.findAllByOrderByTituloAsc().stream().map(FonteDTO.Response::de).toList();
    }

    // Regra: só remove a fonte se nenhum serviço ou indicador ainda depender dela
    @Transactional
    public void excluir(Long id) {
        Fonte fonte = fonteDAO.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Fonte", id));
        long usos = servicoDAO.countByFonteId(id) + indicadorDAO.countByFonteId(id);
        if (usos > 0) {
            throw new ConflitoException("A fonte '" + fonte.getTitulo() + "' ainda é usada por " + usos
                    + " registro(s) e não pode ser excluída.");
        }
        fonteDAO.delete(fonte);
    }
}
