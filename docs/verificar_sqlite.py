import ast
import json
import re
import sqlite3
from pathlib import Path

raiz = Path(__file__).resolve().parents[1]
base = (raiz / 'ejemplo/src/conexion/BaseDeDatos.java').read_text()
datos = (raiz / 'ejemplo/src/conexion/DatosPrueba.java').read_text()
sqls = re.findall(r'String sql\w+ = """(.*?)""";', base, re.S)
con = sqlite3.connect(':memory:')
con.execute('PRAGMA foreign_keys=ON')
for sql in sqls:
    con.execute(sql)

bloques = re.findall(r'insertar\(con, "([^"]+)", new Object\[\]\[\] \{(.*?)\}\);', datos, re.S)
assert len(bloques) == 8, len(bloques)
for sql, bloque in bloques:
    registros = []
    for fila in re.findall(r'\{([^{}]+)\}', bloque):
        valores = re.findall(r'"(?:[^"\\]|\\.)*"|null|-?\d+', fila)
        registros.append([None if v == 'null' else json.loads(v) if v.startswith('"') else int(v) for v in valores])
    assert len(registros) >= 10, (sql, len(registros))
    con.executemany(sql, registros)
con.commit()

conteos = con.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name").fetchall()
print('TABLAS CREADAS:', len(conteos))
for (tabla,) in conteos:
    total = con.execute(f'SELECT COUNT(*) FROM {tabla}').fetchone()[0]
    print(f'{tabla:<22}: {total} registros')
    assert total >= 10, (tabla, total)

consultas = [
    ('usuarios_activos', "SELECT id_usuario, nombre_usuario, estado FROM usuario WHERE estado = ? ORDER BY nombre_usuario", ('ACTIVO',)),
    ('juegos_capacidad', "SELECT id_juego, titulo, max_jugadores FROM juego WHERE max_jugadores >= ? ORDER BY max_jugadores DESC", (4,)),
    ('partidas_por_juego', "SELECT p.id_partida, j.titulo, p.modo, p.estado FROM partida p JOIN juego j ON p.id_juego = j.id_juego WHERE j.id_juego = ? ORDER BY p.id_partida", (1,)),
    ('historial_usuario', "SELECT u.nombre_usuario, j.titulo, p.id_partida, pa.puntos, pa.resultado FROM participacion pa JOIN usuario u ON pa.id_usuario = u.id_usuario JOIN partida p ON pa.id_partida = p.id_partida JOIN juego j ON p.id_juego = j.id_juego WHERE u.id_usuario = ? ORDER BY p.inicio DESC", (1,)),
    ('puntaje_minimo', "SELECT u.nombre_usuario, pa.id_partida, pa.puntos, pa.bajas FROM participacion pa JOIN usuario u ON pa.id_usuario = u.id_usuario WHERE pa.puntos >= ? ORDER BY pa.puntos DESC", (100,)),
    ('finalizadas_region', "SELECT p.id_partida, j.titulo, p.region, p.fin FROM partida p JOIN juego j ON p.id_juego = j.id_juego WHERE p.estado = ? AND p.region = ? ORDER BY p.fin DESC", ('FINALIZADA','LATAM')),
    ('logros_usuario', "SELECT u.nombre_usuario, l.titulo, j.titulo AS juego, ld.fecha_desbloqueo FROM logro_desbloqueado ld JOIN usuario u ON ld.id_usuario = u.id_usuario JOIN logro l ON ld.id_logro = l.id_logro JOIN juego j ON l.id_juego = j.id_juego WHERE u.id_usuario = ? ORDER BY ld.fecha_desbloqueo", (1,)),
]
for nombre, sql, params in consultas:
    filas = con.execute(sql,params).fetchall()
    assert filas, nombre
    print(f'CONSULTA {nombre:<20}: {len(filas)} filas')

try:
    con.execute("INSERT INTO perfil_jugador (id_usuario) VALUES (1)")
    raise AssertionError('Permitio duplicar perfil 1:1')
except sqlite3.IntegrityError:
    print('PRUEBA 1:1: duplicado rechazado')

try:
    con.execute("INSERT INTO participacion (id_usuario,id_partida) VALUES (999,101)")
    raise AssertionError('Permitio FK inexistente')
except sqlite3.IntegrityError:
    print('PRUEBA FK: usuario inexistente rechazado')

con.rollback()
con.execute('BEGIN')
con.execute("UPDATE juego SET version_actual=? WHERE id_juego=?",('9.9',1))
assert con.execute('SELECT version_actual FROM juego WHERE id_juego=1').fetchone()[0]=='9.9'
con.rollback()
assert con.execute('SELECT version_actual FROM juego WHERE id_juego=1').fetchone()[0]=='1.2'
print('PRUEBA ROLLBACK: cambio revertido correctamente')

con.execute('BEGIN')
cambios = [
    ("UPDATE usuario SET estado = ? WHERE id_usuario = ?", ('INACTIVO',10)),
    ("UPDATE juego SET version_actual = ? WHERE id_juego = ?", ('1.3',1)),
    ("UPDATE perfil_jugador SET pais = ? WHERE id_usuario = ?", ('Bolivia',5)),
    ("DELETE FROM logro_desbloqueado WHERE id_usuario = ? AND id_logro = ?", (9,9)),
    ("DELETE FROM participacion WHERE id_usuario = ? AND id_partida = ?", (9,109)),
    ("DELETE FROM estadistica_juego WHERE id_usuario = ? AND id_juego = ?", (10,5)),
]
for sql, args in cambios:
    cur=con.execute(sql,args)
    assert cur.rowcount == 1,(sql,cur.rowcount)
con.commit()
print('PRUEBA 3 UPDATE + 3 DELETE: seis operaciones confirmadas')
assert not con.execute('PRAGMA foreign_key_check').fetchall()
print('PRUEBA FINAL FK: sin referencias invalidas')
print('RESULTADO: VALIDACION SQL SUPERADA')