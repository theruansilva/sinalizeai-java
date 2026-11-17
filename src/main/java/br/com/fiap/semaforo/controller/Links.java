package br.com.fiap.semaforo.controller;

import br.com.fiap.semaforo.dto.AlertaResponse;
import br.com.fiap.semaforo.dto.CruzamentoResponse;
import br.com.fiap.semaforo.dto.LeituraResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

final class Links {

    private Links() { }

    static EntityModel<CruzamentoResponse> cruzamento(CruzamentoResponse c) {
        var m = EntityModel.of(c,
                linkTo(methodOn(CruzamentoController.class).buscar(c.id())).withSelfRel(),
                linkModo(c.id()),
                linkTo(methodOn(CruzamentoController.class).leituras(c.id(), null)).withRel("leituras"),
                linkTo(AlertaController.class).slash("?cruzamentoId=" + c.id()).withRel("alertas"),
                linkTo(CruzamentoController.class).withRel("cruzamentos"));
        if (temPapel("OPERADOR", "ADMIN")) {
            m.add(linkTo(methodOn(CruzamentoController.class).atualizar(c.id(), null)).withRel("atualizar"));
            m.add(linkTo(methodOn(CruzamentoController.class).registrarLeitura(c.id(), null)).withRel("registrar-leitura"));
        }
        if (temPapel("ADMIN")) {
            m.add(linkTo(methodOn(CruzamentoController.class).excluir(c.id())).withRel("excluir"));
        }
        return m;
    }

    static EntityModel<AlertaResponse> alerta(AlertaResponse a) {
        var m = EntityModel.of(a,
                linkTo(methodOn(AlertaController.class).buscar(a.id())).withSelfRel(),
                linkTo(AlertaController.class).withRel("alertas"));
        if (a.cruzamentoId() != null) {
            m.add(linkTo(methodOn(CruzamentoController.class).buscar(a.cruzamentoId())).withRel("cruzamento"));
        }
        if (temPapel("OPERADOR", "ADMIN")) {
            m.add(linkTo(methodOn(AlertaController.class).atualizar(a.id(), null)).withRel("atualizar"));
            if (Boolean.TRUE.equals(a.ativo())) {
                m.add(linkTo(methodOn(AlertaController.class).encerrar(a.id())).withRel("encerrar"));
            }
        }
        if (temPapel("ADMIN")) {
            m.add(linkTo(methodOn(AlertaController.class).excluir(a.id())).withRel("excluir"));
        }
        return m;
    }

    static EntityModel<LeituraResponse> leitura(LeituraResponse l) {
        return EntityModel.of(l,
                linkTo(methodOn(CruzamentoController.class).buscar(l.cruzamentoId())).withRel("cruzamento"),
                linkModo(l.cruzamentoId()));
    }

    private static Link linkModo(Long cruzamentoId) {
        return linkTo(CruzamentoController.class).slash(cruzamentoId).slash("modo").withRel("modo");
    }

    private static boolean temPapel(String... papeis) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (var g : auth.getAuthorities()) {
            for (String p : papeis) if (g.getAuthority().equals("ROLE_" + p)) return true;
        }
        return false;
    }
}
