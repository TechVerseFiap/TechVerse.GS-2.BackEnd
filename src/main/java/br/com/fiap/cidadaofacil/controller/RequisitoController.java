package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.bo.RequisitoBO;
import br.com.fiap.cidadaofacil.dto.RequisitoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Endpoints REST de requisitos (criação via /api/servicos/{id}/requisitos)
@RestController
@RequestMapping("/api/requisitos")
@Tag(name = "Requisitos", description = "Critérios publicados pela fonte oficial")
public class RequisitoController {
    private final RequisitoBO requisitoBO;

    public RequisitoController(RequisitoBO requisitoBO) {
        this.requisitoBO = requisitoBO;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um requisito pelo id")
    public RequisitoDTO.Response buscar(@PathVariable Long id) {
        return requisitoBO.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um requisito")
    public RequisitoDTO.Response atualizar(@PathVariable Long id, @RequestBody @Valid RequisitoDTO.Request req) {
        return requisitoBO.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um requisito")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        requisitoBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
