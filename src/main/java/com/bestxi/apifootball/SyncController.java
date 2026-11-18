package com.bestxi.apifootball;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(name = "bestxi.admin-activo", havingValue = "true")
@RestController
public class SyncController {

    private final DataSyncService dataSyncService;

    public SyncController(DataSyncService dataSyncService) {
        this.dataSyncService = dataSyncService;
    }

    // POST /admin/sync/partidos?liga=140&temporada=2023&equipoId=543
    @PostMapping("/admin/sync/partidos")
    public String sincronizarPartidos(@RequestParam int liga, @RequestParam int temporada, @RequestParam long equipoId) {
        dataSyncService.importarPartidos(liga, temporada, equipoId);
        return "Partidos importados correctamente";
    }

    // POST /admin/sync/alineaciones?partidoId=1038025
    @PostMapping("/admin/sync/alineaciones")
    public String sincronizarAlineaciones(@RequestParam long partidoId) {
        dataSyncService.importarAlineaciones(partidoId);
        return "Alineaciones importadas correctamente";
    }

    // POST /admin/sync/alineaciones/equipo?equipoId=543
    @PostMapping("/admin/sync/alineaciones/equipo")
    public String sincronizarAlineacionesDeEquipo(@RequestParam long equipoId) {
        dataSyncService.importarAlineacionesDeEquipo(equipoId);
        return "Alineaciones del equipo importadas correctamente (saltando las que ya existían)";
    }

    // POST /admin/sync/incidencias?liga=140&temporada=2023&equipoId=543
    @PostMapping("/admin/sync/incidencias")
    public String sincronizarIncidencias(@RequestParam int liga, @RequestParam int temporada, @RequestParam long equipoId) {
        dataSyncService.importarIncidencias(liga, temporada, equipoId);
        return "Incidencias importadas correctamente";
    }

    // POST /admin/sync/jugadores/completar?partidoId=1038201
    // Vuelve a pedir la alineación de un partido solo para rellenar posición y dorsal de jugadores que los tengan a null
    @PostMapping("/admin/sync/jugadores/completar")
    public String completarDatosJugadores(@RequestParam long partidoId) {
        dataSyncService.completarDatosJugadores(partidoId);
        return "Datos de jugadores completados con la alineación del partido " + partidoId;
    }
}