package gestor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import conexion.BaseDeDatos;
import modelo.Registro;
import principal.Principal;

public class GestorRegistros {
    private Registro[] registros;

    public GestorRegistros()
    {
        registros = new Registro[0];
    }

    public void cargar(String tabla) throws SQLException
    {
        if(!GestorCRUD.esTablaValida(tabla))
        {
            throw new SQLException("Tabla no permitida.");
        }
        List<Registro> lista = new ArrayList<>();
        try(Connection con = BaseDeDatos.getConnection(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabla))
        {
            ResultSetMetaData meta = rs.getMetaData();
            while(rs.next())
            {
                Map<String, Object> campos = new LinkedHashMap<>();
                for(int i = 1; i <= meta.getColumnCount(); i++)
                {
                    campos.put(meta.getColumnName(i), rs.getObject(i));
                }
                lista.add(new Registro(campos));
            }
        }
        registros = lista.toArray(new Registro[0]);
    }

    public Registro[] consultar(String[] campos, String campoFiltro, String operador, String valor, String campoOrden, boolean ascendente, int limite)
    {
        List<Registro> encontrados = new ArrayList<>();
        for(Registro registro : registros)
        {
            if(campoFiltro.isBlank() || cumple(registro.get(campoFiltro), operador, valor))
            {
                encontrados.add(registro);
            }
        }
        if(!campoOrden.isBlank())
        {
            Comparator<Registro> comparador = (a, b) -> comparar(a.get(campoOrden), b.get(campoOrden));
            encontrados.sort(ascendente ? comparador : comparador.reversed());
        }
        if(limite > 0 && encontrados.size() > limite)
        {
            encontrados = new ArrayList<>(encontrados.subList(0, limite));
        }
        for(Registro registro : encontrados)
        {
            for(String campo : campos)
            {
                System.out.print(campo + ": " + registro.get(campo) + "  ");
            }
            System.out.println();
        }
        System.out.println("Registros obtenidos desde Java: " + encontrados.size());
        return encontrados.toArray(new Registro[0]);
    }

    private boolean cumple(Object actual, String operador, String valor)
    {
        if(actual == null)
        {
            return operador.equals("=") && valor.equalsIgnoreCase("NULL");
        }
        int comparacion = comparar(actual, valor);
        return switch(operador)
        {
            case "=" -> comparacion == 0;
            case "!=" -> comparacion != 0;
            case ">" -> comparacion > 0;
            case ">=" -> comparacion >= 0;
            case "<" -> comparacion < 0;
            case "<=" -> comparacion <= 0;
            case "contiene" -> actual.toString().toLowerCase().contains(valor.toLowerCase());
            default -> false;
        };
    }

    private int comparar(Object a, Object b)
    {
        if(a == null && b == null) return 0;
        if(a == null) return -1;
        if(b == null) return 1;
        try
        {
            return Double.compare(Double.parseDouble(a.toString()), Double.parseDouble(b.toString()));
        }
        catch(NumberFormatException e)
        {
            return a.toString().compareToIgnoreCase(b.toString());
        }
    }

    public static void menu(Scanner scanner) throws SQLException
    {
        System.out.println("\nTablas disponibles: " + String.join(", ", GestorCRUD.TABLAS));
        System.out.print("Tabla a cargar: ");
        String tabla = scanner.nextLine().trim().toLowerCase();
        if(!GestorCRUD.esTablaValida(tabla))
        {
            System.out.println("Tabla invalida.");
            return;
        }
        GestorRegistros gestor = new GestorRegistros();
        gestor.cargar(tabla);
        if(gestor.registros.length == 0)
        {
            System.out.println("No hay registros en esa tabla.");
            return;
        }
        String[] disponibles = gestor.registros[0].obtenerCampos();
        System.out.println("Campos: " + String.join(", ", disponibles));
        System.out.print("Campos a mostrar (separados por coma o *): ");
        String entrada = scanner.nextLine().trim();
        String[] campos = entrada.equals("*") ? disponibles : Arrays.stream(entrada.split(",")).map(String::trim).toArray(String[]::new);
        for(String campo : campos)
        {
            if(!gestor.registros[0].contiene(campo))
            {
                System.out.println("Campo no valido: " + campo);
                return;
            }
        }
        System.out.print("Campo de filtro (vacio = ninguno): ");
        String campoFiltro = scanner.nextLine().trim();
        if(!campoFiltro.isEmpty() && !gestor.registros[0].contiene(campoFiltro))
        {
            System.out.println("Campo de filtro invalido.");
            return;
        }
        String operador = "=";
        String valor = "";
        if(!campoFiltro.isEmpty())
        {
            System.out.print("Operador (=, !=, >, >=, <, <=, contiene): ");
            operador = scanner.nextLine().trim();
            if(!Arrays.asList("=", "!=", ">", ">=", "<", "<=", "contiene").contains(operador))
            {
                System.out.println("Operador invalido.");
                return;
            }
            System.out.print("Valor a comparar: ");
            valor = scanner.nextLine();
        }
        System.out.print("Campo de orden (vacio = ninguno): ");
        String campoOrden = scanner.nextLine().trim();
        if(!campoOrden.isEmpty() && !gestor.registros[0].contiene(campoOrden))
        {
            System.out.println("Campo de orden invalido.");
            return;
        }
        System.out.print("Orden (ASC/DESC): ");
        boolean ascendente = !scanner.nextLine().trim().equalsIgnoreCase("DESC");
        System.out.print("Limite de registros (0 = todos): ");
        int limite = Principal.leerNumero(scanner);
        gestor.consultar(campos, campoFiltro, operador, valor, campoOrden, ascendente, limite);
    }
}