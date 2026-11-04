package com.bestxi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alineacion")
@Getter
@Setter
@NoArgsConstructor
public class Alineacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // este id lo genera la base de datos (BIGSERIAL)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;

    @ManyToOne
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @ManyToOne
    @JoinColumn(name = "entrenador_id", nullable = false)
    private Entrenador entrenador;

    @Column(nullable = false, length = 10)
    private String equipo; // "local" o "visitante", para saber en qué lado jugó

    @Column(nullable = false)
    private Boolean titular;

    private String formacion; // ej. "4-2-3-1"

    @Column(name = "posicion_campo", length = 10)
    private String posicionCampo; // el "grid" que vimos en la API, ej. "4:2"
}
