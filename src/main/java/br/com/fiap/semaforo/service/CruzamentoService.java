package br.com.fiap.semaforo.service;

import br.com.fiap.semaforo.dto.*;
import br.com.fiap.semaforo.exception.RecursoNaoEncontradoException;
import br.com.fiap.semaforo.model.Cruzamento;
import br.com.fiap.semaforo.model.LeituraSensor;
import br.com.fiap.semaforo.repository.AlertaRepository;
import br.com.fiap.semaforo.repository.CruzamentoRepository;
import br.com.fiap.semaforo.repository.LeituraSensorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CruzamentoService {

    private final CruzamentoRepository cruzamentos;
    private final LeituraSensorRepository leituras;
    private final AlertaRepository alertas;

    public CruzamentoService(CruzamentoRepository cruzamentos, LeituraSensorRepository leituras,
                             AlertaRepository alertas) {
        this.cruzamentos = cruzamentos;
        this.leituras = leituras;
        this.alertas = alertas;
    }

    @Transactional(readOnly = true)
    public Page<CruzamentoResponse> listar(Pageable pageable) {
        return cruzamentos.findAll(pageable).map(CruzamentoResponse::de);
    }

    @Transactional(readOnly = true)
    public CruzamentoResponse buscar(Long id) {
        return CruzamentoResponse.de(carregar(id));
    }

    @Transactional(readOnly = true)
    public ModoResponse modo(Long id) {
        Cruzamento c = carregar(id);
        return new ModoResponse(c.getModo(), c.getVerdeSeg());
    }

    @Transactional
    public CruzamentoResponse criar(CruzamentoRequest req) {
        Cruzamento c = new Cruzamento();
        preencher(c, req);
        cruzamentos.save(c);
        recalcular(c);
        return CruzamentoResponse.de(c);
    }

    @Transactional
    public CruzamentoResponse atualizar(Long id, CruzamentoRequest req) {
        Cruzamento c = carregar(id);
        preencher(c, req);
        recalcular(c);
        return CruzamentoResponse.de(c);
    }

    @Transactional
    public void excluir(Long id) {
        Cruzamento c = carregar(id);
        alertas.desvincularDoCruzamento(id);
        cruzamentos.delete(c);
    }

    @Transactional
    public LeituraResponse registrarLeitura(Long id, LeituraRequest req) {
        Cruzamento c = carregar(id);
        LeituraSensor l = new LeituraSensor();
        l.setCruzamento(c);
        l.setNivelAguaCm(req.nivelAguaCm());
        l.setCarros(req.carros());
        l.setPedestres(req.pedestres());
        leituras.save(l);
        recalcular(c);
        return LeituraResponse.de(l);
    }

    @Transactional(readOnly = true)
    public Page<LeituraResponse> leituras(Long id, Pageable pageable) {
        carregar(id);
        return leituras.findByCruzamentoId(id, pageable).map(LeituraResponse::de);
    }

    /** Reaplica a regra de modo com a leitura mais recente e os alertas ativos. */
    @Transactional
    public void recalcular(Cruzamento c) {
        ModoService.aplicar(c,
                leituras.findFirstByCruzamentoIdOrderByRegistradaEmDescIdDesc(c.getId()).orElse(null),
                alertas.findAtivosDoCruzamento(c.getId(), c.getMunicipio(), c.getUf()));
    }

    @Transactional
    public void recalcularMunicipio(String municipio, String uf) {
        cruzamentos.findByMunicipioIgnoreCaseAndUfIgnoreCase(municipio, uf).forEach(this::recalcular);
    }

    Cruzamento carregar(Long id) {
        return cruzamentos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cruzamento", id));
    }

    private static void preencher(Cruzamento c, CruzamentoRequest req) {
        c.setNome(req.nome());
        c.setMunicipio(req.municipio().trim().toUpperCase());
        c.setUf(req.uf().toUpperCase());
        c.setLatitude(req.latitude());
        c.setLongitude(req.longitude());
        c.setLimiteAguaCm(req.limiteAguaCm());
    }
}
