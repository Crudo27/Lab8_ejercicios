package seguridad;

import java.util.Scanner;

public class Seguridad {
    public static boolean confirmar(Scanner scanner)
    {
        String clave = System.getenv("LAB8_CLAVE");
        if(clave == null || clave.isBlank())
        {
            clave = "lab8";
        }
        System.out.print("Clave para confirmar los cambios: ");
        String ingresada = scanner.nextLine();
        return ingresada.equals(clave);
    }
}