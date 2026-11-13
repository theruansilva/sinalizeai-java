package br.com.fiap.semaforo.dto;

import org.springframework.hateoas.server.core.Relation;
import br.com.fiap.semaforo.model.LeituraSensor;
import java.time.LocalDateTime;

@Relation(itemRelation = "leitura", collectionRelation = "leituras")
public record LeituraResponse(Long id, Long cruzamentoId, Integer nivelAguaCm, Integer carros,
                              Integer pedestres, LocalDateTime registradaEm) {
    public static LeituraResponse de(LeituraSensor l) {
        return new LeituraResponse(l.getId(), l.getCruzamento().getId(), l.getNivelAguaCm(), l.getCarros(),
                l.getPedestres(), l.getRegistradaEm());
    }
}
