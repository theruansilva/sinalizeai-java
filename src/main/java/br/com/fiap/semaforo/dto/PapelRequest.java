package br.com.fiap.semaforo.dto;

import br.com.fiap.semaforo.model.Papel;
import jakarta.validation.constraints.NotNull;

public record PapelRequest(@NotNull Papel papel) {
}
