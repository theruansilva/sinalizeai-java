package br.com.fiap.semaforo.dto;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato único de erro de toda a API. */
public record ErroResponse(LocalDateTime timestamp, int status, String erro, String mensagem, String caminho,
                           Map<String, String> campos) {
    public ErroResponse(int status, String erro, String mensagem, String caminho) {
        this(LocalDateTime.now(), status, erro, mensagem, caminho, null);
    }
}
