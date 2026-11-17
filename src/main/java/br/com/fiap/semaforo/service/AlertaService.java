package br.com.fiap.semaforo.service;

import br.com.fiap.semaforo.dto.AlertaFiltro;
import br.com.fiap.semaforo.dto.AlertaRequest;
import br.com.fiap.semaforo.dto.AlertaResponse;
import br.com.fiap.semaforo.exception.RecursoNaoEncontradoException;
import br.com.fiap.semaforo.exception.RegraNegocioException;
import br.com.fiap.semaforo.model.Alerta;
import br.com.fiap.semaforo.model.Cruzamento;
import br.com.fiap.semaforo.model.FonteAlerta;
import br.com.fiap.semaforo.repository.AlertaRepository;
import br.com.fiap.semaforo.repository.AlertaSpecifications;
import br.com.fiap.semaforo.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertaService {

    private final AlertaRepository alertas;
    private final CruzamentoService cruzamentoService;

    public AlertaService(AlertaRepository alertas, CruzamentoService cruzamentoService) {
        this.alertas = alertas;
        this.cruzamentoService = cruzamentoService;
    }

    @Transactional(readOnly = true)
    public Page<AlertaResponse> listar(AlertaFiltro filtro, Pageable pageable) {
        return alertas.findAll(AlertaSpecifications.com(filtro), pageable).map(AlertaResponse::de);
    }

    @Transactional(readOnly = true)
    public AlertaResponse buscar(Long id) {
        return AlertaResponse.de(carregar(id));
    }

    @CacheEvict(cacheNames = {CacheConfig.MODO, CacheConfig.CRUZAMENTO}, allEntries = true)
    @Transactional
    public AlertaResponse criar(AlertaRequest req) {
        Alerta a = new Alerta();
        a.setFonte(FonteAlerta.OPERADOR);
        preencher(a, req);
        alertas.save(a);
        propagar(a);
        return AlertaResponse.de(a);
    }

    @CacheEvict(cacheNames = {CacheConfig.MODO, CacheConfig.CRUZAMENTO}, allEntries = true)
    @Transactional
    public AlertaResponse atualizar(Long id, AlertaRequest req) {
        Alerta a = carregar(id);
        if (a.getFonte() == FonteAlerta.CEMADEN) {
            throw new RegraNegocioException("Alertas do CEMADEN são sincronizados automaticamente e não podem ser editados");
        }
        String municipioAntigo = a.getMunicipio(), ufAntiga = a.getUf();
        Cruzamento cruzamentoAntigo = a.getCruzamento();
        preencher(a, req);
        cruzamentoService.recalcularMunicipio(municipioAntigo, ufAntiga);
        if (cruzamentoAntigo != null) cruzamentoService.recalcular(cruzamentoAntigo);
        propagar(a);
        return AlertaResponse.de(a);
    }

    @CacheEvict(cacheNames = {CacheConfig.MODO, CacheConfig.CRUZAMENTO}, allEntries = true)
    @Transactional
    public AlertaResponse encerrar(Long id) {
        Alerta a = carregar(id);
        a.setAtivo(false);
        propagar(a);
        return AlertaResponse.de(a);
    }

    @CacheEvict(cacheNames = {CacheConfig.MODO, CacheConfig.CRUZAMENTO}, allEntries = true)
    @Transactional
    public void excluir(Long id) {
        Alerta a = carregar(id);
        alertas.delete(a);
        alertas.flush();
        propagar(a);
    }

    void propagar(Alerta a) {
        alertas.flush();
        if (a.getCruzamento() != null) cruzamentoService.recalcular(a.getCruzamento());
        cruzamentoService.recalcularMunicipio(a.getMunicipio(), a.getUf());
    }

    private Alerta carregar(Long id) {
        return alertas.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Alerta", id));
    }

    private void preencher(Alerta a, AlertaRequest req) {
        a.setTitulo(req.titulo());
        a.setDescricao(req.descricao());
        a.setSeveridade(req.severidade());
        a.setMunicipio(req.municipio().trim().toUpperCase());
        a.setUf(req.uf().toUpperCase());
        a.setCruzamento(req.cruzamentoId() == null ? null : cruzamentoService.carregar(req.cruzamentoId()));
    }
}
