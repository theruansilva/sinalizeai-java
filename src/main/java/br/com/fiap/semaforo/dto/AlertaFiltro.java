package br.com.fiap.semaforo.dto;

import br.com.fiap.semaforo.model.FonteAlerta;
import br.com.fiap.semaforo.model.Severidade;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record AlertaFiltro(Severidade severidade, FonteAlerta fonte, Boolean ativo, String municipio, String uf,
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
                           Long cruzamentoId) {
}
