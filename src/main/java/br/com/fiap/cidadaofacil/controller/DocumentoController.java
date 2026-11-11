package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.bo.DocumentoBO;
import br.com.fiap.cidadaofacil.dto.DocumentoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Endpoints REST de documentos
@RestController
@RequestMapping("/api/documentos")
@Tag(name = "Documentos", description = "Tipos de documento que os serviços podem exigir")
public class DocumentoController {
    private final DocumentoBO documentoBO;

    public DocumentoController(DocumentoBO documentoBO) {
        this.documentoBO = documentoBO;
    }

    @GetMapping
    @Operation(summary = "Lista todos os documentos")
    public List<DocumentoDTO.Response> listar() {
        return documentoBO.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um documento pelo id")
    public DocumentoDTO.Response buscar(@PathVariable Long id) {
        return documentoBO.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um documento")
    public ResponseEntity<DocumentoDTO.Response> criar(@RequestBody @Valid DocumentoDTO.Request req) {
        DocumentoDTO.Response criado = documentoBO.criar(req);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um documento")
    public DocumentoDTO.Response atualizar(@PathVariable Long id, @RequestBody @Valid DocumentoDTO.Request req) {
        return documentoBO.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um documento (somente se não estiver vinculado a serviços)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        documentoBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
