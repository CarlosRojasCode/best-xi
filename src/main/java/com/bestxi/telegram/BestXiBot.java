package com.bestxi.telegram;

import com.bestxi.model.Jugador;
import com.bestxi.model.Partido;
import com.bestxi.repository.AlineacionRepository;
import com.bestxi.repository.JugadorRepository;
import com.bestxi.repository.PartidoRepository;
import com.bestxi.titularidad.TitularidadResponse;
import com.bestxi.titularidad.TitularidadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class BestXiBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final int PARTIDOS_A_ANALIZAR = 5;
    private static final int JORNADA_MINIMA = 2;
    private static final int JORNADA_MAXIMA = 38;

    private final String token;
    private final TelegramClient telegramClient;
    private final JugadorRepository jugadorRepository;
    private final TitularidadService titularidadService;
    private final PartidoRepository partidoRepository;
    private final AlineacionRepository alineacionRepository;

    public BestXiBot(@Value("${telegram.token}") String token,
                     JugadorRepository jugadorRepository,
                     TitularidadService titularidadService,
                     PartidoRepository partidoRepository,
                     AlineacionRepository alineacionRepository) {

        this.token = token;
        this.telegramClient = new OkHttpTelegramClient(token);
        this.jugadorRepository = jugadorRepository;
        this.titularidadService = titularidadService;
        this.partidoRepository = partidoRepository;
        this.alineacionRepository = alineacionRepository;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    // Se ejecuta cada vez que alguien le escribe al bot
    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String texto = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();

        String respuesta;
        try {
            respuesta = construirRespuesta(texto);
        } catch (Exception e) {
            e.printStackTrace();
            respuesta = "Ha ocurrido un error. Inténtalo de nuevo en un rato.";
        }

        enviar(chatId, respuesta);
    }

    private String construirRespuesta(String texto) {

        if (texto.equalsIgnoreCase("/start")) {
            return "Hola, soy BEST XI.\n\n"
                    + "Escribe el nombre del jugador seguido de la jornada.\n"
                    + "Ejemplo: Abner J10";
        }

        Pattern patron = Pattern.compile("^(.*?)\\s+[jJ](\\d+)$");
        Matcher matcher = patron.matcher(texto);

        if (!matcher.matches()) {
            return "Escribe el jugador seguido de la jornada.\n"
                    + "Ejemplo: Abner J10";
        }

        String nombreJugador = matcher.group(1).trim();
        int jornada = Integer.parseInt(matcher.group(2));

        List<Jugador> encontrados =
                jugadorRepository.findByNombreContainingIgnoreCase(nombreJugador);

        if (encontrados.isEmpty()) {
            return "No encuentro ningún jugador llamado " + nombreJugador + ".";
        }

        Jugador elegido = null;

        if (encontrados.size() == 1) {
            elegido = encontrados.get(0);
        } else {
            for (Jugador j : encontrados) {
                if (j.getNombre().equalsIgnoreCase(nombreJugador)) {
                    elegido = j;
                    break;
                }
            }
        }

        if (elegido == null) {
            String lista = encontrados.stream()
                    .limit(5)
                    .map(j -> "- " + j.getNombre() + " (" + j.getEquipo().getNombre() + ")")
                    .collect(Collectors.joining("\n"));

            return "Hay varios jugadores que coinciden, sé más concreto:\n" + lista;
        }

        // Si no hay base real para esa jornada, no calculamos nada
        if (!hayDatosParaJornada(elegido, jornada)) {
            return "No tengo datos para esa jornada.";
        }

        Long equipoId = elegido.getEquipo().getId();

        List<Partido> partidos = partidoRepository
                .findByJornadaAndEquipoLocal_IdOrJornadaAndEquipoVisitante_Id(
                        jornada,
                        equipoId,
                        jornada,
                        equipoId
                );

        if (partidos.isEmpty()) {
            return "No tengo datos para esa jornada.";
        }

        Partido partidoObjetivo = partidos.get(0);

        TitularidadResponse resultado = titularidadService.calcularTitularidad(
                elegido.getId(),
                PARTIDOS_A_ANALIZAR,
                partidoObjetivo.getId()
        );

        return "⚽ " + resultado.nombreJugador() + " (" + resultado.posicion() + ")\n"
                + "📅 Jornada: J" + jornada + "\n"
                + "Probabilidad de titular: " + resultado.porcentajeFinal() + "%\n\n"
                + resultado.explicacion();
    }

    // Solo hay base real si la jornada es válida y tenemos la alineación de la jornada anterior
    private boolean hayDatosParaJornada(Jugador jugador, int jornadaPedida) {
        if (jornadaPedida < JORNADA_MINIMA || jornadaPedida > JORNADA_MAXIMA) {
            return false;
        }

        Long equipoId = jugador.getEquipo().getId();
        int jornadaAnterior = jornadaPedida - 1;

        return partidoRepository
                .findByJornadaAndEquipoLocal_IdOrJornadaAndEquipoVisitante_Id(
                        jornadaAnterior, equipoId, jornadaAnterior, equipoId)
                .stream()
                .anyMatch(p -> alineacionRepository.existsByPartidoId(p.getId()));
    }

    private void enviar(long chatId, String texto) {
        SendMessage mensaje = SendMessage.builder()
                .chatId(chatId)
                .text(texto)
                .build();
        try {
            telegramClient.execute(mensaje);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}