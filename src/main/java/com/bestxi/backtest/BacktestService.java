package com.bestxi.backtest;

import com.bestxi.model.Alineacion;
import com.bestxi.model.Incidencia;
import com.bestxi.repository.AlineacionRepository;
import com.bestxi.repository.IncidenciaRepository;
import com.bestxi.titularidad.TitularidadResponse;
import com.bestxi.titularidad.TitularidadService;
import com.bestxi.titularidad.UltimoPartidoInfo;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BacktestService {

    private static final int PARTIDOS_A_ANALIZAR = 5;
    private static final int HISTORIAL_MINIMO = 3;

    private final AlineacionRepository alineacionRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final TitularidadService titularidadService;

    public BacktestService(AlineacionRepository alineacionRepository,
                           IncidenciaRepository incidenciaRepository,
                           TitularidadService titularidadService) {
        this.alineacionRepository = alineacionRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.titularidadService = titularidadService;
    }

    public record Tramo(
            String rango,
            int casos,
            int titularesReales,
            int porcentajeRealTitular) {
    }

    public record Resultado(
            int casosEvaluados,
            int aciertos,
            int porcentajeAcierto,
            // Regla simple para comparar: "titular si fue titular en su partido anterior"
            int aciertosReglaSimple,
            int porcentajeAciertoReglaSimple,
            List<Tramo> tramos) {
    }

    public Resultado ejecutar(Long equipoId) {

        List<Alineacion> registros =
                alineacionRepository.findAll().stream()
                        .filter(a -> a.getJugador()
                                .getEquipo()
                                .getId()
                                .equals(equipoId))
                        .toList();

        int[] casosPorTramo = new int[4];
        int[] titularesPorTramo = new int[4];

        int casos = 0;
        int aciertos = 0;
        int aciertosReglaSimple = 0;

        for (Alineacion registro : registros) {

            /*
             * Si no fue titular por lesión, sanción, enfermedad,
             * selección, etc., no evaluamos este caso.
             */

            TitularidadResponse prediccion =
                    titularidadService.calcularTitularidad(
                            registro.getJugador().getId(),
                            PARTIDOS_A_ANALIZAR,
                            registro.getPartido().getId()
                    );

            if (prediccion.partidosAnalizados() < HISTORIAL_MINIMO) {
                continue;
            }

            int porcentaje = prediccion.porcentajeFinal();

            boolean realmenteTitular =
                    registro.getTitular();

            boolean predijoTitular =
                    porcentaje >= 50;

            casos++;

            if (predijoTitular == realmenteTitular) {
                aciertos++;
            }

            if(prediccion.ultimosPartidos().get(0).titular() == realmenteTitular){
                aciertosReglaSimple++;
            }

            int tramo = tramoDe(porcentaje);

            casosPorTramo[tramo]++;

            if (realmenteTitular) {
                titularesPorTramo[tramo]++;
            }
        }

        String[] nombres = {
                "0-29%",
                "30-49%",
                "50-69%",
                "70-100%"
        };

        List<Tramo> tramos = new ArrayList<>();

        for (int i = 0; i < 4; i++) {

            int pct = casosPorTramo[i] == 0
                    ? 0
                    : (int) Math.round(
                    titularesPorTramo[i] * 100.0
                            / casosPorTramo[i]);

            tramos.add(
                    new Tramo(
                            nombres[i],
                            casosPorTramo[i],
                            titularesPorTramo[i],
                            pct
                    )
            );
        }

        int porcentajeAcierto =
                casos == 0
                        ? 0
                        : (int) Math.round(
                        aciertos * 100.0 / casos);

        int porcentajeAciertoReglaSimple =
                casos == 0
                        ? 0
                        : (int) Math.round(
                        aciertosReglaSimple * 100.0 / casos);

        return new Resultado(
                casos,
                aciertos,
                porcentajeAcierto,
                aciertosReglaSimple,
                porcentajeAciertoReglaSimple,
                tramos
        );
    }

    private boolean esAusenciaForzosa(Long jugadorId,
                                      Long partidoId) {

        List<Incidencia> incidencias =
                incidenciaRepository.findByJugadorId(jugadorId);

        return incidencias.stream()
                .filter(i -> i.getPartido()
                        .getId()
                        .equals(partidoId))
                .anyMatch(titularidadService::esAusenciaForzosa);
    }

    private int tramoDe(int porcentaje) {

        if (porcentaje < 30) return 0;

        if (porcentaje < 50) return 1;

        if (porcentaje < 70) return 2;

        return 3;
    }
}