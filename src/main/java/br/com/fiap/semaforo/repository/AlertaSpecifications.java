package br.com.fiap.semaforo.repository;

import br.com.fiap.semaforo.dto.AlertaFiltro;
import br.com.fiap.semaforo.model.Alerta;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class AlertaSpecifications {

    private AlertaSpecifications() { }

    public static Specification<Alerta> com(AlertaFiltro f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.severidade() != null) p.add(cb.equal(root.get("severidade"), f.severidade()));
            if (f.fonte() != null) p.add(cb.equal(root.get("fonte"), f.fonte()));
            if (f.ativo() != null) p.add(cb.equal(root.get("ativo"), f.ativo()));
            if (f.uf() != null) p.add(cb.equal(cb.upper(root.get("uf")), f.uf().toUpperCase()));
            if (f.municipio() != null)
                p.add(cb.like(cb.upper(root.get("municipio")), "%" + f.municipio().toUpperCase() + "%"));
            if (f.desde() != null) p.add(cb.greaterThanOrEqualTo(root.get("criadoEm"), f.desde()));
            if (f.cruzamentoId() != null) p.add(cb.equal(root.get("cruzamento").get("id"), f.cruzamentoId()));
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
