package br.com.fiap.cidadaofacil.bo;

import br.com.fiap.cidadaofacil.dao.FonteDAO;
import br.com.fiap.cidadaofacil.dto.FonteDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// BO: consulta das fontes oficiais (são criadas junto com serviços e indicadores)
@Service
public class FonteBO {
    private final FonteDAO fonteDAO;

    public FonteBO(FonteDAO fonteDAO) {
        this.fonteDAO = fonteDAO;
    }

    @Transactional(readOnly = true)
    public List<FonteDTO.Response> listar() {
        return fonteDAO.findAllByOrderByTituloAsc().stream().map(FonteDTO.Response::de).toList();
    }
}
