-- Equipos de LaLiga
CREATE TABLE equipo (
    id BIGINT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    escudo_url VARCHAR(255)
);

-- Jugadores, cada uno pertenece a un equipo
CREATE TABLE jugador (
    id BIGINT PRIMARY KEY,
    equipo_id BIGINT NOT NULL REFERENCES equipo(id),
    nombre VARCHAR(150) NOT NULL,
    posicion VARCHAR(20),
    dorsal INTEGER
);

-- Entrenadores, cada uno asociado a un equipo
CREATE TABLE entrenador (
    id BIGINT PRIMARY KEY,
    equipo_id BIGINT NOT NULL REFERENCES equipo(id),
    nombre VARCHAR(150) NOT NULL
);

-- Partidos de LaLiga
CREATE TABLE partido (
    id BIGINT PRIMARY KEY,
    equipo_local_id BIGINT NOT NULL REFERENCES equipo(id),
    equipo_visitante_id BIGINT NOT NULL REFERENCES equipo(id),
    fecha TIMESTAMP NOT NULL,
    jornada INTEGER
);

-- Quién jugó, con quién de entrenador, titular o suplente, en qué partido
CREATE TABLE alineacion (
    id BIGSERIAL PRIMARY KEY,
    partido_id BIGINT NOT NULL REFERENCES partido(id),
    jugador_id BIGINT NOT NULL REFERENCES jugador(id),
    entrenador_id BIGINT NOT NULL REFERENCES entrenador(id),
    equipo VARCHAR(10) NOT NULL,
    titular BOOLEAN NOT NULL,
    formacion VARCHAR(20),
    posicion_campo VARCHAR(10)
);

-- Lesiones, sanciones, convocatorias de selección, etc.
CREATE TABLE incidencia (
    id BIGSERIAL PRIMARY KEY,
    jugador_id BIGINT NOT NULL REFERENCES jugador(id),
    partido_id BIGINT NOT NULL REFERENCES partido(id),
    tipo VARCHAR(30) NOT NULL,
    motivo VARCHAR(100)
);

-- Índices para las consultas más habituales (buscar por jugador o por partido)
CREATE INDEX idx_alineacion_jugador ON alineacion(jugador_id);
CREATE INDEX idx_alineacion_partido ON alineacion(partido_id);
CREATE INDEX idx_incidencia_jugador ON incidencia(jugador_id);
