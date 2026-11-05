package br.com.fiap.semaforo.controller;

import br.com.fiap.semaforo.dto.AlertaRequest;
import br.com.fiap.semaforo.dto.AlertaResponse;
import br.com.fiap.semaforo.service.AlertaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
    public Page<AlertaResponse> listar(
            @PageableDefault(size = 10, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    public AlertaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<AlertaResponse> criar(@RequestBody @Valid AlertaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PutMapping("/{id}")
    public AlertaResponse atualizar(@PathVariable Long id, @RequestBody @Valid AlertaRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/encerrar")
    public AlertaResponse encerrar(@PathVariable Long id) {
        return service.encerrar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
