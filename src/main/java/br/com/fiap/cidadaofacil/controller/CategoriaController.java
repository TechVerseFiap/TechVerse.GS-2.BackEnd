package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.service.CategoriaService;
import br.com.fiap.cidadaofacil.service.ServicoService;
import br.com.fiap.cidadaofacil.dto.CategoriaDTO;
import br.com.fiap.cidadaofacil.dto.ServicoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Endpoints REST de categorias
@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Agrupamento dos serviços (Trabalho, Educação...)")
public class CategoriaController {
    private final CategoriaService categoriaService;
    private final ServicoService servicoService;

    public CategoriaController(CategoriaService categoriaService, ServicoService servicoService) {
        this.categoriaService = categoriaService;
        this.servicoService = servicoService;
    }

    @GetMapping
    @Operation(summary = "Lista todas as categorias")
    public List<CategoriaDTO.Response> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma categoria pelo id")
    public CategoriaDTO.Response buscar(@PathVariable Long id) {
        return categoriaService.buscarPorId(id);
    }

    @GetMapping("/{id}/servicos")
    @Operation(summary = "Lista os serviços de uma categoria")
    public List<ServicoDTO.Resumo> listarServicos(@PathVariable Long id) {
        return servicoService.listar(id, null);
    }

    @PostMapping
    @Operation(summary = "Cadastra uma categoria")
    public ResponseEntity<CategoriaDTO.Response> criar(@RequestBody @Valid CategoriaDTO.Request req) {
        CategoriaDTO.Response criada = categoriaService.criar(req);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma categoria")
    public CategoriaDTO.Response atualizar(@PathVariable Long id, @RequestBody @Valid CategoriaDTO.Request req) {
        return categoriaService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma categoria (somente se não tiver serviços)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
