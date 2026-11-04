package com.bestxi.repository;

import com.bestxi.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    // Todas las incidencias (lesiones, sanciones...) de un jugador
    List<Incidencia> findByJugadorId(Long jugadorId);
}
