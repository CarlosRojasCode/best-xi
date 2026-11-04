package com.bestxi.repository;

import com.bestxi.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    // Spring genera la consulta automáticamente a partir del nombre del método
    List<Jugador> findByNombreContainingIgnoreCase(String nombre);
}
