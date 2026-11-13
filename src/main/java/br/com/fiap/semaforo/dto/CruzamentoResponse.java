package br.com.fiap.semaforo.dto;

import org.springframework.hateoas.server.core.Relation;
import br.com.fiap.semaforo.model.Cruzamento;
import br.com.fiap.semaforo.model.Modo;

@Relation(itemRelation = "cruzamento", collectionRelation = "cruzamentos")
public record CruzamentoResponse(Long id, String nome, String municipio, String uf, Double latitude,
                                 Double longitude, Integer limiteAguaCm, Modo modo, Integer verdeSeg) {
    public static CruzamentoResponse de(Cruzamento c) {
        return new CruzamentoResponse(c.getId(), c.getNome(), c.getMunicipio(), c.getUf(), c.getLatitude(),
                c.getLongitude(), c.getLimiteAguaCm(), c.getModo(), c.getVerdeSeg());
    }
}
