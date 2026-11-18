package br.com.fiap.semaforo;

import br.com.fiap.semaforo.integration.CemadenClient.AlertaCemaden;
import br.com.fiap.semaforo.service.CemadenSyncService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "cemaden.enabled=false")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiIntegrationTest {

    private static final String SENHA = "Semaforo@2026";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CemadenSyncService cemaden;

    private String token(String usuario) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + usuario + "@semaforo.dev\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }

    private long criarCruzamento(String municipio, String uf, int limite) throws Exception {
        String body = mvc.perform(post("/cruzamentos").header("Authorization", token("operador"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste\",\"municipio\":\"" + municipio + "\",\"uf\":\"" + uf
                                + "\",\"limiteAguaCm\":" + limite + "}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asLong();
    }

    @Test
    void permissoesPorPapel() throws Exception {
        String novo = "{\"nome\":\"X\",\"municipio\":\"Pelotas\",\"uf\":\"RS\",\"limiteAguaCm\":20}";
        mvc.perform(get("/cruzamentos")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mvc.perform(get("/cruzamentos/1/modo")).andExpect(status().isOk());
        mvc.perform(post("/cruzamentos").header("Authorization", token("cidadao"))
                .contentType(MediaType.APPLICATION_JSON).content(novo)).andExpect(status().isForbidden());
        mvc.perform(delete("/cruzamentos/1").header("Authorization", token("operador"))).andExpect(status().isForbidden());
        mvc.perform(get("/actuator/metrics").header("Authorization", token("operador"))).andExpect(status().isForbidden());
        mvc.perform(get("/actuator/metrics").header("Authorization", token("admin"))).andExpect(status().isOk());
        mvc.perform(get("/cruzamentos").header("Authorization", "Bearer token-falso")).andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroPublicoNaoPermiteVirarAdminEDuplicarEmail() throws Exception {
        String req = "{\"nome\":\"Ana\",\"email\":\"ana@x.com\",\"senha\":\"12345678\",\"papel\":\"ADMIN\"}";
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(req))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.papel").value("CIDADAO"))
                .andExpect(jsonPath("$.senha").doesNotExist());
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(req))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void leituraAcimaDoLimiteMudaModoMesmoComCache() throws Exception {
        long id = criarCruzamento("Pelotas", "RS", 30);
        mvc.perform(get("/cruzamentos/" + id + "/modo")).andExpect(jsonPath("$.modo").value("NORMAL"));
        mvc.perform(post("/cruzamentos/" + id + "/leituras").header("Authorization", token("operador"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nivelAguaCm\":42,\"carros\":7,\"pedestres\":3}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/cruzamentos/" + id + "/modo"))
                .andExpect(jsonPath("$.modo").value("ATENCAO")).andExpect(jsonPath("$.verdeSeg").value(0));
    }

    @Test
    void alertaMunicipalEvacuaEEncerrarVoltaAoNormal() throws Exception {
        long id = criarCruzamento("Pelotas", "RS", 30);
        String body = mvc.perform(post("/alertas").header("Authorization", token("operador"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Cheia\",\"severidade\":\"ALTA\",\"municipio\":\"pelotas\",\"uf\":\"rs\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$._links.encerrar").exists())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/cruzamentos/" + id + "/modo")).andExpect(jsonPath("$.modo").value("EVACUACAO"));

        long alerta = json.readTree(body).get("id").asLong();
        mvc.perform(patch("/alertas/" + alerta + "/encerrar").header("Authorization", token("operador")))
                .andExpect(status().isOk()).andExpect(jsonPath("$._links.encerrar").doesNotExist());
        mvc.perform(get("/cruzamentos/" + id + "/modo")).andExpect(jsonPath("$.modo").value("NORMAL"));
    }

    @Test
    void errosPadronizados() throws Exception {
        String admin = token("admin");
        mvc.perform(get("/cruzamentos/9999").header("Authorization", admin))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.mensagem").exists());
        mvc.perform(post("/cruzamentos").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"uf\":\"RSS\",\"limiteAguaCm\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists()).andExpect(jsonPath("$.campos.uf").exists())
                .andExpect(jsonPath("$.campos.limiteAguaCm").exists());
        mvc.perform(get("/alertas?severidade=INEXISTENTE").header("Authorization", admin))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/cruzamentos?sort=inexistente").header("Authorization", admin))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").exists());
        mvc.perform(get("/rota-inexistente").header("Authorization", admin))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.mensagem").value("Rota não encontrada"));
        mvc.perform(put("/alertas/1/encerrar").header("Authorization", admin))
                .andExpect(status().isMethodNotAllowed()).andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void filtrosDinamicosDeAlertas() throws Exception {
        String cidadao = token("cidadao");
        mvc.perform(get("/alertas?municipio=laje&ativo=true").header("Authorization", cidadao))
                .andExpect(jsonPath("$.page.totalElements").value(1));
        mvc.perform(get("/alertas?municipio=laje&severidade=BAIXA").header("Authorization", cidadao))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void cemadenImportaSoMunicipiosMonitoradosEEncerraOsQueSairam() throws Exception {
        long id = criarCruzamento("Teófilo Otoni", "MG", 30);
        var alto = new AlertaCemaden(1L, "Hidrológico - Alto", "Alto", 1, "MG", "TEÓFILO OTONI", "2026-11-17 10:00");
        var foraDaArea = new AlertaCemaden(2L, "Movimento de Massa", "Muito Alto", 1, "BA", "SALVADOR", "2026-11-17 10:00");

        var r = cemaden.aplicar(List.of(alto, foraDaArea));
        org.assertj.core.api.Assertions.assertThat(r.importados()).isEqualTo(1);
        mvc.perform(get("/cruzamentos/" + id + "/modo")).andExpect(jsonPath("$.modo").value("EVACUACAO"));

        org.assertj.core.api.Assertions.assertThat(cemaden.aplicar(List.of(alto)).importados()).isZero();

        org.assertj.core.api.Assertions.assertThat(cemaden.aplicar(List.of()).encerrados()).isEqualTo(1);
        mvc.perform(get("/cruzamentos/" + id + "/modo")).andExpect(jsonPath("$.modo").value("NORMAL"));
    }
}
