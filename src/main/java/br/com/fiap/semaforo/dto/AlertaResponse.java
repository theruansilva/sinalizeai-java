package br.com.fiap.semaforo.dto;

import org.springframework.hateoas.server.core.Relation;
import br.com.fiap.semaforo.model.Alerta;
import br.com.fiap.semaforo.model.FonteAlerta;
import br.com.fiap.semaforo.model.Severidade;
import java.time.LocalDateTime;

@Relation(itemRelation = "alerta", collectionRelation = "alertas")
public record AlertaResponse(Long id, String titulo, String descricao, Severidade severidade, FonteAlerta fonte,
                             String municipio, String uf, Boolean ativo, LocalDateTime criadoEm, Long cruzamentoId) {
    public static AlertaResponse de(Alerta a) {
        return new AlertaResponse(a.getId(), a.getTitulo(), a.getDescricao(), a.getSeveridade(), a.getFonte(),
                a.getMunicipio(), a.getUf(), a.getAtivo(), a.getCriadoEm(),
                a.getCruzamento() == null ? null : a.getCruzamento().getId());
    }
}
