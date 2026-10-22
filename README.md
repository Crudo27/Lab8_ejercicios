# Laboratorio 8 - Plataforma de juego multijugador

Universidad Católica de Santa María - Lenguajes de Programación III.

Proyecto Java con JDBC y SQLite basado en un modelo entidad-relación de ocho tablas. Incluye CRUD de todas las tablas, carga de diez registros iniciales por entidad, siete consultas parametrizadas, tres actualizaciones, tres eliminaciones, transacciones con `commit()` y `rollback()` y una clase gestora que consulta arreglos de objetos aplicando filtros, columnas seleccionadas, orden y límite.

## Cómo ejecutarlo en Eclipse

1. Importar la carpeta `ejemplo` con **File > Import > Existing Projects into Workspace**.
2. Verificar que el JDK del proyecto sea compatible con los *text blocks* y las expresiones `switch` (Java 17 o posterior).
3. Verificar **Project > Properties > Java Build Path > Libraries**. El proyecto original conserva la ruta local del autor al archivo `sqlite-jdbc` (`/home/gp/Descargas/sqlite-jdbc-3.53.4.0(1).jar`). Si se abre desde otra computadora, reemplazarla por la ubicación real del JAR. Para proyectos modulares, revisar el `module-info.java` y que el controlador pueda cargarse en tiempo de ejecución.
4. Ejecutar `principal.Principal` como **Java Application**.
5. Elegir la opción `1` para cargar los diez registros por tabla. La carga es repetible; usa `INSERT OR IGNORE` con claves fijas.

La base `multijugador.db` se genera en el directorio de trabajo de la configuración de ejecución. La clave de demostración es `lab8`, o el valor de la variable de entorno `LAB8_CLAVE` si se ha configurado. Es una clave didáctica, no un mecanismo de autenticación de producción.

## Menú

- `1`: inserción inicial de diez registros por tabla, sujeta a contraseña.
- `2`: siete consultas SQL con `WHERE`, `JOIN`, `ORDER BY` y parámetros.
- `3`: inserción, recuperación, actualización y eliminación sobre cualquiera de las ocho tablas.
- `4`: consulta desde un arreglo `Registro[]` cargado en Java; selección de campos, condición, orden y límite.
- `5`: demostración de `commit()` y `rollback()`.
- `6`: tres actualizaciones y tres eliminaciones filtradas, confirmadas o revertidas como una unidad.

La integridad referencial se habilita con `PRAGMA foreign_keys = ON` en cada conexión. Una comprobación Java evita agregar usuarios a dos partidas marcadas como `EN_CURSO` cuando se utiliza el menú de inserción de `participacion`. Para concurrencia real con múltiples clientes independientes se necesitaría reforzar esta regla a nivel de base de datos o serializar toda la operación.

## Precauciones

- Las contraseñas de usuarios de ejemplo tienen valores `HASH_DE_PRUEBA_n`. No son hashes reales; jamás utilizar estos valores para autenticación.
- La opción 6 modifica tres registros y elimina tres. Primero carga los datos iniciales.
- `CREATE TABLE IF NOT EXISTS` no aplica migraciones a tablas existentes. Si ya existe un esquema antiguo, no es reescrito automáticamente.
- Las capturas incluidas en el informe están señaladas como **simulaciones** de la consola de Eclipse. La compilación Java y las comprobaciones SQL se documentan por separado.
- La ruta del JAR guardada en `.classpath` es particular del equipo de origen; se mantiene para no modificar la configuración original sin necesidad.

## Estructura

```text
ejemplo/src/
  conexion/BaseDeDatos.java
  conexion/DatosPrueba.java
  consultas/Consultas.java
  consultas/EjerciciosSQL.java
  gestor/GestorCRUD.java
  gestor/GestorRegistros.java
  modelo/Registro.java
  principal/Principal.java
  seguridad/Seguridad.java
```

## Comprobación con Java 21+

El código fuente se compila utilizando las interfaces JDBC del JDK. Para **ejecutar** el programa se requiere que el controlador `sqlite-jdbc` esté disponible en el proyecto. La carpeta `docs` contiene las evidencias de diseño y comprobaciones SQL.