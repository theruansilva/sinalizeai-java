package br.com.fiap.semaforo.repository;

import br.com.fiap.semaforo.model.Alerta;
import br.com.fiap.semaforo.model.FonteAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long>, JpaSpecificationExecutor<Alerta> {

    boolean existsByCodigoExterno(String codigoExterno);

    List<Alerta> findByFonteAndAtivoTrue(FonteAlerta fonte);

    @Query("""
            select a from Alerta a left join a.cruzamento c
            where a.ativo = true
              and (c.id = :cruzamentoId
                   or (c is null and upper(a.municipio) = upper(:municipio) and upper(a.uf) = upper(:uf)))
            """)
    List<Alerta> findAtivosDoCruzamento(@Param("cruzamentoId") Long cruzamentoId,
                                        @Param("municipio") String municipio, @Param("uf") String uf);

    @Modifying
    @Query("update Alerta a set a.cruzamento = null where a.cruzamento.id = :cruzamentoId")
    void desvincularDoCruzamento(@Param("cruzamentoId") Long cruzamentoId);
}
