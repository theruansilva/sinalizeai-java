package br.com.fiap.semaforo.dto;

import br.com.fiap.semaforo.model.Modo;

/** Contrato consumido pelo gateway do semáforo: GET /cruzamentos/{id}/modo. */
public record ModoResponse(Modo modo, Integer verdeSeg) {
}
