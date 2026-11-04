package com.bestxi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "incidencia")
@Getter
@Setter
@NoArgsConstructor
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @ManyToOne
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;

    @Column(nullable = false, length = 30)
    private String tipo; // "Missing Fixture", "Questionable"...

    @Column(length = 100)
    private String motivo; // "Knee Injury", "Yellow Cards", "National selection"...
}
