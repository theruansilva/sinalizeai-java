package br.com.fiap.semaforo.repository;

import br.com.fiap.semaforo.model.LeituraSensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeituraSensorRepository extends JpaRepository<LeituraSensor, Long> {
    Page<LeituraSensor> findByCruzamentoId(Long cruzamentoId, Pageable pageable);

    Optional<LeituraSensor> findFirstByCruzamentoIdOrderByRegistradaEmDescIdDesc(Long cruzamentoId);
}
