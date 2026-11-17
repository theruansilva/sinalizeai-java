package br.com.fiap.semaforo.dto;

import br.com.fiap.semaforo.model.Modo;

public record ModoResponse(Modo modo, Integer verdeSeg) {
}
