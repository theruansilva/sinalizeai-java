package br.com.fiap.semaforo.repository;

import br.com.fiap.semaforo.model.Cruzamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CruzamentoRepository extends JpaRepository<Cruzamento, Long> {
    List<Cruzamento> findByMunicipioIgnoreCaseAndUfIgnoreCase(String municipio, String uf);
}
