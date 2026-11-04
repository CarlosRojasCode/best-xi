package com.bestxi.repository;

import com.bestxi.model.Alineacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlineacionRepository extends JpaRepository<Alineacion, Long> {

    // Todas las alineaciones de un jugador, ordenadas por fecha de partido (la más reciente primero)
    List<Alineacion> findByJugadorIdOrderByPartidoFechaDesc(Long jugadorId);

    // Para saber si un partido ya tiene alineaciones guardadas, y no volver a pedirlas a la API
    boolean existsByPartidoId(Long partidoId);
}