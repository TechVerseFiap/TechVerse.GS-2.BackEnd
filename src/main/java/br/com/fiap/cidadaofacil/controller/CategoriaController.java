package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.bo.CategoriaBO;
import br.com.fiap.cidadaofacil.bo.ServicoBO;
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
    private final CategoriaBO categoriaBO;
    private final ServicoBO servicoBO;

    public CategoriaController(CategoriaBO categoriaBO, ServicoBO servicoBO) {
        this.categoriaBO = categoriaBO;
        this.servicoBO = servicoBO;
    }

    @GetMapping
    @Operation(summary = "Lista todas as categorias")
    public List<CategoriaDTO.Response> listar() {
        return categoriaBO.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma categoria pelo id")
    public CategoriaDTO.Response buscar(@PathVariable Long id) {
        return categoriaBO.buscarPorId(id);
    }

    @GetMapping("/{id}/servicos")
    @Operation(summary = "Lista os serviços de uma categoria")
    public List<ServicoDTO.Resumo> listarServicos(@PathVariable Long id) {
        return servicoBO.listar(id, null);
    }

    @PostMapping
    @Operation(summary = "Cadastra uma categoria")
    public ResponseEntity<CategoriaDTO.Response> criar(@RequestBody @Valid CategoriaDTO.Request req) {
        CategoriaDTO.Response criada = categoriaBO.criar(req);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma categoria")
    public CategoriaDTO.Response atualizar(@PathVariable Long id, @RequestBody @Valid CategoriaDTO.Request req) {
        return categoriaBO.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma categoria (somente se não tiver serviços)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
