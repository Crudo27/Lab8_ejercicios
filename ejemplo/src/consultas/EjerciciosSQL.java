package consultas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import conexion.BaseDeDatos;
import seguridad.Seguridad;

public class EjerciciosSQL {
    public static void aplicarCambios(Scanner scanner) throws SQLException
    {
        try(Connection con = BaseDeDatos.getConnection())
        {
            con.setAutoCommit(false);
            try
            {
                modificar(con, "UPDATE usuario SET estado = ? WHERE id_usuario = ?", "INACTIVO", 10);
                modificar(con, "UPDATE juego SET version_actual = ? WHERE id_juego = ?", "1.3", 1);
                modificar(con, "UPDATE perfil_jugador SET pais = ? WHERE id_usuario = ?", "Bolivia", 5);

                modificar(con, "DELETE FROM logro_desbloqueado WHERE id_usuario = ? AND id_logro = ?", 9, 9);
                modificar(con, "DELETE FROM participacion WHERE id_usuario = ? AND id_partida = ?", 9, 109);
                modificar(con, "DELETE FROM estadistica_juego WHERE id_usuario = ? AND id_juego = ?", 10, 5);

                if(Seguridad.confirmar(scanner))
                {
                    con.commit();
                    System.out.println("Las 3 actualizaciones y las 3 eliminaciones fueron confirmadas.");
                }
                else
                {
                    con.rollback();
                    System.out.println("Clave incorrecta: ninguna de las seis operaciones se guardo.");
                }
            }
            catch(SQLException e)
            {
                con.rollback();
                throw e;
            }
        }
    }

    private static void modificar(Connection con, String sql, Object... parametros) throws SQLException
    {
        try(PreparedStatement ps = con.prepareStatement(sql))
        {
            for(int i = 0; i < parametros.length; i++)
            {
                ps.setObject(i + 1, parametros[i]);
            }
            System.out.println("Registros afectados: " + ps.executeUpdate());
        }
    }
}