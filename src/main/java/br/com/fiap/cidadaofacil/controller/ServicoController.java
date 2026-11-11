package br.com.fiap.cidadaofacil.controller;

import br.com.fiap.cidadaofacil.bo.EtapaBO;
import br.com.fiap.cidadaofacil.bo.RequisitoBO;
import br.com.fiap.cidadaofacil.bo.ServicoBO;
import br.com.fiap.cidadaofacil.dto.DocumentoDTO;
import br.com.fiap.cidadaofacil.dto.EtapaDTO;
import br.com.fiap.cidadaofacil.dto.RequisitoDTO;
import br.com.fiap.cidadaofacil.dto.ServicoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Endpoints REST de serviços e seus requisitos, etapas e documentos
@RestController
@RequestMapping("/api/servicos")
@Tag(name = "Serviços", description = "Catálogo de serviços públicos com requisitos, documentos e etapas")
public class ServicoController {
    private final ServicoBO servicoBO;
    private final RequisitoBO requisitoBO;
    private final EtapaBO etapaBO;

    public ServicoController(ServicoBO servicoBO, RequisitoBO requisitoBO, EtapaBO etapaBO) {
        this.servicoBO = servicoBO;
        this.requisitoBO = requisitoBO;
        this.etapaBO = etapaBO;
    }

    // Serviço
    @GetMapping
    @Operation(summary = "Lista serviços (filtros opcionais: ?categoria=1&busca=curso)")
    public List<ServicoDTO.Resumo> listar(@RequestParam(name = "categoria", required = false) Long categoriaId,
                                          @RequestParam(name = "busca", required = false) String busca) {
        return servicoBO.listar(categoriaId, busca);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe completo do serviço (requisitos, documentos, etapas e fonte)")
    public ServicoDTO.Detalhe detalhar(@PathVariable Long id) {
        return servicoBO.detalhar(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um serviço")
    public ResponseEntity<ServicoDTO.Detalhe> criar(@RequestBody @Valid ServicoDTO.Request req) {
        ServicoDTO.Detalhe criado = servicoBO.criar(req);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um serviço")
    public ServicoDTO.Detalhe atualizar(@PathVariable Long id, @RequestBody @Valid ServicoDTO.Request req) {
        return servicoBO.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um serviço (e seus requisitos, etapas e vínculos)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servicoBO.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // Requisitos do serviço
    @GetMapping("/{id}/requisitos")
    @Operation(summary = "Lista os requisitos do serviço")
    public List<RequisitoDTO.Response> listarRequisitos(@PathVariable Long id) {
        return requisitoBO.listarDoServico(id);
    }

    @PostMapping("/{id}/requisitos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona um requisito ao serviço")
    public RequisitoDTO.Response adicionarRequisito(@PathVariable Long id, @RequestBody @Valid RequisitoDTO.Request req) {
        return requisitoBO.adicionar(id, req);
    }

    // Etapas do serviço
    @GetMapping("/{id}/etapas")
    @Operation(summary = "Lista as etapas do serviço, em ordem")
    public List<EtapaDTO.Response> listarEtapas(@PathVariable Long id) {
        return etapaBO.listarDoServico(id);
    }

    @PostMapping("/{id}/etapas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona uma etapa ao serviço")
    public EtapaDTO.Response adicionarEtapa(@PathVariable Long id, @RequestBody @Valid EtapaDTO.Request req) {
        return etapaBO.adicionar(id, req);
    }

    // Documentos do serviço
    @GetMapping("/{id}/documentos")
    @Operation(summary = "Lista os documentos indicados para o serviço")
    public List<DocumentoDTO.DoServico> listarDocumentos(@PathVariable Long id) {
        return servicoBO.listarDocumentos(id);
    }

    @PostMapping("/{id}/documentos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Vincula um documento existente ao serviço")
    public DocumentoDTO.DoServico vincularDocumento(@PathVariable Long id, @RequestBody @Valid DocumentoDTO.Vinculo req) {
        return servicoBO.vincularDocumento(id, req);
    }

    @PutMapping("/{id}/documentos/{documentoId}")
    @Operation(summary = "Altera a observação do vínculo documento-serviço")
    public DocumentoDTO.DoServico atualizarVinculo(@PathVariable Long id, @PathVariable Long documentoId,
                                                   @RequestBody @Valid DocumentoDTO.Vinculo req) {
        return servicoBO.atualizarVinculoDocumento(id, documentoId, req);
    }

    @DeleteMapping("/{id}/documentos/{documentoId}")
    @Operation(summary = "Remove o vínculo de um documento com o serviço")
    public ResponseEntity<Void> desvincularDocumento(@PathVariable Long id, @PathVariable Long documentoId) {
        servicoBO.desvincularDocumento(id, documentoId);
        return ResponseEntity.noContent().build();
    }
}
