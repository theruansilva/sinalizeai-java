package br.com.fiap.semaforo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LeituraRequest(
        @NotNull @Min(0) @Max(1000) Integer nivelAguaCm,
        @NotNull @Min(0) Integer carros,
        @NotNull @Min(0) Integer pedestres) {
}
