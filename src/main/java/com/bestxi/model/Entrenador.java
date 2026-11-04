package com.bestxi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "entrenador")
@Getter
@Setter
@NoArgsConstructor
public class Entrenador {

    @Id
    private Long id; // Id de API-Football

    @ManyToOne
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Column(nullable = false, length = 150)
    private String nombre;
}
