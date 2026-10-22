package gestor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import conexion.BaseDeDatos;
import principal.Principal;
import seguridad.Seguridad;

public class GestorCRUD {
    public static final String[] TABLAS = {
            "usuario", "perfil_jugador", "juego", "partida", "participacion",
            "estadistica_juego", "logro", "logro_desbloqueado"
    };

    private record Campo(String nombre, String tipo, boolean obligatorio, int clave, String defecto) { }

    public static boolean esTablaValida(String tabla)
    {
        for(String nombre : TABLAS)
        {
            if(nombre.equals(tabla))
            {
                return true;
            }
        }
        return false;
    }

    public static void menu(Scanner scanner) throws SQLException
    {
        System.out.println("\n===== TABLAS DISPONIBLES =====");
        for(int i = 0; i < TABLAS.length; i++)
        {
            System.out.println((i + 1) + ". " + TABLAS[i]);
        }
        System.out.print("Seleccione una tabla: ");
        int indice = Principal.leerNumero(scanner) - 1;
        if(indice < 0 || indice >= TABLAS.length)
        {
            System.out.println("Tabla invalida.");
            return;
        }
        String tabla = TABLAS[indice];
        int opcion;
        do
        {
            System.out.println("\n===== " + tabla.toUpperCase() + " =====");
            System.out.println("1. Insertar registro");
            System.out.println("2. Mostrar registros");
            System.out.println("3. Actualizar registro");
            System.out.println("4. Eliminar registro");
            System.out.println("0. Volver");
            System.out.print("Opcion: ");
            opcion = Principal.leerNumero(scanner);
            switch(opcion)
            {
                case 1 -> insertar(tabla, scanner);
                case 2 -> mostrar(tabla);
                case 3 -> actualizar(tabla, scanner);
                case 4 -> eliminar(tabla, scanner);
                case 0 -> { }
                default -> System.out.println("Opcion invalida.");
            }
        } while(opcion != 0);
    }

    private static List<Campo> obtenerCampos(Connection con, String tabla) throws SQLException
    {
        List<Campo> campos = new ArrayList<>();
        try(Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tabla + ")"))
        {
            while(rs.next())
            {
                campos.add(new Campo(rs.getString("name"), rs.getString("type"), rs.getInt("notnull") != 0,
                        rs.getInt("pk"), rs.getString("dflt_value")));
            }
        }
        return campos;
    }

    private static Object pedirDato(Scanner scanner, Campo campo, boolean admiteVacio)
    {
        while(true)
        {
            System.out.print(campo.nombre() + " (" + campo.tipo() + ")" + (admiteVacio ? " [vacio = NULL o valor predeterminado]" : "") + ": ");
            String valor = scanner.nextLine().trim();
            if(valor.isEmpty())
            {
                if(admiteVacio)
                {
                    return null;
                }
                System.out.println("El campo es obligatorio.");
                continue;
            }
            if(campo.tipo().toUpperCase().contains("INT"))
            {
                try
                {
                    return Integer.parseInt(valor);
                }
                catch(NumberFormatException e)
                {
                    System.out.println("Debe ingresar un numero entero.");
                    continue;
                }
            }
            return valor;
        }
    }

    private static void insertar(String tabla, Scanner scanner) throws SQLException
    {
        try(Connection con = BaseDeDatos.getConnection())
        {
            List<Campo> campos = obtenerCampos(con, tabla);
            List<String> nombres = new ArrayList<>();
            List<Object> valores = new ArrayList<>();
            for(Campo campo : campos)
            {
                boolean claveAutomatica = (tabla.equals("usuario") || tabla.equals("juego") || tabla.equals("partida") || tabla.equals("logro")) && campo.clave() == 1 && campo.tipo().equalsIgnoreCase("INTEGER");
                boolean opcional = campo.defecto() != null || !campo.obligatorio() && campo.clave() == 0 || claveAutomatica;
                Object valor = pedirDato(scanner, campo, opcional);
                if(valor != null)
                {
                    nombres.add(campo.nombre());
                    valores.add(valor);
                }
            }
            String sql;
            if(nombres.isEmpty())
            {
                sql = "INSERT INTO " + tabla + " DEFAULT VALUES";
            }
            else
            {
                sql = "INSERT INTO " + tabla + " (" + String.join(", ", nombres) + ") VALUES (" + String.join(", ", java.util.Collections.nCopies(nombres.size(), "?")) + ")";
            }
            con.setAutoCommit(false);
            if(tabla.equals("participacion") && !puedeIngresar(con, nombres, valores))
            {
                con.rollback();
                System.out.println("El usuario ya participa en otra partida activa.");
                return;
            }
            try(PreparedStatement ps = con.prepareStatement(sql))
            {
                for(int i = 0; i < valores.size(); i++)
                {
                    ps.setObject(i + 1, valores.get(i));
                }
                confirmarCambio(con, ps, scanner);
            }
            catch(SQLException e)
            {
                con.rollback();
                System.out.println("No se pudo insertar: " + e.getMessage());
            }
        }
    }

    private static void mostrar(String tabla) throws SQLException
    {
        try(Connection con = BaseDeDatos.getConnection(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabla + " LIMIT 100"))
        {
            var meta = rs.getMetaData();
            int columnas = meta.getColumnCount();
            for(int i = 1; i <= columnas; i++)
            {
                System.out.printf("%-20.20s", meta.getColumnName(i));
            }
            System.out.println();
            int filas = 0;
            while(rs.next())
            {
                for(int i = 1; i <= columnas; i++)
                {
                    Object valor = rs.getObject(i);
                    System.out.printf("%-20.20s", valor == null ? "NULL" : valor.toString());
                }
                System.out.println();
                filas++;
            }
            System.out.println("Registros mostrados: " + filas);
        }
    }

    private static List<Campo> claves(List<Campo> campos) throws SQLException
    {
        List<Campo> claves = campos.stream().filter(c -> c.clave() > 0).sorted((a, b) -> Integer.compare(a.clave(), b.clave())).toList();
        if(claves.isEmpty())
        {
            throw new SQLException("Esta tabla no tiene una clave primaria definida.");
        }
        return claves;
    }

    private static List<Object> pedirClaves(Scanner scanner, List<Campo> claves)
    {
        List<Object> valores = new ArrayList<>();
        System.out.println("Identifique el registro por su clave primaria:");
        for(Campo clave : claves)
        {
            valores.add(pedirDato(scanner, clave, false));
        }
        return valores;
    }

    private static String condicion(List<Campo> claves)
    {
        return String.join(" AND ", claves.stream().map(c -> c.nombre() + " = ?").toList());
    }

    private static void actualizar(String tabla, Scanner scanner) throws SQLException
    {
        try(Connection con = BaseDeDatos.getConnection())
        {
            List<Campo> campos = obtenerCampos(con, tabla);
            List<Campo> claves = claves(campos);
            System.out.println("Campos modificables:");
            for(Campo campo : campos)
            {
                if(campo.clave() == 0)
                {
                    System.out.println("- " + campo.nombre());
                }
            }
            System.out.print("Campo que desea modificar: ");
            String nombre = scanner.nextLine().trim();
            Campo seleccionado = null;
            for(Campo campo : campos)
            {
                if(campo.nombre().equals(nombre) && campo.clave() == 0)
                {
                    seleccionado = campo;
                }
            }
            if(seleccionado == null)
            {
                System.out.println("Campo invalido.");
                return;
            }
            Object nuevoValor = pedirDato(scanner, seleccionado, !seleccionado.obligatorio());
            List<Object> ids = pedirClaves(scanner, claves);
            String sql = "UPDATE " + tabla + " SET " + nombre + " = ? WHERE " + condicion(claves);
            con.setAutoCommit(false);
            try(PreparedStatement ps = con.prepareStatement(sql))
            {
                ps.setObject(1, nuevoValor);
                for(int i = 0; i < ids.size(); i++)
                {
                    ps.setObject(i + 2, ids.get(i));
                }
                confirmarCambio(con, ps, scanner);
            }
            catch(SQLException e)
            {
                con.rollback();
                System.out.println("No se pudo actualizar: " + e.getMessage());
            }
        }
    }

    private static void eliminar(String tabla, Scanner scanner) throws SQLException
    {
        try(Connection con = BaseDeDatos.getConnection())
        {
            List<Campo> claves = claves(obtenerCampos(con, tabla));
            List<Object> ids = pedirClaves(scanner, claves);
            String sql = "DELETE FROM " + tabla + " WHERE " + condicion(claves);
            con.setAutoCommit(false);
            try(PreparedStatement ps = con.prepareStatement(sql))
            {
                for(int i = 0; i < ids.size(); i++)
                {
                    ps.setObject(i + 1, ids.get(i));
                }
                confirmarCambio(con, ps, scanner);
            }
            catch(SQLException e)
            {
                con.rollback();
                System.out.println("No se pudo eliminar: " + e.getMessage());
            }
        }
    }

    private static boolean puedeIngresar(Connection con, List<String> nombres, List<Object> valores) throws SQLException
    {
        if(!nombres.contains("id_usuario") || !nombres.contains("id_partida"))
        {
            return false;
        }
        int idUsuario = ((Number) valores.get(nombres.indexOf("id_usuario"))).intValue();
        int idPartida = ((Number) valores.get(nombres.indexOf("id_partida"))).intValue();
        try(PreparedStatement ps = con.prepareStatement("SELECT estado FROM partida WHERE id_partida = ?"))
        {
            ps.setInt(1, idPartida);
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next())
                {
                    return false;
                }
                if(!"EN_CURSO".equalsIgnoreCase(rs.getString(1)))
                {
                    return true;
                }
            }
        }
        String sql = "SELECT COUNT(*) FROM participacion pa JOIN partida p ON pa.id_partida = p.id_partida WHERE pa.id_usuario = ? AND pa.id_partida <> ? AND p.estado = ?";
        try(PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idPartida);
            ps.setString(3, "EN_CURSO");
            try(ResultSet rs = ps.executeQuery())
            {
                return rs.next() && rs.getInt(1) == 0;
            }
        }
    }

    private static void confirmarCambio(Connection con, PreparedStatement ps, Scanner scanner) throws SQLException
    {
        int filas = ps.executeUpdate();
        System.out.println("Registros afectados: " + filas);
        if(Seguridad.confirmar(scanner))
        {
            con.commit();
            System.out.println("Cambios confirmados con commit().");
        }
        else
        {
            con.rollback();
            System.out.println("Clave incorrecta. Cambios revertidos con rollback().");
        }
    }
}