package br.com.fiap.semaforo.controller;

import br.com.fiap.semaforo.dto.*;
import br.com.fiap.semaforo.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(@RequestBody @Valid CadastroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(req));
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest req) {
        return service.login(req);
    }
}
