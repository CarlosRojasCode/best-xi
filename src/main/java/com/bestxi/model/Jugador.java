package com.bestxi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "jugador")
@Getter
@Setter
@NoArgsConstructor
public class Jugador {

    @Id
    private Long id; // Id de API-Football

    @ManyToOne
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 20)
    private String posicion; // G, D, M, F (portero, defensa, medio, delantero)

    private Integer dorsal;
}
