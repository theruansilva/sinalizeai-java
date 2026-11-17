package br.com.fiap.semaforo.service;

import br.com.fiap.semaforo.model.Alerta;
import br.com.fiap.semaforo.model.Cruzamento;
import br.com.fiap.semaforo.model.LeituraSensor;
import br.com.fiap.semaforo.model.Modo;
import br.com.fiap.semaforo.model.Severidade;

import java.util.List;

public final class ModoService {

    public static final int VERDE_PADRAO = 30;
    public static final int VERDE_MAXIMO = 60;
    public static final int VERDE_EVACUACAO = 90;

    private ModoService() { }

    public static void aplicar(Cruzamento c, LeituraSensor ultimaLeitura, List<Alerta> alertasAtivos) {
        boolean evacuar = alertasAtivos.stream().anyMatch(a -> a.getSeveridade().exigeEvacuacao());
        boolean aguaAlta = ultimaLeitura != null && ultimaLeitura.getNivelAguaCm() >= c.getLimiteAguaCm();
        boolean alertaModerado = alertasAtivos.stream().anyMatch(a -> a.getSeveridade() == Severidade.MODERADA);

        if (evacuar) {
            c.setModo(Modo.EVACUACAO);
            c.setVerdeSeg(VERDE_EVACUACAO);
        } else if (aguaAlta || alertaModerado) {
            c.setModo(Modo.ATENCAO);
            c.setVerdeSeg(0);
        } else {
            int pedestres = ultimaLeitura == null ? 0 : ultimaLeitura.getPedestres();
            c.setModo(Modo.NORMAL);
            c.setVerdeSeg(Math.min(VERDE_PADRAO + 2 * pedestres, VERDE_MAXIMO));
        }
    }
}
