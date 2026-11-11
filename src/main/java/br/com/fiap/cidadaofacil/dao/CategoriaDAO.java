package br.com.fiap.cidadaofacil.dao;

import br.com.fiap.cidadaofacil.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// DAO: acesso ao banco de categorias (Spring Data JPA gera o SQL)
@Repository
public interface CategoriaDAO extends JpaRepository<Categoria, Long> {
    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<Categoria> findAllByOrderByNomeAsc();
}
