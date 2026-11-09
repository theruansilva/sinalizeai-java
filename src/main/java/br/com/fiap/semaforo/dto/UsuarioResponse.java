package br.com.fiap.semaforo.dto;

import br.com.fiap.semaforo.model.Papel;
import br.com.fiap.semaforo.model.Usuario;
import java.time.LocalDateTime;

public record UsuarioResponse(Long id, String nome, String email, Papel papel, LocalDateTime criadoEm) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPapel(), u.getCriadoEm());
    }
}
