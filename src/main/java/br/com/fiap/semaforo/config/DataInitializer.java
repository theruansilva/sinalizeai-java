package br.com.fiap.semaforo.config;

import br.com.fiap.semaforo.dto.AlertaRequest;
import br.com.fiap.semaforo.dto.CruzamentoRequest;
import br.com.fiap.semaforo.dto.LeituraRequest;
import br.com.fiap.semaforo.model.Papel;
import br.com.fiap.semaforo.model.Severidade;
import br.com.fiap.semaforo.repository.CruzamentoRepository;
import br.com.fiap.semaforo.service.AlertaService;
import br.com.fiap.semaforo.service.CruzamentoService;
import br.com.fiap.semaforo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CruzamentoRepository repo;
    private final CruzamentoService cruzamentos;
    private final AlertaService alertas;
    private final UsuarioService usuarios;
    private final String senhaDemo;

    public DataInitializer(CruzamentoRepository repo, CruzamentoService cruzamentos, AlertaService alertas,
                           UsuarioService usuarios, @Value("${app.senha-demo:Semaforo@2026}") String senhaDemo) {
        this.repo = repo;
        this.cruzamentos = cruzamentos;
        this.alertas = alertas;
        this.usuarios = usuarios;
        this.senhaDemo = senhaDemo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        usuarios.criar("Admin Defesa Civil", "admin@semaforo.dev", senhaDemo, Papel.ADMIN);
        usuarios.criar("Operador CCO", "operador@semaforo.dev", senhaDemo, Papel.OPERADOR);
        usuarios.criar("Cidadã Teste", "cidadao@semaforo.dev", senhaDemo, Papel.CIDADAO);

        var mauaVoluntarios = cruzamentos.criar(new CruzamentoRequest(
                "Av. Mauá x R. Voluntários da Pátria", "Porto Alegre", "RS", -30.0277, -51.2287, 30));
        cruzamentos.criar(new CruzamentoRequest(
                "Av. Borges de Medeiros x Av. Salgado Filho", "Porto Alegre", "RS", -30.0310, -51.2270, 40));
        var sarandi = cruzamentos.criar(new CruzamentoRequest(
                "Av. Assis Brasil x Av. Sertório", "Porto Alegre", "RS", -29.9930, -51.1500, 25));
        cruzamentos.criar(new CruzamentoRequest(
                "Av. Getúlio Vargas x R. Bento Gonçalves", "Canoas", "RS", -29.9177, -51.1839, 35));
        cruzamentos.criar(new CruzamentoRequest(
                "R. Independência x R. Marechal Floriano", "Lajeado", "RS", -29.4669, -51.9614, 30));

        cruzamentos.registrarLeitura(mauaVoluntarios.id(), new LeituraRequest(12, 18, 6));
        cruzamentos.registrarLeitura(sarandi.id(), new LeituraRequest(41, 4, 1));
        alertas.criar(new AlertaRequest("Risco hidrológico - Rio Taquari",
                "Elevação do Rio Taquari acima da cota de inundação", Severidade.ALTA, "Lajeado", "RS", null));
    }
}
