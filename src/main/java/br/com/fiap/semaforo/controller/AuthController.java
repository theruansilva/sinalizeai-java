package br.com.fiap.semaforo.controller;

import br.com.fiap.semaforo.dto.*;
import br.com.fiap.semaforo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Cadastro público e login com JWT")
@SecurityRequirements
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastra cidadão",
            description = "Público. Sempre cria papel CIDADAO, com senha em BCrypt. 201; 400 se inválido; 422 se o e-mail já existir.")
    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(@RequestBody @Valid CadastroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(req));
    }

    @Operation(summary = "Login",
            description = "Público. Devolve o JWT (HS256) para usar em Authorization: Bearer. 401 se e-mail ou senha estiverem errados.")
    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest req) {
        return service.login(req);
    }
}
