import sqlite3
from pathlib import Path

DB_PATH = Path(__file__).resolve().parent.parent / "data" / "dao_canchas.db"

def obtener_conexion():
    """Devuelve una conexión a la base de datos SQLite."""
    conexion = sqlite3.connect(DB_PATH)
    conexion.execute("PRAGMA foreign_keys = ON;")
    return conexion

def resetear_autoincrement(tabla):
    """Resetea el autoincrement de una tabla después de eliminar registros."""
    conexion = obtener_conexion()
    try:
        # Obtener el máximo ID actual de la tabla
        cursor = conexion.cursor()
        id_column = "id_" + tabla
        cursor.execute(f"SELECT MAX({id_column}) FROM {tabla}")
        max_id = cursor.fetchone()[0] or 0
        
        # Resetear la secuencia sqlite_sequence
        cursor.execute(f"UPDATE sqlite_sequence SET seq = {max_id} WHERE name = '{tabla}'")
        conexion.commit()
        print(f"✅ Autoincrement de '{tabla}' reseteado a {max_id}")
    except Exception as e:
        print(f"❌ Error al resetear autoincrement: {e}")
    finally:
        conexion.close()

def resetear_todos_autoincrement():
    """Resetea todos los autoincrement a 0."""
    conexion = obtener_conexion()
    try:
        cursor = conexion.cursor()
        # Resetear todos los valores en sqlite_sequence a 0
        cursor.execute("DELETE FROM sqlite_sequence")
        conexion.commit()
        print("✅ Todos los autoincrement han sido reseteados a 0")
    except Exception as e:
        print(f"❌ Error al resetear autoincrement: {e}")
    finally:
        conexion.close()


