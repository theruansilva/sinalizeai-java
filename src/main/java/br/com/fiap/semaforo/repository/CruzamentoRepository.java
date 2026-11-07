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

    // Query methods derivados: filtros simples e fixos, o nome do método já é a consulta.
    Page<Cruzamento> findByModo(Modo modo, Pageable pageable);

    List<Cruzamento> findByMunicipioIgnoreCaseAndUfIgnoreCase(String municipio, String uf);

    /**
     * JPQL: compara a leitura com o limite do PRÓPRIO cruzamento (coluna x coluna) e usa subconsulta,
     * o que não dá para expressar com query method.
     */
    @Query("""
            select c from Cruzamento c
            where exists (select 1 from LeituraSensor l
                          where l.cruzamento = c and l.nivelAguaCm >= c.limiteAguaCm and l.registradaEm >= :desde)
            order by c.nome
            """)
    List<Cruzamento> findComAguaAcimaDoLimiteDesde(@Param("desde") LocalDateTime desde);
}
