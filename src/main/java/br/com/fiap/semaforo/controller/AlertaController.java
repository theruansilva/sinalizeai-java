package br.com.fiap.semaforo.controller;

import br.com.fiap.semaforo.dto.AlertaFiltro;
import br.com.fiap.semaforo.dto.AlertaRequest;
import br.com.fiap.semaforo.dto.AlertaResponse;
import br.com.fiap.semaforo.service.AlertaService;
import br.com.fiap.semaforo.service.CemadenSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Alertas", description = "Alertas de enchente do CEMADEN e alertas manuais da Defesa Civil")
@RestController
@RequestMapping("/alertas")
public class AlertaController {

    private final AlertaService service;
    private final CemadenSyncService cemaden;

    public AlertaController(AlertaService service, CemadenSyncService cemaden) {
        this.service = service;
        this.cemaden = cemaden;
    }

    @Operation(summary = "Busca alertas com filtros dinâmicos",
            description = "Specification: só os filtros informados entram na consulta. Paginada e ordenável. Qualquer usuário autenticado.")
    @GetMapping
    public PagedModel<EntityModel<AlertaResponse>> listar(@ParameterObject AlertaFiltro filtro,
            @PageableDefault(size = 10, sort = "criadoEm", direction = Sort.Direction.DESC) @ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<AlertaResponse> assembler) {
        return assembler.toModel(service.listar(filtro, pageable), Links::alerta);
    }

    @Operation(summary = "Sincroniza com o CEMADEN agora",
            description = "Somente ADMIN. Importa alertas ativos dos municípios monitorados, encerra os que saíram do painel e recalcula os modos.")
    @PostMapping("/cemaden/sincronizar")
    public CemadenSyncService.Resultado sincronizarCemaden() {
        return cemaden.sincronizar();
    }

    @Operation(summary = "Detalha um alerta",
            description = "Com links HATEOAS conforme o papel. Qualquer usuário autenticado. 404 se o id não existir.")
    @GetMapping("/{id}")
    public EntityModel<AlertaResponse> buscar(@PathVariable Long id) {
        return Links.alerta(service.buscar(id));
    }

    @Operation(summary = "Cria alerta manual",
            description = "OPERADOR ou ADMIN (plano B da Defesa Civil). Sem cruzamentoId vale para o município todo. 201 com Location; 400 se inválido; 404 se o cruzamento não existir.")
    @PostMapping
    public ResponseEntity<EntityModel<AlertaResponse>> criar(@RequestBody @Valid AlertaRequest req) {
        var model = Links.alerta(service.criar(req));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Operation(summary = "Atualiza alerta manual",
            description = "OPERADOR ou ADMIN. 422 se o alerta veio do CEMADEN; 400 se inválido; 404 se o id não existir.")
    @PutMapping("/{id}")
    public EntityModel<AlertaResponse> atualizar(@PathVariable Long id, @RequestBody @Valid AlertaRequest req) {
        return Links.alerta(service.atualizar(id, req));
    }

    @Operation(summary = "Encerra alerta",
            description = "OPERADOR ou ADMIN. Marca como inativo e recalcula o modo dos cruzamentos afetados. 404 se o id não existir.")
    @PatchMapping("/{id}/encerrar")
    public EntityModel<AlertaResponse> encerrar(@PathVariable Long id) {
        return Links.alerta(service.encerrar(id));
    }

    @Operation(summary = "Exclui alerta",
            description = "Somente ADMIN. Recalcula o modo dos cruzamentos afetados. 204; 404 se o id não existir.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
