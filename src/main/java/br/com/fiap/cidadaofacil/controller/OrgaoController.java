package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.service.OrgaoService;
import br.com.fiap.cidadaofacil.dto.OrgaoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Endpoints REST de órgãos responsáveis pelos serviços
@RestController
@RequestMapping("/api/orgaos")
@Tag(name = "Órgãos", description = "Órgãos responsáveis pelo atendimento (federal, estadual ou municipal)")
public class OrgaoController {
    private final OrgaoService orgaoService;

    public OrgaoController(OrgaoService orgaoService) {
        this.orgaoService = orgaoService;
    }

    @GetMapping
    @Operation(summary = "Lista os órgãos")
    public List<OrgaoDTO.Response> listar() {
        return orgaoService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um órgão pelo id")
    public OrgaoDTO.Response buscar(@PathVariable Long id) {
        return orgaoService.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um órgão (sigla única)")
    public ResponseEntity<OrgaoDTO.Response> criar(@RequestBody @Valid OrgaoDTO.Request req) {
        OrgaoDTO.Response criado = orgaoService.criar(req);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um órgão")
    public OrgaoDTO.Response atualizar(@PathVariable Long id, @RequestBody @Valid OrgaoDTO.Request req) {
        return orgaoService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um órgão (somente se não for responsável por serviços)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        orgaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
