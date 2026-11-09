package br.com.fiap.semaforo.dto;

import br.com.fiap.semaforo.model.Papel;

public record TokenResponse(String token, String tipo, long expiraEmSegundos, String email, Papel papel) {
}
