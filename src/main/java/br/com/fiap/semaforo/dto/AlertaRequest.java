package br.com.fiap.semaforo.dto;

import br.com.fiap.semaforo.model.Severidade;
import jakarta.validation.constraints.*;

public record AlertaRequest(
        @NotBlank @Size(max = 150) String titulo,
        @Size(max = 500) String descricao,
        @NotNull Severidade severidade,
        @NotBlank @Size(max = 80) String municipio,
        @NotBlank @Size(min = 2, max = 2) String uf,
        Long cruzamentoId) {
}
