package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.service.FonteService;
import br.com.fiap.cidadaofacil.dto.FonteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Endpoints das fontes oficiais usadas no catálogo e no panorama
@RestController
@RequestMapping("/api/fontes")
@Tag(name = "Fontes", description = "Referências oficiais usadas no catálogo e no panorama")
public class FonteController {
    private final FonteService fonteService;

    public FonteController(FonteService fonteService) {
        this.fonteService = fonteService;
    }

    @GetMapping
    @Operation(summary = "Lista as fontes oficiais com a data de consulta")
    public List<FonteDTO.Response> listar() {
        return fonteService.listar();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma fonte que não é mais usada por serviços nem indicadores")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        fonteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
