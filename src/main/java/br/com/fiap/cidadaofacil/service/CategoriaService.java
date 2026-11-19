package br.com.fiap.cidadaofacil.service;

import br.com.fiap.cidadaofacil.repository.CategoriaRepository;
import br.com.fiap.cidadaofacil.repository.ServicoRepository;
import br.com.fiap.cidadaofacil.dto.CategoriaDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Categoria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service: regras de negócio de categorias
@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final ServicoRepository servicoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ServicoRepository servicoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.servicoRepository = servicoRepository;
    }

    // Consultas
    @Transactional(readOnly = true)
    public List<CategoriaDTO.Response> listar() {
        return categoriaRepository.findAllByOrderByNomeAsc().stream()
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
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe uma categoria com o nome '" + nome + "'.");
        }
        Categoria categoria = new Categoria(nome, descricaoOuNome(req.descricao(), nome));
        return CategoriaDTO.Response.de(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaDTO.Response atualizar(Long id, CategoriaDTO.Request req) {
        Categoria categoria = buscarEntidade(id);
        String nome = req.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe outra categoria com o nome '" + nome + "'.");
        }
        categoria.setNome(nome);
        categoria.setDescricao(descricaoOuNome(req.descricao(), nome));
        return CategoriaDTO.Response.de(categoriaRepository.save(categoria));
    }

    // Exclusão permitida só sem serviços vinculados
    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidade(id);
        long qtd = servicoRepository.countByCategoriaId(id);
        if (qtd > 0) {
            throw new ConflitoException("A categoria '" + categoria.getNome() + "' possui " + qtd
                    + " serviço(s) vinculado(s). Mova ou exclua os serviços antes.");
        }
        categoriaRepository.delete(categoria);
    }

    // A descrição é obrigatória no banco: se vier vazia, usa o próprio nome
    static String descricaoOuNome(String descricao, String nome) {
        return descricao == null || descricao.isBlank() ? nome : descricao.trim();
    }

    // Busca a entidade ou lança 404 (usado pelos outros services)
    @Transactional(readOnly = true)
    public Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }
}
