package br.com.fiap.cidadaofacil.dao;

import br.com.fiap.cidadaofacil.model.ServicoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// DAO: acesso ao banco de vínculos serviço-documento (Spring Data JPA gera o SQL)
@Repository
public interface ServicoDocumentoDAO extends JpaRepository<ServicoDocumento, Long> {
    List<ServicoDocumento> findByServicoId(Long servicoId);

    Optional<ServicoDocumento> findByServicoIdAndDocumentoId(Long servicoId, Long documentoId);

    boolean existsByServicoIdAndDocumentoId(Long servicoId, Long documentoId);

    boolean existsByDocumentoId(Long documentoId);
}
