package conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatosPrueba {
    public static void cargar(Connection con) throws SQLException
    {
        insertar(con, "INSERT OR IGNORE INTO usuario (id_usuario, nombre_usuario, correo, hash_contrasena, estado) VALUES (?, ?, ?, ?, ?)", new Object[][] {
                {1, "Piero", "piero@ejemplo.com", "HASH_DE_PRUEBA_1", "ACTIVO"},
                {2, "Andrea", "andrea@ejemplo.com", "HASH_DE_PRUEBA_2", "ACTIVO"},
                {3, "Carlos", "carlos@ejemplo.com", "HASH_DE_PRUEBA_3", "ACTIVO"},
                {4, "Lucia", "lucia@ejemplo.com", "HASH_DE_PRUEBA_4", "ACTIVO"},
                {5, "Diego", "diego@ejemplo.com", "HASH_DE_PRUEBA_5", "ACTIVO"},
                {6, "Camila", "camila@ejemplo.com", "HASH_DE_PRUEBA_6", "ACTIVO"},
                {7, "Mateo", "mateo@ejemplo.com", "HASH_DE_PRUEBA_7", "ACTIVO"},
                {8, "Valeria", "valeria@ejemplo.com", "HASH_DE_PRUEBA_8", "ACTIVO"},
                {9, "Sofia", "sofia@ejemplo.com", "HASH_DE_PRUEBA_9", "ACTIVO"},
                {10, "Bruno", "bruno@ejemplo.com", "HASH_DE_PRUEBA_10", "ACTIVO"}
        });

        insertar(con, "INSERT OR IGNORE INTO perfil_jugador (id_usuario, apodo, avatar_url, pais) VALUES (?, ?, ?, ?)", new Object[][] {
                {1, "PieroX", "avatars/1.png", "Peru"}, {2, "Andromeda", "avatars/2.png", "Peru"},
                {3, "CarlosGG", "avatars/3.png", "Chile"}, {4, "Luci4", "avatars/4.png", "Peru"},
                {5, "D_Rex", "avatars/5.png", "Mexico"}, {6, "Camix", "avatars/6.png", "Peru"},
                {7, "Mateo7", "avatars/7.png", "Argentina"}, {8, "Valkyrie", "avatars/8.png", "Peru"},
                {9, "SofiStar", "avatars/9.png", "Chile"}, {10, "BRun", "avatars/10.png", "Colombia"}
        });

        insertar(con, "INSERT OR IGNORE INTO juego (id_juego, titulo, genero, version_actual, max_jugadores, estado) VALUES (?, ?, ?, ?, ?, ?)", new Object[][] {
                {1, "Arena Nova", "Accion", "1.2", 10, "ACTIVO"},
                {2, "Reino Pixel", "RPG", "2.0", 8, "ACTIVO"},
                {3, "Velocidad X", "Carreras", "1.5", 12, "ACTIVO"},
                {4, "Titan Clash", "Estrategia", "3.1", 6, "ACTIVO"},
                {5, "Galaxia Sur", "Aventura", "1.7", 4, "ACTIVO"},
                {6, "Guardianes", "Accion", "1.1", 10, "ACTIVO"},
                {7, "Cubo Magico", "Puzzle", "2.2", 4, "ACTIVO"},
                {8, "Mundo Mar", "Supervivencia", "1.3", 6, "ACTIVO"},
                {9, "Zona Roja", "Accion", "1.0", 12, "ACTIVO"},
                {10, "Fortaleza", "Estrategia", "2.5", 8, "ACTIVO"}
        });

        insertar(con, "INSERT OR IGNORE INTO partida (id_partida, id_juego, modo, region, inicio, fin, estado) VALUES (?, ?, ?, ?, ?, ?, ?)", new Object[][] {
                {101, 1, "Equipos", "LATAM", "2026-10-18 18:00:00", "2026-10-18 18:35:00", "FINALIZADA"},
                {102, 1, "Equipos", "LATAM", "2026-10-19 19:00:00", "2026-10-19 19:40:00", "FINALIZADA"},
                {103, 2, "Cooperativo", "LATAM", "2026-10-19 20:00:00", "2026-10-19 20:30:00", "FINALIZADA"},
                {104, 2, "Duelo", "EU", "2026-10-19 21:00:00", "2026-10-19 21:20:00", "FINALIZADA"},
                {105, 3, "Carrera", "LATAM", "2026-10-20 16:00:00", "2026-10-20 16:15:00", "FINALIZADA"},
                {106, 3, "Carrera", "NA", "2026-10-20 16:20:00", "2026-10-20 16:40:00", "FINALIZADA"},
                {107, 4, "Equipos", "LATAM", "2026-10-20 17:00:00", "2026-10-20 17:25:00", "FINALIZADA"},
                {108, 4, "Duelo", "EU", "2026-10-20 18:00:00", "2026-10-20 18:20:00", "FINALIZADA"},
                {109, 5, "Cooperativo", "LATAM", "2026-10-21 21:00:00", null, "EN_CURSO"},
                {110, 5, "Cooperativo", "LATAM", "2026-10-21 21:30:00", null, "ESPERA"}
        });

        insertar(con, "INSERT OR IGNORE INTO participacion (id_usuario, id_partida, puntos, bajas, muertes, resultado) VALUES (?, ?, ?, ?, ?, ?)", new Object[][] {
                {1, 101, 170, 14, 6, "VICTORIA"}, {2, 101, 90, 8, 9, "DERROTA"},
                {1, 102, 120, 11, 8, "VICTORIA"}, {3, 102, 80, 7, 10, "DERROTA"},
                {4, 103, 150, 12, 3, "VICTORIA"}, {5, 104, 50, 3, 12, "DERROTA"},
                {6, 105, 200, 0, 0, "VICTORIA"}, {7, 106, 70, 0, 0, "DERROTA"},
                {8, 107, 140, 8, 5, "VICTORIA"}, {9, 109, 0, 0, 0, null}
        });

        insertar(con, "INSERT OR IGNORE INTO estadistica_juego (id_usuario, id_juego, partidas_jugadas, victorias, derrotas, puntuacion_total, ultima_partida) VALUES (?, ?, ?, ?, ?, ?, ?)", new Object[][] {
                {1, 1, 2, 2, 0, 290, "2026-10-19 19:40:00"},
                {2, 1, 1, 0, 1, 90, "2026-10-18 18:35:00"},
                {3, 1, 1, 0, 1, 80, "2026-10-19 19:40:00"},
                {4, 2, 1, 1, 0, 150, "2026-10-19 20:30:00"},
                {5, 2, 1, 0, 1, 50, "2026-10-19 21:20:00"},
                {6, 3, 1, 1, 0, 200, "2026-10-20 16:15:00"},
                {7, 3, 1, 0, 1, 70, "2026-10-20 16:40:00"},
                {8, 4, 1, 1, 0, 140, "2026-10-20 17:25:00"},
                {9, 5, 0, 0, 0, 0, null},
                {10, 5, 0, 0, 0, 0, null}
        });

        insertar(con, "INSERT OR IGNORE INTO logro (id_logro, id_juego, titulo, descripcion, puntos_recompensa) VALUES (?, ?, ?, ?, ?)", new Object[][] {
                {1, 1, "Primera partida", "Disputa una partida", 100},
                {2, 1, "Dos victorias", "Gana dos partidas", 200},
                {3, 2, "Explorador", "Descubre un area", 50},
                {4, 2, "Maestro", "Supera una mision", 150},
                {5, 3, "Rapido", "Gana una carrera", 100},
                {6, 3, "Piloto", "Participa en una carrera", 300},
                {7, 4, "Estratega", "Gana un duelo", 100},
                {8, 4, "Comandante", "Gana cinco duelos", 250},
                {9, 5, "Pionero", "Inicia una aventura", 50},
                {10, 5, "Leyenda", "Completa la aventura", 400}
        });

        insertar(con, "INSERT OR IGNORE INTO logro_desbloqueado (id_usuario, id_logro, fecha_desbloqueo) VALUES (?, ?, ?)", new Object[][] {
                {1, 1, "2026-10-18 18:35:00"}, {1, 2, "2026-10-19 19:40:00"},
                {2, 1, "2026-10-18 18:35:00"}, {3, 1, "2026-10-19 19:40:00"},
                {4, 3, "2026-10-19 20:30:00"}, {5, 4, "2026-10-19 21:20:00"},
                {6, 5, "2026-10-20 16:15:00"}, {7, 6, "2026-10-20 16:40:00"},
                {8, 7, "2026-10-20 17:25:00"}, {9, 9, "2026-10-21 21:00:00"}
        });
    }

    private static void insertar(Connection con, String sql, Object[][] registros) throws SQLException
    {
        try(PreparedStatement ps = con.prepareStatement(sql))
        {
            for(Object[] registro : registros)
            {
                for(int i = 0; i < registro.length; i++)
                {
                    ps.setObject(i + 1, registro[i]);
                }
                ps.executeUpdate();
            }
        }
    }
}