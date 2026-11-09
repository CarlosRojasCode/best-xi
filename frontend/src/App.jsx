import { useState } from 'react'
import './App.css'

function App() {
  const [nombreJugador, setNombreJugador] = useState('')
  const [jugadores, setJugadores] = useState([])
  const [buscando, setBuscando] = useState(false)
  const [mensajeBusqueda, setMensajeBusqueda] = useState('')
  const [esErrorBusqueda, setEsErrorBusqueda] = useState(false)
  const [jornada, setJornada] = useState('')
  const [prediccion, setPrediccion] = useState(null)



  const buscarJugadores = async (evento) => {
    evento.preventDefault()
    setPrediccion(null)

    const nombre = nombreJugador.trim()

    if (!nombre) {
      setJugadores([])
      setMensajeBusqueda('Escribe el nombre de un jugador.')
      setEsErrorBusqueda(true)
      return
    }

    setBuscando(true)
    setJugadores([])
    setMensajeBusqueda('')
    setEsErrorBusqueda(false)

    try {
      const respuesta = await fetch(
        `http://localhost:8080/api/jugadores?nombre=${encodeURIComponent(nombre)}`
      )

      if (!respuesta.ok) {
        throw new Error('Error al consultar los jugadores')
      }

      const datos = await respuesta.json()
      setJugadores(datos)

      if (datos.length === 0) {
        setMensajeBusqueda('No se han encontrado jugadores con ese nombre.')
        setEsErrorBusqueda(true)
      }
    } catch (error) {
      console.error('Error al buscar jugadores:', error)
      setMensajeBusqueda(
        'No se ha podido conectar con Spring Boot. Comprueba que esté iniciado.'
      )
      setEsErrorBusqueda(true)
    } finally {
      setBuscando(false)
    }
  }

  const analizarJugador = async (id) => {
  setPrediccion(null)

  setMensajeBusqueda('')
  setEsErrorBusqueda(false)

  try {
    const urlBase = `http://localhost:8080/jugadores/${id}/titularidad`

    const url = jornada
      ? `${urlBase}?ultimosPartidos=5&jornada=${jornada}`
      : `${urlBase}/proximo-partido?ultimosPartidos=5`

    const respuesta = await fetch(url)


    if (!respuesta.ok) {
      throw new Error('Error al consultar la predicción')
    }

    const datos = await respuesta.json()

    if (datos.partidosAnalizados === 0) {
      setMensajeBusqueda(datos.explicacion)
      setEsErrorBusqueda(true)
      return
    }

    setPrediccion({ ...datos, jornada })
  } catch (error) {
    console.error('Error al analizar jugador:', error)
    setMensajeBusqueda('No se ha podido obtener la predicción. Comprueba que Spring Boot esté iniciado y que la jornada exista.')
    setEsErrorBusqueda(true)
  }
}


  return (
    <div className="app">
      <header className="navbar">
        <a className="brand" href="#inicio">
          <img
            className="header-logo"
            src="/logo-bxi.png"
            alt="BEST XI"
          />
        </a>

        <nav className="nav-links">
          <a href="#inicio">Inicio</a>
          <a href="#como-funciona">Cómo funciona</a>
        </nav>

        <a className="nav-button" href="#analizador">
          Analizar jugador
          <span>↗</span>
        </a>
      </header>

      <main>
        <section className="hero" id="inicio">
          <div className="hero-content">
            <div className="eyebrow">
              <span className="status-dot"></span>
              ANÁLISIS DE TITULARIDAD
            </div>

            <h1>
              Anticípate al
              <br />
              <span>once inicial.</span>
            </h1>

            <p className="hero-description">
              Descubre qué posibilidades tiene un futbolista
              de ser titular en cada jornada. Analizamos su
              historial, su situación deportiva y la confianza
              del entrenador.
            </p>

            <div className="hero-actions">
              <a className="primary-button" href="#analizador">
                Descubrir BEST XI
                <span>→</span>
              </a>

              <a className="secondary-button" href="#como-funciona">
                Cómo funciona
              </a>
            </div>

            <div className="hero-note">
              <span className="note-line"></span>
              Datos, contexto y análisis futbolístico.
            </div>
          </div>

          <div className="preview-card">
            <div className="preview-header">
              <div>
                <span className="preview-label">BEST XI ANALYTICS</span>
                <h2>Predicción de titularidad</h2>
              </div>

              <span className="preview-icon">↗</span>
            </div>

            <div className="preview-player">
              <div className="player-placeholder">XI</div>

              <div>
                <span className="player-team">EJEMPLO DE ANÁLISIS</span>
                <h3>Tu próximo análisis</h3>
                <p>Jugador · Jornada</p>
              </div>
            </div>

            <div className="preview-probability">
              <div className="probability-heading">
                <span>PROBABILIDAD DE TITULAR</span>
                <span className="example-tag">DEMO</span>
              </div>

              <div className="probability-value">
                <strong>78<span>%</span></strong>
                <span className="probability-status">
                  Ejemplo visual
                </span>
              </div>

              <div className="progress-track">
                <div className="progress-fill"></div>
              </div>

              <p className="preview-footnote">
                Así mostraremos los resultados del análisis.
              </p>
            </div>

            <div className="preview-footer">
              <span className="footer-dot"></span>
              Historial · Forma reciente · Contexto
            </div>
          </div>

          <div className="hero-decoration decoration-one"></div>
          <div className="hero-decoration decoration-two"></div>
        </section>

        <section className="intro-section" id="como-funciona">
          <div className="section-heading">
            <span className="section-kicker">QUÉ HACEMOS</span>

            <h2>
              Entiende el contexto.
              <br />
              <span>Anticipa el once.</span>
            </h2>
          </div>

          <p className="section-description">
            Una herramienta para analizar la probabilidad de
            titularidad de los futbolistas, sin perder de vista
            lo que sucede dentro y fuera del terreno de juego.
          </p>

          <div className="feature-grid">
            <article className="feature-card">
              <span className="feature-number">01</span>
              <div className="feature-icon">↗</div>
              <h3>Historial del jugador</h3>
              <p>
                Consulta sus titularidades y suplencias
                en las jornadas anteriores.
              </p>
            </article>

            <article className="feature-card">
              <span className="feature-number">02</span>
              <div className="feature-icon">⌁</div>
              <h3>Forma reciente</h3>
              <p>
                Comprueba su evolución y el protagonismo
                que tiene en los últimos partidos.
              </p>
            </article>

            <article className="feature-card">
              <span className="feature-number">03</span>
              <div className="feature-icon">◎</div>
              <h3>Contexto deportivo</h3>
              <p>
                Ten en cuenta al entrenador, los partidos
                en casa y fuera y las ausencias conocidas.
              </p>
            </article>
          </div>
        </section>

        <section className="start-section" id="analizador">
          <div className="analyzer-copy">
            <span className="section-kicker">BUSCADOR DE JUGADORES</span>
            <h2>¿A quién quieres analizar?</h2>
            <p>
              Busca un futbolista y consulta los datos disponibles
              en BEST XI.
            </p>
          </div>

          <div className="player-search-panel">
            <form className="search-form" onSubmit={buscarJugadores}>
              <label htmlFor="nombreJugador">Nombre del futbolista</label>

              <div className="search-controls">
                <input
                  id="nombreJugador"
                  type="text"
                  value={nombreJugador}
                  onChange={(evento) => setNombreJugador(evento.target.value)}
                  placeholder="Ej. N. Fekir"
                  autoComplete="off"
                />

                <button type="submit" disabled={buscando}>
                  {buscando ? 'Buscando...' : 'Buscar jugador'}
                  <span>→</span>
                </button>
              </div>
                            <div className="jornada-field">
                <label htmlFor="jornada">Jornada (opcional)</label>

                <input
                  id="jornada"
                  type="number"
                  min="1"
                  max="38"
                  value={jornada}
                  onChange={(evento) => setJornada(evento.target.value)}
                  placeholder="Próximo partido"
                />
              </div>

            </form>

            {mensajeBusqueda && (
              <p className={`search-message ${esErrorBusqueda ? 'search-error' : ''}`}>
                {mensajeBusqueda}
              </p>
            )}

            {jugadores.length > 0 && (
              <div className="search-results">
                <h3>Jugadores encontrados ({jugadores.length})</h3>

                <div className="search-results-list">
                  {jugadores.map((jugador) => (
                    <article className="search-result" key={jugador.id}>
                      <div className="search-result-main">
                        <h4>{jugador.nombre}</h4>
                        <p>{jugador.equipoNombre || 'Equipo no disponible'}</p>
                      </div>

                      <div className="search-result-actions">
                        <span className="search-result-position">
                          {{
                            G: 'Portero',
                            D: 'Defensa',
                            M: 'Centrocampista',
                            F: 'Delantero',
                          }[jugador.posicion] || 'Posición no disponible'}
                        </span>

                        <button
                          className="analyze-button"
                          type="button"
                          onClick={() => analizarJugador(jugador.id)}
                        >
                          Analizar titularidad
                        </button>
                      </div>
                    </article>
                  ))}
                </div>
              </div>
            )}
                        {prediccion && (
              <article className="prediction-card">
                <div className="probability-heading">
                  <span>PROBABILIDAD DE TITULAR</span>
                  <span className="example-tag">
                    {prediccion.jornada ? `JORNADA ${prediccion.jornada}` : 'PRÓXIMO PARTIDO'}
                  </span>

                </div>

                <h3 className="prediction-player">{prediccion.nombreJugador}</h3>
                <p className="prediction-explanation">
                  J{prediccion.partido.jornada} · {prediccion.partido.equipoLocal} - {prediccion.partido.equipoVisitante} ·{' '}
                  {new Date(prediccion.partido.fecha).toLocaleDateString('es-ES', {
                    day: 'numeric',
                    month: 'long',
                    year: 'numeric',
                  })}
                </p>


                <div className="probability-value">
                  <strong>{prediccion.porcentajeFinal}<span>%</span></strong>
                </div>

                <div className="progress-track">
                  <div
                    className="progress-fill"
                    style={{ width: `${prediccion.porcentajeFinal}%` }}
                  ></div>
                </div>

                <dl className="prediction-stats">
                  <div>
                    <dt>Partidos analizados</dt>
                    <dd>{prediccion.partidosAnalizados}</dd>
                  </div>
                  <div>
                    <dt>Titularidades recientes</dt>
                    <dd>{prediccion.titularesRecientes}</dd>
                  </div>
                  <div>
                    <dt>Frecuencia últimos 3</dt>
                    <dd>{prediccion.frecuenciaUltimos3}%</dd>
                  </div>
                  <div>
                    <dt>Con el entrenador (partidos: {prediccion.partidosConEntrenadorActual})</dt>
                    <dd>{prediccion.frecuenciaConEntrenadorActual}%</dd>
                  </div>
                  <div>
                    <dt>En casa (partidos: {prediccion.partidosCasa})</dt>
                    <dd>{prediccion.partidosCasa > 0 ? `${prediccion.frecuenciaCasa}%` : 'Sin datos'}</dd>
                  </div>
                  <div>
                    <dt>Fuera (partidos: {prediccion.partidosFuera})</dt>
                    <dd>{prediccion.partidosFuera > 0 ? `${prediccion.frecuenciaFuera}%` : 'Sin datos'}</dd>
                  </div>

                </dl>
                <div className="prediction-recent">
                  {prediccion.ultimosPartidos.map((ultimo) => (
                    <span
                      className={`example-tag ${ultimo.titular ? '' : 'suplente'}`}
                      key={ultimo.jornada}
                    >
                      J{ultimo.jornada} · {ultimo.titular ? 'Titular' : 'Suplente'}
                    </span>
                  ))}
                </div>
                 {prediccion.ausencias.length > 0 && (
                <div className="prediction-recent">
                  {prediccion.ausencias.map((ausencia) => (
                    <span className="example-tag suplente" key={ausencia.motivo}>
                      {ausencia.partidos} {ausencia.partidos === 1 ? 'partido' : 'partidos'} · {ausencia.motivo}
                    </span>
                  ))}
                </div>
              )}
 
                <p className="prediction-explanation">{prediccion.explicacion}</p>
              </article>
            )}
          </div>
        </section>
      </main>

      <footer className="footer">
        <a className="brand footer-brand" href="#inicio">
          <img
            className="footer-logo"
            src="/logo-bxi.png"
            alt="BEST XI"
          />
        </a>

        <p>
          Análisis de titularidad. El fútbol, con más contexto.
        </p>

        <span className="footer-year">BEST XI</span>
      </footer>
    </div>
  )
}

export default App