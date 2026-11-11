package br.com.fiap.semaforo.controller;

import br.com.fiap.semaforo.dto.*;
import br.com.fiap.semaforo.service.CruzamentoService;
import br.com.fiap.semaforo.model.Modo;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/cruzamentos")
public class CruzamentoController {

    private final CruzamentoService service;
    private final PagedResourcesAssembler<LeituraResponse> pagedLeituras;

    public CruzamentoController(CruzamentoService service, PagedResourcesAssembler<LeituraResponse> pagedLeituras) {
        this.service = service;
        this.pagedLeituras = pagedLeituras;
    }

    @GetMapping
    public PagedModel<EntityModel<CruzamentoResponse>> listar(@RequestParam(required = false) Modo modo,
            @PageableDefault(size = 10, sort = "nome") @ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<CruzamentoResponse> assembler) {
        return assembler.toModel(service.listar(modo, pageable), Links::cruzamento);
    }

    @GetMapping("/em-risco")
    public CollectionModel<EntityModel<CruzamentoResponse>> emRisco(
            @RequestParam(defaultValue = "24") @Min(1) @Max(720) int horas) {
        return CollectionModel.of(service.emRisco(horas).stream().map(Links::cruzamento).toList(),
                linkTo(methodOn(CruzamentoController.class).emRisco(horas)).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<CruzamentoResponse> buscar(@PathVariable Long id) {
        return Links.cruzamento(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<CruzamentoResponse>> criar(@RequestBody @Valid CruzamentoRequest req) {
        var model = Links.cruzamento(service.criar(req));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    public EntityModel<CruzamentoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid CruzamentoRequest req) {
        return Links.cruzamento(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/modo")
    public ModoResponse modo(@PathVariable Long id) {
        return service.modo(id);
    }

    @PostMapping("/{id}/leituras")
    public ResponseEntity<EntityModel<LeituraResponse>> registrarLeitura(@PathVariable Long id,
                                                                         @RequestBody @Valid LeituraRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Links.leitura(service.registrarLeitura(id, req)));
    }

    @GetMapping("/{id}/leituras")
    public PagedModel<EntityModel<LeituraResponse>> leituras(@PathVariable Long id,
            @PageableDefault(size = 20, sort = "registradaEm", direction = Sort.Direction.DESC) @ParameterObject Pageable pageable) {
        return pagedLeituras.toModel(service.leituras(id, pageable), Links::leitura);
    }
}
