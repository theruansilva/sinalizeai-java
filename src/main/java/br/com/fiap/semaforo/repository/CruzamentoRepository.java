package br.com.fiap.semaforo.repository;

import br.com.fiap.semaforo.model.Cruzamento;
import br.com.fiap.semaforo.model.Modo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CruzamentoRepository extends JpaRepository<Cruzamento, Long> {

    Page<Cruzamento> findByModo(Modo modo, Pageable pageable);

    List<Cruzamento> findByMunicipioIgnoreCaseAndUfIgnoreCase(String municipio, String uf);

    @Query("""
            select c from Cruzamento c
            where exists (select 1 from LeituraSensor l
                          where l.cruzamento = c and l.nivelAguaCm >= c.limiteAguaCm and l.registradaEm >= :desde)
            order by c.nome
            """)
    List<Cruzamento> findComAguaAcimaDoLimiteDesde(@Param("desde") LocalDateTime desde);
}
