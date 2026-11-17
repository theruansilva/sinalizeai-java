package br.com.fiap.semaforo.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CemadenClient {

    private final RestClient http;

    public CemadenClient(@Value("${cemaden.url:https://painelalertas.cemaden.gov.br/wsAlertas2}") String url) {
        var fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(5_000);
        fabrica.setReadTimeout(20_000);
        this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica)
                .defaultHeader("User-Agent", "semaforo-api/1.0 (FIAP Global Solution)").build();
    }

    public List<AlertaCemaden> buscarAtivos() {
        Resposta r = http.get().retrieve().body(Resposta.class);
        return r == null || r.alertas() == null ? List.of() : r.alertas();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Resposta(List<AlertaCemaden> alertas) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AlertaCemaden(
            @JsonProperty("cod_alerta") Long codigo,
            String evento,
            String nivel,
            Integer status,
            String uf,
            String municipio,
            @JsonProperty("datahoracriacao") String criadoEm) {
    }
}
