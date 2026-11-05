package br.com.fiap.semaforo.controller;

import br.com.fiap.semaforo.dto.*;
import br.com.fiap.semaforo.service.CruzamentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cruzamentos")
public class CruzamentoController {

    private final CruzamentoService service;

    public CruzamentoController(CruzamentoService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CruzamentoResponse> listar(@PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    public CruzamentoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<CruzamentoResponse> criar(@RequestBody @Valid CruzamentoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PutMapping("/{id}")
    public CruzamentoResponse atualizar(@PathVariable Long id, @RequestBody @Valid CruzamentoRequest req) {
        return service.atualizar(id, req);
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
    public ResponseEntity<LeituraResponse> registrarLeitura(@PathVariable Long id, @RequestBody @Valid LeituraRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarLeitura(id, req));
    }

    @GetMapping("/{id}/leituras")
    public Page<LeituraResponse> leituras(@PathVariable Long id,
            @PageableDefault(size = 20, sort = "registradaEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.leituras(id, pageable);
    }
}
