package com.bestxi.jugadores;

import com.bestxi.model.Jugador;
import com.bestxi.repository.JugadorRepository;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class JugadorController {

    private final JugadorRepository jugadorRepository;

    public JugadorController(JugadorRepository jugadorRepository) {
        this.jugadorRepository = jugadorRepository;
    }

    // Datos que enviaremos a React de cada jugador
    public record JugadorResumen(
            Long id,
            String nombre,
            String posicion,
            Long equipoId,
            String equipoNombre
    ) {
    }

    // GET /api/jugadores?nombre=Fekir
    @GetMapping("/api/jugadores")
    public List<JugadorResumen> buscarJugadores(
            @RequestParam(defaultValue = "") String nombre) {

        if (nombre.isBlank()) {
            return List.of();
        }

        List<Jugador> jugadores =
                jugadorRepository.findByNombreContainingIgnoreCase(
                        nombre.trim()
                );

        return jugadores.stream()
                .limit(10)
                .map(j -> new JugadorResumen(
                        j.getId(),
                        j.getNombre(),
                        j.getPosicion(),
                        j.getEquipo().getId(),
                        j.getEquipo().getNombre()
                ))
                .toList();
    }
}