package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseDeDatos {
	private static final String URL = "jdbc:sqlite:multijugador.db";
	
	public static Connection getConnection() throws SQLException
	{
		Connection con = DriverManager.getConnection(URL);
		
		try
		{
			try(Statement stmt = con.createStatement())
			{
				stmt.execute("PRAGMA foreign_keys = ON");
			}
			
			return con;
		} catch(SQLException e)
		{
			con.close();
			throw e;
			
		}
	}
	
	public static void inicializar() throws SQLException
	{
		try(Connection con = getConnection(); Statement stmt = con.createStatement())
		{
			String sqlUsuario = """
					CREATE TABLE IF NOT EXISTS usuario
					(
						id_usuario INTEGER PRIMARY KEY,
						nombre_usuario TEXT NOT NULL UNIQUE,
						correo TEXT NOT NULL UNIQUE,
						hash_contrasena TEXT NOT NULL,
						fecha_registro TEXT DEFAULT CURRENT_TIMESTAMP,
						estado TEXT NOT NULL
					)
					""";
			stmt.execute(sqlUsuario);
			
			String sqlPerfil = """
					CREATE TABLE IF NOT EXISTS perfil_jugador
					(
						id_usuario INTEGER PRIMARY KEY,
						apodo TEXT,
						avatar_url TEXT,
						pais TEXT,
						fecha_actualizacion DEFAULT CURRENT_TIMESTAMP,
						
						FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
					)
					""";
			stmt.execute(sqlPerfil);
			
			String sqlJuego = """
					CREATE TABLE IF NOT EXISTS juego
					(
						id_juego INTEGER PRIMARY KEY,
						titulo TEXT NOT NULL,
						genero TEXT,
						version_actual TEXT NOT NULL,
						max_jugadores INTEGER NOT NULL,
						estado TEXT NOT NULL
					)
					""";
			stmt.execute(sqlJuego);
			
			String sqlPartida = """
					CREATE TABLE IF NOT EXISTS partida
					(
						id_partida INTEGER PRIMARY KEY,
						id_juego INTEGER NOT NULL,
						modo TEXT NOT NULL,
						region TEXT NOT NULL,
						inicio TEXT DEFAULT CURRENT_TIMESTAMP NOT NULL,
						fin TEXT,
						estado TEXT NOT NULL,
						
						FOREIGN KEY (id_juego) REFERENCES juego(id_juego)
					)
					""";
			stmt.execute(sqlPartida);
			
			String sqlParticipacion = """
					CREATE TABLE IF NOT EXISTS participacion
					(
						id_usuario INTEGER NOT NULL,
						id_partida INTEGER NOT NULL,
						fecha_union TEXT DEFAULT CURRENT_TIMESTAMP,
						puntos INTEGER NOT NULL DEFAULT 0,
						bajas INTEGER NOT NULL DEFAULT 0,
						muertes INTEGER NOT NULL DEFAULT 0,
						resultado TEXT,
						
						PRIMARY KEY(id_usuario, id_partida),
						
						FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
						FOREIGN KEY (id_partida) REFERENCES partida(id_partida)
					)
					""";
			
			stmt.execute(sqlParticipacion);
			
			String sqlEstadistica = """
			        CREATE TABLE IF NOT EXISTS estadistica_juego
			        (
			            id_usuario INTEGER NOT NULL,
			            id_juego INTEGER NOT NULL,
			            partidas_jugadas INTEGER NOT NULL DEFAULT 0,
			            victorias INTEGER NOT NULL DEFAULT 0,
			            derrotas INTEGER NOT NULL DEFAULT 0,
			            puntuacion_total INTEGER NOT NULL DEFAULT 0,
			            ultima_partida TEXT,

			            PRIMARY KEY (id_usuario, id_juego),

			            FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
			            FOREIGN KEY (id_juego) REFERENCES juego(id_juego)
			        )
			        """;

			stmt.execute(sqlEstadistica);
			
			String sqlLogro = """
			        CREATE TABLE IF NOT EXISTS logro
			        (
			            id_logro INTEGER PRIMARY KEY,
			            id_juego INTEGER NOT NULL,
			            titulo TEXT NOT NULL,
			            descripcion TEXT,
			            puntos_recompensa INTEGER NOT NULL DEFAULT 0,

			            FOREIGN KEY (id_juego) REFERENCES juego(id_juego)
			        )
			        """;

			stmt.execute(sqlLogro);
			
			String sqlLogroDesbloqueado = """
			        CREATE TABLE IF NOT EXISTS logro_desbloqueado
			        (
			            id_usuario INTEGER NOT NULL,
			            id_logro INTEGER NOT NULL,
			            fecha_desbloqueo TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

			            PRIMARY KEY (id_usuario, id_logro),

			            FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
			            FOREIGN KEY (id_logro) REFERENCES logro(id_logro)
			        )
			        """;

			stmt.execute(sqlLogroDesbloqueado);
		}
	}
}
