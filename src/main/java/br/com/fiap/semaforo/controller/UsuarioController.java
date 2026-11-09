package br.com.fiap.semaforo.controller;

import br.com.fiap.semaforo.dto.PapelRequest;
import br.com.fiap.semaforo.dto.UsuarioResponse;
import br.com.fiap.semaforo.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public Page<UsuarioResponse> listar(@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.listar(pageable);
    }

    @PatchMapping("/{id}/papel")
    public UsuarioResponse alterarPapel(@PathVariable Long id, @RequestBody @Valid PapelRequest req) {
        return service.alterarPapel(id, req.papel());
    }
}
