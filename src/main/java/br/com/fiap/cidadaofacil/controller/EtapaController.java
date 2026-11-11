package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.bo.EtapaBO;
import br.com.fiap.cidadaofacil.dto.EtapaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Endpoints REST de etapas (criação via /api/servicos/{id}/etapas)
@RestController
@RequestMapping("/api/etapas")
@Tag(name = "Etapas", description = "Passo a passo para buscar o atendimento")
public class EtapaController {
    private final EtapaBO etapaBO;

    public EtapaController(EtapaBO etapaBO) {
        this.etapaBO = etapaBO;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma etapa pelo id")
    public EtapaDTO.Response buscar(@PathVariable Long id) {
        return etapaBO.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma etapa")
    public EtapaDTO.Response atualizar(@PathVariable Long id, @RequestBody @Valid EtapaDTO.Request req) {
        return etapaBO.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma etapa")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        etapaBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
