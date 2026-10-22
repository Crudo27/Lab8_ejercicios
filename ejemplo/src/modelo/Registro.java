package modelo;

import java.util.LinkedHashMap;
import java.util.Map;

public class Registro {
    private final Map<String, Object> campos;

    public Registro(Map<String, Object> campos)
    {
        this.campos = new LinkedHashMap<>(campos);
    }

    public Object get(String nombre)
    {
        return campos.get(nombre);
    }

    public boolean contiene(String nombre)
    {
        return campos.containsKey(nombre);
    }

    public String[] obtenerCampos()
    {
        return campos.keySet().toArray(new String[0]);
    }
}