package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.service.EtapaService;
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
    private final EtapaService etapaService;

    public EtapaController(EtapaService etapaService) {
        this.etapaService = etapaService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma etapa pelo id")
    public EtapaDTO.Response buscar(@PathVariable Long id) {
        return etapaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma etapa")
    public EtapaDTO.Response atualizar(@PathVariable Long id, @RequestBody @Valid EtapaDTO.Request req) {
        return etapaService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma etapa")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        etapaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
