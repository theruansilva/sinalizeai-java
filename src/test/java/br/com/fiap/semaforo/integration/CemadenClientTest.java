package br.com.fiap.semaforo.integration;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CemadenClientTest {

    private HttpServer servidor;

    private CemadenClient clienteRespondendo(String corpo) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", ex -> {
            byte[] b = corpo.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        servidor.start();
        return new CemadenClient("http://127.0.0.1:" + servidor.getAddress().getPort() + "/");
    }

    @AfterEach
    void parar() {
        servidor.stop(0);
    }

    @Test
    void formatoInesperadoFalhaEmVezDeVirarListaVazia() throws IOException {
        var cliente = clienteRespondendo("{\"dados\":[]}");
        assertThatThrownBy(cliente::buscarAtivos).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void painelSemAlertasDevolveListaVazia() throws IOException {
        assertThat(clienteRespondendo("{\"alertas\":[]}").buscarAtivos()).isEmpty();
    }
}
