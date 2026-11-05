package br.com.fiap.semaforo.dto;

import jakarta.validation.constraints.*;

public record CruzamentoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 80) String municipio,
        @NotBlank @Size(min = 2, max = 2) String uf,
        @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @DecimalMin("-180") @DecimalMax("180") Double longitude,
        @NotNull @Min(1) @Max(500) Integer limiteAguaCm) {
}
