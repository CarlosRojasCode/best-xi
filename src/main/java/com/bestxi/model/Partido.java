package com.bestxi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "partido")
@Getter
@Setter
@NoArgsConstructor
public class Partido {

    @Id
    private Long id; // Id de API-Football (el "fixture.id" que vimos en las respuestas)

    @ManyToOne
    @JoinColumn(name = "equipo_local_id", nullable = false)
    private Equipo equipoLocal;

    @ManyToOne
    @JoinColumn(name = "equipo_visitante_id", nullable = false)
    private Equipo equipoVisitante;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private Integer jornada;
}
