package com.bestxi.jugadores;

import com.bestxi.model.Jugador;
import com.bestxi.repository.JugadorRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
import java.util.List;

@RestController
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
                        String equipoNombre) {
        }

        // GET /api/jugadores?nombre=Fekir
        @GetMapping("/api/jugadores")
        public List<JugadorResumen> buscarJugadores(
                        @RequestParam(defaultValue = "") String nombre) {

                if (nombre.isBlank()) {
                        return List.of();
                }

                // Lo que ha escrito el usuario, en minúsculas y sin tildes
                String buscado = normalizar(nombre.trim());

                // Se compara la versión «en limpio» de cada nombre, pero se devuelve el jugador
                // original
                List<Jugador> jugadores = jugadorRepository.findAll().stream()
                                .filter(j -> normalizar(j.getNombre()).contains(buscado))
                                .toList();

                return jugadores.stream()
                                .limit(10)
                                .map(j -> new JugadorResumen(
                                                j.getId(),
                                                j.getNombre(),
                                                j.getPosicion(),
                                                j.getEquipo().getId(),
                                                j.getEquipo().getNombre()))
                                .toList();
        }

        // Devuelve el texto en minúsculas y sin tildes, solo para comparar («A. Véliz»
        // → «a. veliz»).
        // No cambia nada de lo guardado en la base de datos.
        private static String normalizar(String texto) {
                return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
        }
}