package principal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

import conexion.BaseDeDatos;
import conexion.DatosPrueba;
import consultas.Consultas;
import consultas.EjerciciosSQL;
import gestor.GestorCRUD;
import gestor.GestorRegistros;
import seguridad.Seguridad;

public class Principal {
    public static void main(String[] args)
    {
        try(Scanner scanner = new Scanner(System.in))
        {
            BaseDeDatos.inicializar();
            System.out.println("Base de datos inicializada correctamente.");
            int opcion;

            do
            {
                System.out.println("\n===== PLATAFORMA MULTIJUGADOR =====");
                System.out.println("1. Cargar registros de prueba");
                System.out.println("2. Consultas SQL");
                System.out.println("3. Gestionar tablas (CRUD)");
                System.out.println("4. Consultar arreglo de objetos");
                System.out.println("5. Comprobar transacciones");
                System.out.println("6. Ejecutar 3 UPDATE y 3 DELETE");
                System.out.println("0. Salir");
                System.out.print("Opcion: ");
                opcion = leerNumero(scanner);

                try
                {
                    switch(opcion)
                    {
                        case 1 -> cargarDatos(scanner);
                        case 2 -> Consultas.menu(scanner);
                        case 3 -> GestorCRUD.menu(scanner);
                        case 4 -> GestorRegistros.menu(scanner);
                        case 5 -> comprobarTransaccion(scanner);
                        case 6 -> EjerciciosSQL.aplicarCambios(scanner);
                        case 0 -> System.out.println("Hasta luego.");
                        default -> System.out.println("Opcion invalida.");
                    }
                }
                catch(SQLException e)
                {
                    System.out.println("Error de base de datos: " + e.getMessage());
                }
            } while(opcion != 0);
        }
        catch(SQLException e)
        {
            System.out.println("No se pudo iniciar: " + e.getMessage());
        }
    }

    public static int leerNumero(Scanner scanner)
    {
        try
        {
            return Integer.parseInt(scanner.nextLine().trim());
        }
        catch(NumberFormatException e)
        {
            return -1;
        }
    }

    private static void cargarDatos(Scanner scanner) throws SQLException
    {
        try(Connection con = BaseDeDatos.getConnection())
        {
            con.setAutoCommit(false);
            try
            {
                DatosPrueba.cargar(con);
                if(Seguridad.confirmar(scanner))
                {
                    con.commit();
                    System.out.println("Carga confirmada: 10 registros por tabla como minimo.");
                }
                else
                {
                    con.rollback();
                    System.out.println("Clave incorrecta. Se revirtio la carga.");
                }
            }
            catch(SQLException e)
            {
                con.rollback();
                throw e;
            }
        }
    }

    private static void comprobarTransaccion(Scanner scanner) throws SQLException
    {
        try(Connection con = BaseDeDatos.getConnection())
        {
            con.setAutoCommit(false);
            try
            {
                String sql = "UPDATE usuario SET estado = estado WHERE id_usuario = 1";
                try(var ps = con.prepareStatement(sql))
                {
                    int filas = ps.executeUpdate();
                    System.out.println("Registros afectados antes de confirmar: " + filas);
                }
                if(Seguridad.confirmar(scanner))
                {
                    con.commit();
                    System.out.println("Transaccion confirmada con commit().");
                }
                else
                {
                    con.rollback();
                    System.out.println("Transaccion revertida con rollback().");
                }
            }
            catch(SQLException e)
            {
                con.rollback();
                throw e;
            }
        }
    }
}