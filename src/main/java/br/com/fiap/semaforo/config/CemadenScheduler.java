package br.com.fiap.semaforo.config;

import br.com.fiap.semaforo.service.CemadenSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Sincroniza alertas do CEMADEN periodicamente. Desligue com CEMADEN_ENABLED=false (ex.: demo offline). */
@Component
@EnableScheduling
@ConditionalOnProperty(name = "cemaden.enabled", havingValue = "true", matchIfMissing = true)
public class CemadenScheduler {

    private static final Logger log = LoggerFactory.getLogger(CemadenScheduler.class);

    private final CemadenSyncService sync;

    public CemadenScheduler(CemadenSyncService sync) {
        this.sync = sync;
    }

    @Scheduled(initialDelayString = "${cemaden.atraso-inicial:PT20S}", fixedDelayString = "${cemaden.intervalo:PT5M}")
    public void executar() {
        try {
            sync.sincronizar();
        } catch (Exception e) {
            // Falha na fonte externa não pode derrubar a API; alertas atuais continuam valendo (plano B: POST /alertas).
            log.warn("Falha ao sincronizar CEMADEN: {}", e.getMessage());
        }
    }
}
