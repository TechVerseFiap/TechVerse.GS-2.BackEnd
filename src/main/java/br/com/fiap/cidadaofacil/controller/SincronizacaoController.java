package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.dto.SincronizacaoDTO;
import br.com.fiap.cidadaofacil.service.SincronizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Endpoints da sincronização com as fontes oficiais: catálogo do gov.br e Base dos Dados (BigQuery) -> Oracle
@RestController
@RequestMapping("/api/sincronizacao")
@Tag(name = "Sincronização", description = "Monta o catálogo a partir do gov.br e atualiza o panorama pela Base dos Dados")
public class SincronizacaoController {
    private final SincronizacaoService sincronizacaoService;

    public SincronizacaoController(SincronizacaoService sincronizacaoService) {
        this.sincronizacaoService = sincronizacaoService;
    }

    @GetMapping
    @Operation(summary = "Situação da integração: configurada, em andamento e resultado da última execução")
    public SincronizacaoDTO.Status status() {
        return sincronizacaoService.status();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Inicia em segundo plano: catálogo de serviços do gov.br e indicadores da Base dos Dados. "
            + "Acompanhe pelo GET /api/sincronizacao")
    public SincronizacaoDTO.Status sincronizar() {
        return sincronizacaoService.iniciar();
    }
}
