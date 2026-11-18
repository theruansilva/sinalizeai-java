package br.com.fiap.semaforo.controller;

import br.com.fiap.semaforo.dto.PapelRequest;
import br.com.fiap.semaforo.dto.UsuarioResponse;
import br.com.fiap.semaforo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Usuários", description = "Gestão de papéis (somente ADMIN)")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Lista usuários", description = "Somente ADMIN. Paginada e ordenável.")
    @GetMapping
    public Page<UsuarioResponse> listar(@PageableDefault(size = 20, sort = "nome") @ParameterObject Pageable pageable) {
        return service.listar(pageable);
    }

    @Operation(summary = "Altera o papel de um usuário",
            description = "Somente ADMIN. Vale na próxima requisição do usuário, pois o filtro JWT recarrega o papel do banco. 400 se inválido; 404 se o id não existir.")
    @PatchMapping("/{id}/papel")
    public UsuarioResponse alterarPapel(@PathVariable Long id, @RequestBody @Valid PapelRequest req) {
        return service.alterarPapel(id, req.papel());
    }
}
