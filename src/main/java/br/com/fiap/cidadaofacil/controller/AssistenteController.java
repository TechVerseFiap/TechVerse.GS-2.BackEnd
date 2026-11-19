package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.service.AssistenteService;
import br.com.fiap.cidadaofacil.dto.AssistenteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints REST de assistente, histórico e indicadores
@RestController
@RequestMapping("/api/assistente")
@Tag(name = "Assistente", description = "Pergunta em linguagem natural, recomendações e indicador de impacto")
public class AssistenteController {
    private final AssistenteService assistenteService;

    public AssistenteController(AssistenteService assistenteService) {
        this.assistenteService = assistenteService;
    }

    @PostMapping("/perguntas")
    @Operation(summary = "Envia uma pergunta ao assistente e recebe serviços recomendados")
    public AssistenteDTO.Resposta perguntar(@RequestBody @Valid AssistenteDTO.Pergunta pergunta) {
        return assistenteService.responder(pergunta);
    }

    @GetMapping("/interacoes")
    @Operation(summary = "Histórico de interações (sem o texto do cidadão)")
    public List<AssistenteDTO.InteracaoResponse> listarInteracoes() {
        return assistenteService.listarInteracoes();
    }

    @GetMapping("/interacoes/{id}")
    @Operation(summary = "Busca uma interação pelo id")
    public AssistenteDTO.InteracaoResponse buscarInteracao(@PathVariable Long id) {
        return assistenteService.buscarInteracao(id);
    }

    @DeleteMapping("/interacoes/{id}")
    @Operation(summary = "Exclui uma interação do histórico")
    public ResponseEntity<Void> excluirInteracao(@PathVariable Long id) {
        assistenteService.excluirInteracao(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/indicadores")
    @Operation(summary = "Indicador de impacto: taxa de buscas com orientação encontrada")
    public AssistenteDTO.Indicadores indicadores() {
        return assistenteService.indicadores();
    }
}
