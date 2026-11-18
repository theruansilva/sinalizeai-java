package br.com.fiap.semaforo.controller;

import br.com.fiap.semaforo.dto.*;
import br.com.fiap.semaforo.service.CruzamentoService;
import br.com.fiap.semaforo.model.Modo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Cruzamentos", description = "Cruzamentos monitorados, modo do semáforo e leituras do sensor")
@RestController
@RequestMapping("/cruzamentos")
public class CruzamentoController {

    private final CruzamentoService service;
    private final PagedResourcesAssembler<LeituraResponse> pagedLeituras;

    public CruzamentoController(CruzamentoService service, PagedResourcesAssembler<LeituraResponse> pagedLeituras) {
        this.service = service;
        this.pagedLeituras = pagedLeituras;
    }

    @Operation(summary = "Lista cruzamentos",
            description = "Paginada e ordenável (page, size, sort), com filtro opcional por modo. Qualquer usuário autenticado. 400 se modo ou sort forem inválidos.")
    @GetMapping
    public PagedModel<EntityModel<CruzamentoResponse>> listar(
            @Parameter(description = "Filtra pelo modo atual do semáforo") @RequestParam(required = false) Modo modo,
            @PageableDefault(size = 10, sort = "nome") @ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<CruzamentoResponse> assembler) {
        return assembler.toModel(service.listar(modo, pageable), Links::cruzamento);
    }

    @Operation(summary = "Cruzamentos em risco",
            description = "Cruzamentos com alguma leitura de água acima do próprio limite nas últimas N horas (consulta JPQL). Qualquer usuário autenticado.")
    @GetMapping("/em-risco")
    public CollectionModel<EntityModel<CruzamentoResponse>> emRisco(
            @Parameter(description = "Janela em horas (1 a 720)") @RequestParam(defaultValue = "24") @Min(1) @Max(720) int horas) {
        return CollectionModel.of(service.emRisco(horas).stream().map(Links::cruzamento).toList(),
                linkTo(methodOn(CruzamentoController.class).emRisco(horas)).withSelfRel());
    }

    @Operation(summary = "Detalha um cruzamento",
            description = "Resposta cacheada, com links HATEOAS conforme o papel do token. 404 se o id não existir.")
    @GetMapping("/{id}")
    public EntityModel<CruzamentoResponse> buscar(@PathVariable Long id) {
        return Links.cruzamento(service.buscar(id));
    }

    @Operation(summary = "Cadastra cruzamento",
            description = "OPERADOR ou ADMIN. 201 com header Location; 400 se os dados forem inválidos.")
    @PostMapping
    public ResponseEntity<EntityModel<CruzamentoResponse>> criar(@RequestBody @Valid CruzamentoRequest req) {
        var model = Links.cruzamento(service.criar(req));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Operation(summary = "Atualiza cruzamento",
            description = "OPERADOR ou ADMIN. Recalcula o modo e invalida o cache. 400 se inválido, 404 se o id não existir.")
    @PutMapping("/{id}")
    public EntityModel<CruzamentoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid CruzamentoRequest req) {
        return Links.cruzamento(service.atualizar(id, req));
    }

    @Operation(summary = "Exclui cruzamento",
            description = "Somente ADMIN. Apaga as leituras (cascade) e desvincula os alertas. 204; 404 se o id não existir.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Modo atual do semáforo",
            description = "Público: consultado pelo dispositivo a cada ciclo. Devolve modo e tempo de verde; resposta cacheada. 404 se o id não existir.")
    @SecurityRequirements
    @GetMapping("/{id}/modo")
    public ModoResponse modo(@PathVariable Long id) {
        return service.modo(id);
    }

    @Operation(summary = "Registra leitura do sensor",
            description = "OPERADOR ou ADMIN (conta do gateway). Salva nível da água, carros e pedestres e recalcula o modo. 201; 400 se inválido; 404 se o cruzamento não existir.")
    @PostMapping("/{id}/leituras")
    public ResponseEntity<EntityModel<LeituraResponse>> registrarLeitura(@PathVariable Long id,
                                                                         @RequestBody @Valid LeituraRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Links.leitura(service.registrarLeitura(id, req)));
    }

    @Operation(summary = "Histórico de leituras",
            description = "Paginado, mais recentes primeiro. Qualquer usuário autenticado. 404 se o cruzamento não existir.")
    @GetMapping("/{id}/leituras")
    public PagedModel<EntityModel<LeituraResponse>> leituras(@PathVariable Long id,
            @PageableDefault(size = 20, sort = "registradaEm", direction = Sort.Direction.DESC) @ParameterObject Pageable pageable) {
        return pagedLeituras.toModel(service.leituras(id, pageable), Links::leitura);
    }
}
