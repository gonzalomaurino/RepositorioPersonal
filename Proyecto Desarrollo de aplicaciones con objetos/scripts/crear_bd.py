"""
Script para crear la base de datos inicial del sistema de reservas de canchas deportivas.
Ejecuta el script SQL 'crear_tablas.sql' y genera 'dao_canchas.db' en la carpeta /data.
"""

import sqlite3
from pathlib import Path

# Rutas relativas
BASE_DIR = Path(__file__).resolve().parent.parent
DB_PATH = BASE_DIR / "data" / "dao_canchas.db"
SQL_PATH = BASE_DIR / "scripts" / "crear_tablas.sql"

def crear_base():
    """Crea la base de datos y ejecuta las sentencias SQL iniciales."""
    try:
        # Crear conexión
        conexion = sqlite3.connect(DB_PATH)
        conexion.execute("PRAGMA foreign_keys = ON;")

        # Leer y ejecutar el SQL
        with open(SQL_PATH, "r", encoding="utf-8") as archivo_sql:
            script_sql = archivo_sql.read()
            conexion.executescript(script_sql)

        conexion.commit()
        print(f"✅ Base de datos creada correctamente en: {DB_PATH}")
    
    except sqlite3.Error as e:
        print(f"❌ Error al crear la base de datos: {e}")
    
    finally:
        conexion.close()

if __name__ == "__main__":
    crear_base()
