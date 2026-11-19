package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository: acesso ao banco de categorias (Spring Data JPA gera o SQL)
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<Categoria> findAllByOrderByNomeAsc();
}
