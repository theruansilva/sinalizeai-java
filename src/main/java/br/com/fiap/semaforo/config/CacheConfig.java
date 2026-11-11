package br.com.fiap.semaforo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    /** Modo de um cruzamento: consultado pelo gateway a cada ciclo do semáforo. */
    public static final String MODO = "modoCruzamento";
    /** Detalhe de um cruzamento: aberto toda vez que o cidadão toca no mapa do app. */
    public static final String CRUZAMENTO = "cruzamento";
}
