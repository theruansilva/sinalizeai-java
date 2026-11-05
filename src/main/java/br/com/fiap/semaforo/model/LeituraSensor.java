package br.com.fiap.semaforo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leitura_sensor")
public class LeituraSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cruzamento_id", nullable = false)
    private Cruzamento cruzamento;

    @Column(name = "nivel_agua_cm", nullable = false)
    private Integer nivelAguaCm;

    @Column(nullable = false)
    private Integer carros;

    @Column(nullable = false)
    private Integer pedestres;

    @Column(name = "registrada_em", nullable = false)
    private LocalDateTime registradaEm = LocalDateTime.now();

    public Long getId() { return id; }
    public Cruzamento getCruzamento() { return cruzamento; }
    public void setCruzamento(Cruzamento cruzamento) { this.cruzamento = cruzamento; }
    public Integer getNivelAguaCm() { return nivelAguaCm; }
    public void setNivelAguaCm(Integer nivelAguaCm) { this.nivelAguaCm = nivelAguaCm; }
    public Integer getCarros() { return carros; }
    public void setCarros(Integer carros) { this.carros = carros; }
    public Integer getPedestres() { return pedestres; }
    public void setPedestres(Integer pedestres) { this.pedestres = pedestres; }
    public LocalDateTime getRegistradaEm() { return registradaEm; }
    public void setRegistradaEm(LocalDateTime registradaEm) { this.registradaEm = registradaEm; }
}
