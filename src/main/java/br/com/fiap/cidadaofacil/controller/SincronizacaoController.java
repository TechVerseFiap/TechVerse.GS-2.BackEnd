package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.dto.SincronizacaoDTO;
import br.com.fiap.cidadaofacil.service.SincronizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoints da sincronização do panorama com a Base dos Dados (BigQuery -> Oracle)
@RestController
@RequestMapping("/api/sincronizacao")
@Tag(name = "Base dos Dados", description = "Atualiza os indicadores do panorama com SQL na Base dos Dados (BigQuery)")
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
    @Operation(summary = "Consulta a Base dos Dados e grava os valores mais recentes no Oracle (pode levar 1 a 2 minutos)")
    public SincronizacaoDTO.Resultado sincronizar() {
        return sincronizacaoService.sincronizar();
    }
}
