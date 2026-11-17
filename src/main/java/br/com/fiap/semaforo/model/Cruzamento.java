package br.com.fiap.semaforo.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cruzamento")
public class Cruzamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 80)
    private String municipio;

    @Column(nullable = false, length = 2)
    private String uf;

    private Double latitude;
    private Double longitude;

    @Column(name = "limite_agua_cm", nullable = false)
    private Integer limiteAguaCm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modo modo = Modo.NORMAL;

    @Column(name = "verde_seg", nullable = false)
    private Integer verdeSeg = 30;

    @OneToMany(mappedBy = "cruzamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeituraSensor> leituras = new ArrayList<>();

    @OneToMany(mappedBy = "cruzamento")
    private List<Alerta> alertas = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Integer getLimiteAguaCm() { return limiteAguaCm; }
    public void setLimiteAguaCm(Integer limiteAguaCm) { this.limiteAguaCm = limiteAguaCm; }
    public Modo getModo() { return modo; }
    public void setModo(Modo modo) { this.modo = modo; }
    public Integer getVerdeSeg() { return verdeSeg; }
    public void setVerdeSeg(Integer verdeSeg) { this.verdeSeg = verdeSeg; }
    public List<LeituraSensor> getLeituras() { return leituras; }
    public List<Alerta> getAlertas() { return alertas; }
}
