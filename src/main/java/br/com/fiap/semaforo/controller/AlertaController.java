package br.com.fiap.semaforo.controller;

import br.com.fiap.semaforo.dto.AlertaFiltro;
import br.com.fiap.semaforo.dto.AlertaRequest;
import br.com.fiap.semaforo.dto.AlertaResponse;
import br.com.fiap.semaforo.service.AlertaService;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/alertas")
public class AlertaController {

    private final AlertaService service;

    public AlertaController(AlertaService service) {
        this.service = service;
    }

    @GetMapping
    public PagedModel<EntityModel<AlertaResponse>> listar(@ParameterObject AlertaFiltro filtro,
            @PageableDefault(size = 10, sort = "criadoEm", direction = Sort.Direction.DESC) @ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<AlertaResponse> assembler) {
        return assembler.toModel(service.listar(filtro, pageable), Links::alerta);
    }

    @GetMapping("/{id}")
    public EntityModel<AlertaResponse> buscar(@PathVariable Long id) {
        return Links.alerta(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<AlertaResponse>> criar(@RequestBody @Valid AlertaRequest req) {
        var model = Links.alerta(service.criar(req));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    public EntityModel<AlertaResponse> atualizar(@PathVariable Long id, @RequestBody @Valid AlertaRequest req) {
        return Links.alerta(service.atualizar(id, req));
    }

    @PatchMapping("/{id}/encerrar")
    public EntityModel<AlertaResponse> encerrar(@PathVariable Long id) {
        return Links.alerta(service.encerrar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
