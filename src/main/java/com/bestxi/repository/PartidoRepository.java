package com.bestxi.repository;

import com.bestxi.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    Optional<Partido> findFirstByEquipoLocal_IdAndFechaAfterOrEquipoVisitante_IdAndFechaAfterOrderByFechaAsc(
        Long equipoLocalId,
        LocalDateTime fecha,
        Long equipoVisitanteId,
        LocalDateTime fecha2
);

}