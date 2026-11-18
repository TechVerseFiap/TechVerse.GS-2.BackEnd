package br.com.fiap.cidadaofacil.dao;

import br.com.fiap.cidadaofacil.model.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// DAO: acesso ao banco de municípios do panorama (Spring Data JPA gera o SQL)
@Repository
public interface MunicipioDAO extends JpaRepository<Municipio, Long> {
    List<Municipio> findAllByOrderByNomeAsc();
}
