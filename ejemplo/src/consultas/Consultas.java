package consultas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Scanner;

import conexion.BaseDeDatos;
import principal.Principal;

public class Consultas {
    public static void menu(Scanner scanner) throws SQLException
    {
        int opcion;
        do
        {
            System.out.println("\n===== CONSULTAS CON CONDICIONES =====");
            System.out.println("1. Usuarios activos");
            System.out.println("2. Juegos por capacidad minima");
            System.out.println("3. Partidas de un juego");
            System.out.println("4. Historial de un usuario");
            System.out.println("5. Jugadores con puntaje minimo");
            System.out.println("6. Partidas finalizadas por region");
            System.out.println("7. Logros de un usuario");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");
            opcion = Principal.leerNumero(scanner);
            switch(opcion)
            {
                case 1 -> ejecutar("SELECT id_usuario, nombre_usuario, estado FROM usuario WHERE estado = ? ORDER BY nombre_usuario", "ACTIVO");
                case 2 -> {
                    System.out.print("Capacidad minima: ");
                    ejecutar("SELECT id_juego, titulo, max_jugadores FROM juego WHERE max_jugadores >= ? ORDER BY max_jugadores DESC", Principal.leerNumero(scanner));
                }
                case 3 -> {
                    System.out.print("ID del juego: ");
                    ejecutar("SELECT p.id_partida, j.titulo, p.modo, p.estado FROM partida p JOIN juego j ON p.id_juego = j.id_juego WHERE j.id_juego = ? ORDER BY p.id_partida", Principal.leerNumero(scanner));
                }
                case 4 -> {
                    System.out.print("ID del usuario: ");
                    ejecutar("SELECT u.nombre_usuario, j.titulo, p.id_partida, pa.puntos, pa.resultado FROM participacion pa JOIN usuario u ON pa.id_usuario = u.id_usuario JOIN partida p ON pa.id_partida = p.id_partida JOIN juego j ON p.id_juego = j.id_juego WHERE u.id_usuario = ? ORDER BY p.inicio DESC", Principal.leerNumero(scanner));
                }
                case 5 -> {
                    System.out.print("Puntaje minimo: ");
                    ejecutar("SELECT u.nombre_usuario, pa.id_partida, pa.puntos, pa.bajas FROM participacion pa JOIN usuario u ON pa.id_usuario = u.id_usuario WHERE pa.puntos >= ? ORDER BY pa.puntos DESC", Principal.leerNumero(scanner));
                }
                case 6 -> {
                    System.out.print("Region (LATAM, EU o NA): ");
                    ejecutar("SELECT p.id_partida, j.titulo, p.region, p.fin FROM partida p JOIN juego j ON p.id_juego = j.id_juego WHERE p.estado = ? AND p.region = ? ORDER BY p.fin DESC", "FINALIZADA", scanner.nextLine().trim());
                }
                case 7 -> {
                    System.out.print("ID del usuario: ");
                    ejecutar("SELECT u.nombre_usuario, l.titulo, j.titulo AS juego, ld.fecha_desbloqueo FROM logro_desbloqueado ld JOIN usuario u ON ld.id_usuario = u.id_usuario JOIN logro l ON ld.id_logro = l.id_logro JOIN juego j ON l.id_juego = j.id_juego WHERE u.id_usuario = ? ORDER BY ld.fecha_desbloqueo", Principal.leerNumero(scanner));
                }
                case 0 -> { }
                default -> System.out.println("Opcion invalida.");
            }
        } while(opcion != 0);
    }

    public static void ejecutar(String sql, Object... parametros) throws SQLException
    {
        try(Connection con = BaseDeDatos.getConnection(); PreparedStatement ps = con.prepareStatement(sql))
        {
            for(int i = 0; i < parametros.length; i++)
            {
                ps.setObject(i + 1, parametros[i]);
            }
            try(ResultSet rs = ps.executeQuery())
            {
                ResultSetMetaData meta = rs.getMetaData();
                int columnas = meta.getColumnCount();
                int filas = 0;
                for(int i = 1; i <= columnas; i++)
                {
                    System.out.printf("%-23s", meta.getColumnLabel(i));
                }
                System.out.println();
                while(rs.next())
                {
                    for(int i = 1; i <= columnas; i++)
                    {
                        String valor = rs.getString(i);
                        System.out.printf("%-23.23s", valor == null ? "NULL" : valor);
                    }
                    System.out.println();
                    filas++;
                }
                System.out.println("Registros encontrados: " + filas);
            }
        }
    }
}