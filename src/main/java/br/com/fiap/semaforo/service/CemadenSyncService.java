package br.com.fiap.semaforo.service;

import br.com.fiap.semaforo.config.CacheConfig;
import br.com.fiap.semaforo.integration.CemadenClient;
import br.com.fiap.semaforo.integration.CemadenClient.AlertaCemaden;
import br.com.fiap.semaforo.model.Alerta;
import br.com.fiap.semaforo.model.FonteAlerta;
import br.com.fiap.semaforo.model.Severidade;
import br.com.fiap.semaforo.repository.AlertaRepository;
import br.com.fiap.semaforo.repository.CruzamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class CemadenSyncService {

    private static final Logger log = LoggerFactory.getLogger(CemadenSyncService.class);
    private static final String PREFIXO = "CEMADEN-";

    private final CemadenClient client;
    private final AlertaRepository alertas;
    private final CruzamentoRepository cruzamentos;
    private final CruzamentoService cruzamentoService;

    public CemadenSyncService(CemadenClient client, AlertaRepository alertas, CruzamentoRepository cruzamentos,
                              CruzamentoService cruzamentoService) {
        this.client = client;
        this.alertas = alertas;
        this.cruzamentos = cruzamentos;
        this.cruzamentoService = cruzamentoService;
    }

    public record Resultado(int recebidos, int importados, int encerrados) { }

    @CacheEvict(cacheNames = {CacheConfig.MODO, CacheConfig.CRUZAMENTO}, allEntries = true)
    @Transactional
    public Resultado sincronizar() {
        return aplicar(client.buscarAtivos());
    }

    @CacheEvict(cacheNames = {CacheConfig.MODO, CacheConfig.CRUZAMENTO}, allEntries = true)
    @Transactional
    public Resultado aplicar(List<AlertaCemaden> recebidos) {
        Set<String> monitorados = new HashSet<>();
        cruzamentos.findAll().forEach(c -> monitorados.add(chave(c.getMunicipio(), c.getUf())));

        Set<String> ativosNoPainel = new HashSet<>();
        Set<String> municipiosAfetados = new HashSet<>();
        int importados = 0;
        for (AlertaCemaden a : recebidos) {
            if (a.codigo() == null || a.municipio() == null || a.uf() == null) continue;
            if (a.status() != null && a.status() != 1) continue;
            String codigo = PREFIXO + a.codigo();
            ativosNoPainel.add(codigo);
            String chave = chave(a.municipio(), a.uf());
            if (!monitorados.contains(chave) || alertas.existsByCodigoExterno(codigo)) continue;

            Alerta novo = new Alerta();
            novo.setFonte(FonteAlerta.CEMADEN);
            novo.setCodigoExterno(codigo);
            novo.setTitulo(a.evento() == null ? "Alerta CEMADEN" : a.evento());
            novo.setDescricao("Alerta " + a.nivel() + " emitido pelo CEMADEN em " + a.criadoEm());
            novo.setSeveridade(Severidade.deCemaden(a.nivel()));
            novo.setMunicipio(a.municipio().trim().toUpperCase(Locale.ROOT));
            novo.setUf(a.uf().trim().toUpperCase(Locale.ROOT));
            alertas.save(novo);
            municipiosAfetados.add(chave);
            importados++;
        }

        int encerrados = 0;
        for (Alerta a : alertas.findByFonteAndAtivoTrue(FonteAlerta.CEMADEN)) {
            if (!ativosNoPainel.contains(a.getCodigoExterno())) {
                a.setAtivo(false);
                municipiosAfetados.add(chave(a.getMunicipio(), a.getUf()));
                encerrados++;
            }
        }

        alertas.flush();
        municipiosAfetados.forEach(k -> {
            String[] p = k.split("\\|");
            cruzamentoService.recalcularMunicipio(p[0], p[1]);
        });
        log.info("CEMADEN: {} alertas no painel, {} importados, {} encerrados", recebidos.size(), importados, encerrados);
        return new Resultado(recebidos.size(), importados, encerrados);
    }

    private static String chave(String municipio, String uf) {
        return municipio.trim().toUpperCase(Locale.ROOT) + "|" + uf.trim().toUpperCase(Locale.ROOT);
    }
}
