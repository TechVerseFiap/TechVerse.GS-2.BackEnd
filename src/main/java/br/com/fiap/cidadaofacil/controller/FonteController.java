package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.bo.FonteBO;
import br.com.fiap.cidadaofacil.dto.FonteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Endpoint de consulta das fontes oficiais usadas no catálogo e no panorama
@RestController
@RequestMapping("/api/fontes")
@Tag(name = "Fontes", description = "Referências oficiais usadas no catálogo e no panorama")
public class FonteController {
    private final FonteBO fonteBO;

    public FonteController(FonteBO fonteBO) {
        this.fonteBO = fonteBO;
    }

    @GetMapping
    @Operation(summary = "Lista as fontes oficiais com a data de consulta")
    public List<FonteDTO.Response> listar() {
        return fonteBO.listar();
    }
}
