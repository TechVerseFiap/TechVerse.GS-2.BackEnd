package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.model.Requisito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository: acesso ao banco de requisitos (Spring Data JPA gera o SQL)
@Repository
public interface RequisitoRepository extends JpaRepository<Requisito, Long> {
    List<Requisito> findByServicoIdOrderByIdAsc(Long servicoId);
}
