package br.com.fiap.cidadaofacil.bo;

import br.com.fiap.cidadaofacil.dao.CategoriaDAO;
import br.com.fiap.cidadaofacil.dao.ServicoDAO;
import br.com.fiap.cidadaofacil.dto.CategoriaDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Categoria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// BO: regras de negócio de categorias
@Service
public class CategoriaBO {
    private final CategoriaDAO categoriaDAO;
    private final ServicoDAO servicoDAO;

    public CategoriaBO(CategoriaDAO categoriaDAO, ServicoDAO servicoDAO) {
        this.categoriaDAO = categoriaDAO;
        this.servicoDAO = servicoDAO;
    }

    // Consultas
    @Transactional(readOnly = true)
    public List<CategoriaDTO.Response> listar() {
        return categoriaDAO.findAllByOrderByNomeAsc().stream()
                .map(CategoriaDTO.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDTO.Response buscarPorId(Long id) {
        return CategoriaDTO.Response.de(buscarEntidade(id));
    }

    // Criação e atualização com nome único
    @Transactional
    public CategoriaDTO.Response criar(CategoriaDTO.Request req) {
        String nome = req.nome().trim();
        if (categoriaDAO.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe uma categoria com o nome '" + nome + "'.");
        }
        Categoria categoria = new Categoria(nome, req.descricao());
        return CategoriaDTO.Response.de(categoriaDAO.save(categoria));
    }

    @Transactional
    public CategoriaDTO.Response atualizar(Long id, CategoriaDTO.Request req) {
        Categoria categoria = buscarEntidade(id);
        String nome = req.nome().trim();
        if (categoriaDAO.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe outra categoria com o nome '" + nome + "'.");
        }
        categoria.setNome(nome);
        categoria.setDescricao(req.descricao());
        return CategoriaDTO.Response.de(categoriaDAO.save(categoria));
    }

    // Exclusão permitida só sem serviços vinculados
    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidade(id);
        long qtd = servicoDAO.countByCategoriaId(id);
        if (qtd > 0) {
            throw new ConflitoException("A categoria '" + categoria.getNome() + "' possui " + qtd
                    + " serviço(s) vinculado(s). Mova ou exclua os serviços antes.");
        }
        categoriaDAO.delete(categoria);
    }

    // Busca a entidade ou lança 404 (usado pelos outros BOs)
    @Transactional(readOnly = true)
    public Categoria buscarEntidade(Long id) {
        return categoriaDAO.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }
}
