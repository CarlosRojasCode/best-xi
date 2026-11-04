package com.bestxi.repository;

import com.bestxi.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartidoRepository extends JpaRepository<Partido, Long> {

    List<Partido> findByEquipoLocal_IdOrEquipoVisitante_Id(
            Long equipoLocalId,
            Long equipoVisitanteId
    );

    List<Partido> findByJornadaAndEquipoLocal_IdOrJornadaAndEquipoVisitante_Id(
            Integer jornada,
            Long equipoLocalId,
            Integer jornada2,
            Long equipoVisitanteId
    );
}