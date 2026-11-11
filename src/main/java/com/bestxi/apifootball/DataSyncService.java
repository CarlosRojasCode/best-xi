package com.bestxi.apifootball;

import com.bestxi.apifootball.dto.FixtureResponse;
import com.bestxi.apifootball.dto.InjuryResponse;
import com.bestxi.apifootball.dto.LineupResponse;
import com.bestxi.model.*;
import com.bestxi.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataSyncService {

    private final ApiFootballClient client;
    private final EquipoRepository equipoRepository;
    private final JugadorRepository jugadorRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final PartidoRepository partidoRepository;
    private final AlineacionRepository alineacionRepository;
    private final IncidenciaRepository incidenciaRepository;

    public DataSyncService(ApiFootballClient client, EquipoRepository equipoRepository,
                            JugadorRepository jugadorRepository, EntrenadorRepository entrenadorRepository,
                            PartidoRepository partidoRepository, AlineacionRepository alineacionRepository,
                            IncidenciaRepository incidenciaRepository) {
        this.client = client;
        this.equipoRepository = equipoRepository;
        this.jugadorRepository = jugadorRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.partidoRepository = partidoRepository;
        this.alineacionRepository = alineacionRepository;
        this.incidenciaRepository = incidenciaRepository;
    }

    // Paso 1: importa los partidos de un equipo en una liga/temporada, creando también los equipos rivales
    @Transactional
    public void importarPartidos(int liga, int temporada, long equipoId) {
        FixtureResponse respuesta = client.obtenerPartidos(liga, temporada, equipoId);

        for (FixtureResponse.FixtureData datos : respuesta.response()) {
            Equipo local = obtenerOCrearEquipo(datos.teams().home());
            Equipo visitante = obtenerOCrearEquipo(datos.teams().away());

            if (partidoRepository.existsById(datos.fixture().id())) {
                continue; // ya lo teníamos, no hacemos nada más
            }

            Partido partido = new Partido();
            partido.setId(datos.fixture().id());
            partido.setEquipoLocal(local);
            partido.setEquipoVisitante(visitante);
            partido.setFecha(OffsetDateTime.parse(datos.fixture().date()).toLocalDateTime());
            partido.setJornada(extraerNumeroJornada(datos.league().round()));

            partidoRepository.save(partido);
        }
    }

    // Importa las alineaciones de TODOS los partidos ya guardados de un equipo, saltándose los que ya tengan datos
    public void importarAlineacionesDeEquipo(long equipoId) {
        List<Partido> partidos = partidoRepository.findByEquipoLocal_IdOrEquipoVisitante_Id(equipoId, equipoId);

        for (Partido partido : partidos) {
            if (alineacionRepository.existsByPartidoId(partido.getId())) {
                continue; // ya lo teníamos, no gastamos otra petición a la API
            }

            try {
                importarAlineaciones(partido.getId());
            } catch (HttpClientErrorException.TooManyRequests e) {
                // Nos hemos pasado del límite por minuto: esperamos más y lo intentamos una vez más
                dormir(15000);
                try {
                    importarAlineaciones(partido.getId());
                } catch (Exception e2) {
                    // Si sigue fallando, lo saltamos y seguimos con el resto en vez de morir aquí
                    continue;
                }
            }

            dormir(3000); // pausa entre peticiones para no volver a pasarnos
        }
    }

    private void dormir(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Paso 2: importa la alineación (titulares y suplentes) de un partido concreto
    @Transactional
    public void importarAlineaciones(long partidoId) {
        if (alineacionRepository.existsByPartidoId(partidoId)) {
            return; // ya importado, no lo dupliques
        }

        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalStateException("Primero hay que importar el partido " + partidoId));

        LineupResponse respuesta = client.obtenerAlineaciones(partidoId);

        for (LineupResponse.LineupData datosEquipo : respuesta.response()) {
            Equipo equipo = obtenerOCrearEquipo(datosEquipo.team());
            String ladoEquipo = equipo.getId().equals(partido.getEquipoLocal().getId()) ? "local" : "visitante";

            Entrenador entrenador = obtenerOCrearEntrenador(datosEquipo.coach(), equipo);

            for (LineupResponse.PlayerSlot slot : datosEquipo.startXI()) {
                guardarAlineacion(partido, equipo, entrenador, slot, ladoEquipo, datosEquipo.formation(), true);
            }
            for (LineupResponse.PlayerSlot slot : datosEquipo.substitutes()) {
                guardarAlineacion(partido, equipo, entrenador, slot, ladoEquipo, datosEquipo.formation(), false);
            }
        }
    }

    // Paso 3: importa lesiones/sanciones/convocatorias de un equipo en una liga/temporada
    @Transactional
    public void importarIncidencias(int liga, int temporada, long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new IllegalStateException("Primero hay que importar el equipo " + equipoId));

        InjuryResponse respuesta = client.obtenerIncidencias(liga, temporada, equipoId);

        for (InjuryResponse.InjuryData datos : respuesta.response()) {
            // Si el partido no lo tenemos importado, nos lo saltamos (no podemos guardar la incidencia sin partido)
            Partido partido = partidoRepository.findById(datos.fixture().id()).orElse(null);
            if (partido == null) {
                continue;
            }

            Jugador jugador = obtenerOCrearJugador(datos.player().id(), datos.player().name(), equipo, null, null);

            Incidencia incidencia = new Incidencia();
            incidencia.setJugador(jugador);
            incidencia.setPartido(partido);
            incidencia.setTipo(datos.player().type());
            incidencia.setMotivo(datos.player().reason());

            incidenciaRepository.save(incidencia);
        }
    }

    // Paso 4: rellena posición y dorsal de los jugadores de un partido SIN volver a guardar la alineación
    @Transactional
    public void completarDatosJugadores(long partidoId) {
        LineupResponse respuesta = client.obtenerAlineaciones(partidoId);

        for (LineupResponse.LineupData datosEquipo : respuesta.response()) {
            Equipo equipo = obtenerOCrearEquipo(datosEquipo.team());

            // obtenerOCrearJugador solo rellena lo que está vacío; no guardamos filas de alineacion
            for (LineupResponse.PlayerSlot slot : datosEquipo.startXI()) {
                obtenerOCrearJugador(
                        slot.player().id(), slot.player().name(), equipo,
                        slot.player().pos(), slot.player().number()
                );
            }

            for (LineupResponse.PlayerSlot slot : datosEquipo.substitutes()) {
                obtenerOCrearJugador(
                        slot.player().id(), slot.player().name(), equipo,
                        slot.player().pos(), slot.player().number()
                );
            }
        }
    }

    // --- Métodos de apoyo, para no repetir código ---

    private Equipo obtenerOCrearEquipo(FixtureResponse.TeamRef ref) {
        return equipoRepository.findById(ref.id()).orElseGet(() -> {
            Equipo equipo = new Equipo();
            equipo.setId(ref.id());
            equipo.setNombre(ref.name());
            equipo.setEscudoUrl(ref.logo());
            return equipoRepository.save(equipo);
        });
    }

    private Entrenador obtenerOCrearEntrenador(LineupResponse.CoachRef ref, Equipo equipo) {
        return entrenadorRepository.findById(ref.id()).orElseGet(() -> {
            Entrenador entrenador = new Entrenador();
            entrenador.setId(ref.id());
            entrenador.setEquipo(equipo);
            entrenador.setNombre(ref.name());
            return entrenadorRepository.save(entrenador);
        });
    }

    private Jugador obtenerOCrearJugador(
        Long id,
        String nombre,
        Equipo equipo,
        String posicion,
        Integer dorsal) {

    Jugador jugador = jugadorRepository.findById(id).orElse(null);

    if (jugador == null) {
        jugador = new Jugador();
        jugador.setId(id);
        jugador.setEquipo(equipo);
        jugador.setNombre(nombre);
        jugador.setPosicion(posicion);
        jugador.setDorsal(dorsal);

        return jugadorRepository.save(jugador);
    }

    // Completar los datos que faltaban
    if ((jugador.getPosicion() == null
            || jugador.getPosicion().isBlank())
            && posicion != null && !posicion.isBlank()) {
        jugador.setPosicion(posicion);
    }

    if (jugador.getDorsal() == null && dorsal != null) {
        jugador.setDorsal(dorsal);
    }

    if ((jugador.getNombre() == null
            || jugador.getNombre().isBlank())
            && nombre != null && !nombre.isBlank()) {
        jugador.setNombre(nombre);
    }

    if (jugador.getEquipo() == null && equipo != null) {
        jugador.setEquipo(equipo);
    }

    return jugadorRepository.save(jugador);
}

    private void guardarAlineacion(Partido partido, Equipo equipo, Entrenador entrenador,
                                    LineupResponse.PlayerSlot slot, String ladoEquipo,
                                    String formacion, boolean titular) {
        Jugador jugador = obtenerOCrearJugador(
                slot.player().id(), slot.player().name(), equipo,
                slot.player().pos(), slot.player().number()
        );

        Alineacion alineacion = new Alineacion();
        alineacion.setPartido(partido);
        alineacion.setJugador(jugador);
        alineacion.setEntrenador(entrenador);
        alineacion.setEquipo(ladoEquipo);
        alineacion.setTitular(titular);
        alineacion.setFormacion(formacion);
        alineacion.setPosicionCampo(slot.player().grid());

        alineacionRepository.save(alineacion);
    }

    // Convierte "Regular Season - 8" en el número 8
    private Integer extraerNumeroJornada(String round) {
        if (round == null) return null;
        Matcher matcher = Pattern.compile("(\\d+)").matcher(round);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }
}