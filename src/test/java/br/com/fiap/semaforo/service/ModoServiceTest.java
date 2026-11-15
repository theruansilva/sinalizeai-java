package br.com.fiap.semaforo.service;

import br.com.fiap.semaforo.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModoServiceTest {

    private static Cruzamento cruzamento() {
        Cruzamento c = new Cruzamento();
        c.setLimiteAguaCm(30);
        return c;
    }

    private static LeituraSensor leitura(int agua, int pedestres) {
        LeituraSensor l = new LeituraSensor();
        l.setNivelAguaCm(agua);
        l.setCarros(0);
        l.setPedestres(pedestres);
        return l;
    }

    private static Alerta alerta(Severidade s) {
        Alerta a = new Alerta();
        a.setSeveridade(s);
        return a;
    }

    @Test
    void aguaExatamenteNoLimiteJaEntraEmAtencao() {
        Cruzamento c = cruzamento();
        ModoService.aplicar(c, leitura(29, 0), List.of());
        assertThat(c.getModo()).isEqualTo(Modo.NORMAL);
        ModoService.aplicar(c, leitura(30, 0), List.of());
        assertThat(c.getModo()).isEqualTo(Modo.ATENCAO);
        assertThat(c.getVerdeSeg()).isZero();
    }

    @Test
    void alertaAltoTemPrioridadeSobreAguaAlta() {
        Cruzamento c = cruzamento();
        ModoService.aplicar(c, leitura(80, 0), List.of(alerta(Severidade.MODERADA), alerta(Severidade.ALTA)));
        assertThat(c.getModo()).isEqualTo(Modo.EVACUACAO);
        assertThat(c.getVerdeSeg()).isEqualTo(ModoService.VERDE_EVACUACAO);
    }

    @Test
    void alertaModeradoSemAguaAltaColocaEmAtencao() {
        Cruzamento c = cruzamento();
        ModoService.aplicar(c, null, List.of(alerta(Severidade.MODERADA)));
        assertThat(c.getModo()).isEqualTo(Modo.ATENCAO);
    }

    @Test
    void pedestresAumentamVerdeAteOTeto() {
        Cruzamento c = cruzamento();
        ModoService.aplicar(c, leitura(0, 5), List.of());
        assertThat(c.getVerdeSeg()).isEqualTo(40);
        ModoService.aplicar(c, leitura(0, 50), List.of());
        assertThat(c.getVerdeSeg()).isEqualTo(ModoService.VERDE_MAXIMO);
    }

    @Test
    void niveisDoCemadenSaoMapeados() {
        assertThat(Severidade.deCemaden("Muito Alto")).isEqualTo(Severidade.MUITO_ALTA);
        assertThat(Severidade.deCemaden("Alto")).isEqualTo(Severidade.ALTA);
        assertThat(Severidade.deCemaden("Moderado")).isEqualTo(Severidade.MODERADA);
    }
}
