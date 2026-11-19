package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.model.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository: acesso ao banco de municípios do panorama (Spring Data JPA gera o SQL)
@Repository
public interface MunicipioRepository extends JpaRepository<Municipio, Long> {
    List<Municipio> findAllByOrderByNomeAsc();

    // Só os códigos IBGE: confere se um município já existe sem carregar as entidades
    @Query("SELECT m.codigoIbge FROM Municipio m")
    List<Long> listarCodigos();
}
