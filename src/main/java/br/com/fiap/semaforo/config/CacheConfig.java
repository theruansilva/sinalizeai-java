package br.com.fiap.semaforo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String MODO = "modoCruzamento";
    public static final String CRUZAMENTO = "cruzamento";
}
