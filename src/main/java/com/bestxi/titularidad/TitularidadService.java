package com.bestxi.titularidad;

import com.bestxi.model.Alineacion;
import com.bestxi.model.Incidencia;
import com.bestxi.model.Jugador;
import com.bestxi.model.Partido;
import com.bestxi.repository.AlineacionRepository;
import com.bestxi.repository.EquipoRepository;
import com.bestxi.repository.IncidenciaRepository;
import com.bestxi.repository.JugadorRepository;
import com.bestxi.repository.PartidoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.stream.Collectors;

import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class TitularidadService {

    private final EquipoRepository equipoRepository;
    private final JugadorRepository jugadorRepository;
    private final AlineacionRepository alineacionRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final PartidoRepository partidoRepository;

    // Lee bestxi.fecha-simulada de application.yml. Si no existe, queda vacía.
    @Value("${bestxi.fecha-simulada:}")
    private String fechaSimulada;

    public TitularidadService(JugadorRepository jugadorRepository,
                              AlineacionRepository alineacionRepository,
                              IncidenciaRepository incidenciaRepository,
                              PartidoRepository partidoRepository, EquipoRepository equipoRepository) {
        this.jugadorRepository = jugadorRepository;
        this.alineacionRepository = alineacionRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.partidoRepository = partidoRepository;
        this.equipoRepository = equipoRepository;
    }

    public TitularidadResponse calcularTitularidadParaJornada(Long jugadorId,
        int ultimosNPartidos, int jornada){
                
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el jugador " + jugadorId));

        Long equipoId = jugador.getEquipo().getId();

        List<Partido> partidos = partidoRepository
        .findByJornadaAndEquipoLocal_IdOrJornadaAndEquipoVisitante_Id(jornada, equipoId, jornada, equipoId);

        if(partidos.isEmpty()){
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No hay datos de la jornada " + jornada);
        }

        return calcularTitularidad(jugadorId, ultimosNPartidos, partidos.get(0).getId());
        }

    public TitularidadResponse calcularTitularidadProximoPartido(Long jugadorId,
        int ultimosNPartidos){

        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el jugador " + jugadorId));

        Long equipoId = jugador.getEquipo().getId();

        LocalDateTime ahora = obtenerAhora();

        Partido proximoPartido = partidoRepository
                .findFirstByEquipoLocal_IdAndFechaAfterOrEquipoVisitante_IdAndFechaAfterOrderByFechaAsc(
                        equipoId, ahora, equipoId, ahora)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No hay próximos partidos para este equipo"));

        return calcularTitularidad(jugadorId, ultimosNPartidos, proximoPartido.getId());
        }

    // Devuelve la fecha simulada si está configurada y, si no, la fecha real.
    private LocalDateTime obtenerAhora() {
        if (fechaSimulada.isBlank()) {
            return LocalDateTime.now();
        }

        return LocalDate.parse(fechaSimulada).atStartOfDay();
    }

    public TitularidadResponse calcularTitularidad(Long jugadorId,
                                                   int ultimosNPartidos,
                                                   Long excluirPartidoId) {

        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el jugador " + jugadorId));

        List<Alineacion> historialCompleto =
                alineacionRepository.findByJugadorIdOrderByPartidoFechaDesc(jugadorId);

        Partido partidoObjetivo = null;

        if (excluirPartidoId != null) {
                partidoObjetivo = partidoRepository.findById(excluirPartidoId)
                        .orElseThrow(() -> new IllegalArgumentException(
                    "No existe el partido " + excluirPartidoId));
        }

        final Partido partidoObjetivoFinal = partidoObjetivo;

        if (partidoObjetivoFinal != null) {
                historialCompleto = historialCompleto.stream()
                        .filter(a -> a.getPartido().getFecha()
                                .isBefore(partidoObjetivoFinal.getFecha()))
                        .toList();
}

        // ---------------------------------------------------------
        // Incidencias de fuerza mayor
        // ---------------------------------------------------------

        List<Incidencia> incidenciasJugador =
        incidenciaRepository.findByJugadorId(jugadorId);

        // Partidos anteriores en los que sí tenemos una alineación
        Set<Long> partidosConAlineacion = historialCompleto.stream()
                .map(a -> a.getPartido().getId())
                .collect(Collectors.toSet());

        // Incidencias de fuerza mayor en partidos en los que
        // el jugador no aparece en la alineación
        List<Incidencia> incidenciasForzosas = incidenciasJugador.stream()
                .filter(this::esAusenciaForzosa)
                .filter(i -> partidoObjetivoFinal == null
                        || i.getPartido().getFecha()
                        .isBefore(partidoObjetivoFinal.getFecha()))
                .filter(i -> !partidosConAlineacion.contains(
                        i.getPartido().getId()))
                .sorted(Comparator.comparing(
                        i -> i.getPartido().getFecha()))
                .toList();
        Set<Long> partidosForzosos = incidenciasForzosas.stream()
                .map(i -> i.getPartido().getId())
                .collect(Collectors.toSet());

        /*
         * Un partido con lesión/sanción NO debe penalizar la confianza
         * del entrenador.
         *
         * Pero si el jugador sí aparece como TITULAR en ese partido,
         * significa que realmente jugó y por tanto sí lo contamos.
         */
        historialCompleto = historialCompleto.stream()
                .filter(a ->
                        a.getTitular()
                                || !partidosForzosos.contains(a.getPartido().getId()))
                .toList();

        if (historialCompleto.isEmpty()) {
            return new TitularidadResponse(
                    jugador.getId(),
                    jugador.getNombre(),
                    jugador.getPosicion(),
                    jugador.getDorsal(),
                    jugador.getEquipo().getNombre(),
                    jugador.getEquipo().getEscudoUrl(),
                    0, 0, 0,
                    null, null,
                    null, null, null, null,
                    null,
                    0,
                    "No hay historial de partidos disponible para este jugador todavía.",
                    crearPartidoInfo(partidoObjetivoFinal),
                    List.of(),
                    List.of()
            );
        }

        // ---------------------------------------------------------
        // Señal 1: últimos N partidos DISPONIBLES
        // ---------------------------------------------------------

        List<Alineacion> ultimos =
                historialCompleto.stream()
                        .limit(ultimosNPartidos)
                        .toList();

        long titularesRecientes =
                ultimos.stream()
                        .filter(Alineacion::getTitular)
                        .count();

        int frecuenciaReciente =
                porcentaje(titularesRecientes, ultimos.size());

        // ---------------------------------------------------------
        // Señal 2: historial DISPONIBLE con entrenador actual
        // ---------------------------------------------------------

        Long entrenadorActualId =
                ultimos.get(0).getEntrenador().getId();

        List<Alineacion> conEntrenadorActual =
                historialCompleto.stream()
                        .filter(a -> a.getEntrenador().getId()
                                .equals(entrenadorActualId))
                        .toList();

        long titularesConEntrenador =
                conEntrenadorActual.stream()
                        .filter(Alineacion::getTitular)
                        .count();

        int frecuenciaConEntrenador =
                porcentaje(titularesConEntrenador,
                        conEntrenadorActual.size());

        // ---------------------------------------------------------
        // Señal 3: toda la temporada / historial disponible
        // ---------------------------------------------------------

        long titularesTemporada =
                historialCompleto.stream()
                        .filter(Alineacion::getTitular)
                        .count();

        int frecuenciaTemporada =
                porcentaje(titularesTemporada,
                        historialCompleto.size());

        // ---------------------------------------------------------
        // Señal 4: casa vs fuera
        // ---------------------------------------------------------

        List<Alineacion> partidosCasa =
                historialCompleto.stream()
                        .filter(a -> "local".equals(a.getEquipo()))
                        .toList();

        List<Alineacion> partidosFuera =
                historialCompleto.stream()
                        .filter(a -> "visitante".equals(a.getEquipo()))
                        .toList();

        long titularesCasa =
                partidosCasa.stream()
                        .filter(Alineacion::getTitular)
                        .count();

        long titularesFuera =
                partidosFuera.stream()
                        .filter(Alineacion::getTitular)
                        .count();

        // ---------------------------------------------------------
        // Señal 5: últimos 3 partidos DISPONIBLES
        // ---------------------------------------------------------

        List<Alineacion> ultimosTres =
                historialCompleto.stream()
                        .limit(3)
                        .toList();

        long titularesUltimosTres =
                ultimosTres.stream()
                        .filter(Alineacion::getTitular)
                        .count();

        int frecuenciaUltimosTres =
                porcentaje(titularesUltimosTres,
                        ultimosTres.size());

        // ---------------------------------------------------------
        // Cálculo final
        // ---------------------------------------------------------

        int porcentajeFinal;

        /*
         * Damos más importancia a lo reciente,
         * pero sin olvidar toda la temporada.
         *
         * 50% -> últimos 5 disponibles
         * 30% -> entrenador actual
         * 20% -> historial completo disponible
         */

        if (conEntrenadorActual.size() >= 3) {

                porcentajeFinal = (int) Math.round(
                        frecuenciaUltimosTres * 0.40
                    + frecuenciaReciente * 0.30
                    + frecuenciaConEntrenador * 0.20
                    + frecuenciaTemporada * 0.10
        );

        } else {

                porcentajeFinal = (int) Math.round(
                        frecuenciaUltimosTres * 0.45
                                + frecuenciaReciente * 0.35
                                + frecuenciaTemporada * 0.20
                );
        }

        // ---------------------------------------------------------
        // Explicación
        // ---------------------------------------------------------

        String detalleAusencias =
                construirDetalleAusencias(incidenciasForzosas);

        String explicacion = construirExplicacion(
                partidoObjetivoFinal,
                porcentajeFinal,
                frecuenciaReciente,
                ultimos.size(),
                (int) titularesRecientes,
                conEntrenadorActual.size(),
                (int) titularesConEntrenador,
                detalleAusencias
        );

        return new TitularidadResponse(
                jugador.getId(),
                jugador.getNombre(),
                jugador.getPosicion(),

                jugador.getDorsal(),
                jugador.getEquipo().getNombre(),
                jugador.getEquipo().getEscudoUrl(),
                
                ultimos.size(),
                (int) titularesRecientes,
                frecuenciaReciente,

                conEntrenadorActual.size(),
                frecuenciaConEntrenador,

                partidosCasa.size(),
                porcentaje(titularesCasa, partidosCasa.size()),

                partidosFuera.size(),
                porcentaje(titularesFuera, partidosFuera.size()),

                frecuenciaUltimosTres,
                porcentajeFinal,
                explicacion,
                crearPartidoInfo(partidoObjetivoFinal),
                crearUltimosPartidos(ultimos),
                crearAusencias(incidenciasForzosas)
        );
    }

    private List<AusenciaInfo> crearAusencias(List<Incidencia> incidencias) {
        Map<String, Integer> cantidadPorMotivo = contarAusenciasPorMotivo(incidencias);
        return cantidadPorMotivo.entrySet().stream()
                .map(e -> new AusenciaInfo(e.getKey(), e.getValue()))
                .toList();

    }

    private List<UltimoPartidoInfo> crearUltimosPartidos(List<Alineacion> alineaciones) {
        return alineaciones.stream()
        .map(a -> new UltimoPartidoInfo(a.getPartido().getJornada(), a.getTitular()))
        .toList();

    }

    private PartidoInfo crearPartidoInfo(Partido partido) {
        if(partido == null){
                return null;
        }else{
               return new PartidoInfo(partido.getJornada(), partido.getEquipoLocal().getNombre(), partido.getEquipoVisitante().getNombre(), partido.getFecha());
        }
    }

    private String construirExplicacion(
        Partido partido,
        int porcentajeFinal,
        int frecuenciaReciente,
        int totalRecientes,
        int titularesRecientes,
        int partidosEntrenador,
        int titularesEntrenador,
        String detalleAusencias) {

    StringBuilder texto = new StringBuilder();

    // Qué partido se predice
    if (partido != null) {
        texto.append("Para el ")
                .append(partido.getEquipoLocal().getNombre())
                .append(" - ")
                .append(partido.getEquipoVisitante().getNombre())
                .append(" (J")
                .append(partido.getJornada())
                .append(") tiene un ");
    } else {
        texto.append("Tiene un ");
    }

    // Motivo principal del porcentaje
    texto.append(porcentajeFinal)
            .append(" % ")
            .append(describirMotivoPrincipal(frecuenciaReciente))
            .append(": titular en ")
            .append(titularesRecientes)
            .append(" de ")
            .append(totalRecientes)
            .append(" partidos recientes. ");

    // Confianza con el entrenador actual
    if (partidosEntrenador >= 3) {
        texto.append("Con el entrenador actual ha sido titular en ")
                .append(titularesEntrenador)
                .append(" de ")
                .append(partidosEntrenador)
                .append(" partidos. ");
    }

    // Ausencias justificadas
    if (detalleAusencias != null && !detalleAusencias.isBlank()) {
        texto.append(detalleAusencias);
    }

    return texto.toString().trim();
}

    // Devuelve el trozo de frase que explica el porcentaje.
    private String describirMotivoPrincipal(int frecuenciaReciente) {
        if(frecuenciaReciente >= 80){
                return "gracias a su buena racha";
        }else if(frecuenciaReciente >= 40){
                return "porque últimamente alterna titularidades y suplencias";
        }else{
                return "porque últimamente no suele ser titular";
        }
    }


    private String construirDetalleAusencias(List<Incidencia> incidencias) {

    if (incidencias.isEmpty()) {
        return "";
    }

    Map<String, Integer> cantidadPorMotivo = contarAusenciasPorMotivo(incidencias);

    int totalPartidos = cantidadPorMotivo.values().stream()
            .mapToInt(Integer::intValue)
            .sum();

    String resumen = cantidadPorMotivo.entrySet().stream()
            .map(e -> e.getValue() + " por " + e.getKey())
            .collect(Collectors.joining(", "));

    return "No cuentan en su contra "
            + totalPartidos
            + (totalPartidos == 1 ? " partido que se perdió: " : " partidos que se perdió: ")
            + resumen
            + ".";
}

    // Agrupa las ausencias por motivo: motivo -> número de partidos.
    private Map<String, Integer> contarAusenciasPorMotivo(List<Incidencia> incidencias) {

    // Evitamos contar dos veces el mismo partido
    Map<Long, String> motivoPorPartido = new LinkedHashMap<>();

    for (Incidencia incidencia : incidencias) {
        motivoPorPartido.putIfAbsent(
                incidencia.getPartido().getId(),
                traducirMotivo(incidencia)
        );
    }

    // Agrupamos las ausencias por motivo
    Map<String, Integer> cantidadPorMotivo = new LinkedHashMap<>();

    for (String motivo : motivoPorPartido.values()) {
        cantidadPorMotivo.merge(motivo, 1, Integer::sum);
    }

    return cantidadPorMotivo;
}

    /**
     * Determina si una incidencia representa una ausencia
     * que no debería penalizar la confianza del entrenador.
     */
    public boolean esAusenciaForzosa(Incidencia incidencia) {

        String tipo = normalizar(incidencia.getTipo());
        String motivo = normalizar(incidencia.getMotivo());

        String texto = tipo + " " + motivo;

        return texto.contains("injury")
                || texto.contains("illness")
                || texto.contains("surgery")
                || texto.contains("yellow cards")
                || texto.contains("red card")
                || texto.contains("suspension")
                || texto.contains("national selection")
                || texto.contains("broken")
                || texto.contains("fracture");
    }

    private String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto.toLowerCase(Locale.ROOT).trim();
    }

    private int porcentaje(long parte, int total) {

        if (total == 0) {
            return 0;
        }

        return (int) Math.round(parte * 100.0 / total);
        }

        private String traducirMotivo(Incidencia incidencia) {

        String motivo = incidencia.getMotivo();

        if (motivo == null || motivo.isBlank()) {
                motivo = incidencia.getTipo();
        }

        return switch (normalizar(motivo)) {
                case "knee injury" -> "lesión de rodilla";
                case "hip injury" -> "lesión de cadera";
                case "muscle injury" -> "lesión muscular";
                case "achilles tendon injury" -> "lesión del tendón de Aquiles";
                case "ankle injury" -> "lesión de tobillo";
                case "calf injury" -> "lesión de gemelo";
                case "thigh injury" -> "lesión de muslo";
                case "toe injury" -> "lesión en el dedo del pie";
                case "groin injury" -> "lesión inguinal";
                case "broken nose" -> "fractura de nariz";
                case "yellow cards" -> "acumulación de tarjetas amarillas";
                case "red card" -> "sanción por tarjeta roja";
                case "national selection" -> "convocatoria con la selección";
                case "illness" -> "enfermedad";
                case "surgery" -> "cirugía";
                case "injury" -> "lesión";
                default -> motivo;
        };
        }

}