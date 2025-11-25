import sqlite3
from dao.conexion import obtener_conexion
from models.horario import Horario


class HorarioDAO:
    """DAO para la entidad Horario."""

    @staticmethod
    def crear(horario: Horario):
        conexion = obtener_conexion()
        try:
            conexion.execute(
                "INSERT INTO horario (hora_inicio, hora_fin) VALUES (?, ?)",
                (horario.hora_inicio, horario.hora_fin),
            )
            conexion.commit()
        except sqlite3.Error as e:
            print(f"❌ Error al crear horario: {e}")
        finally:
            conexion.close()

    @staticmethod
    def obtener_todos():
        conexion = obtener_conexion()
        horarios = []
        try:
            cursor = conexion.execute("SELECT * FROM horario")
            for fila in cursor.fetchall():
                horarios.append(Horario(*fila))
        except sqlite3.Error as e:
            print(f"❌ Error al obtener horarios: {e}")
        finally:
            conexion.close()
        return horarios

    @staticmethod
    def actualizar(horario: Horario):
        """Actualiza un horario existente."""
        conexion = obtener_conexion()
        try:
            conexion.execute(
                """
                UPDATE horario 
                SET hora_inicio = ?, hora_fin = ?
                WHERE id_horario = ?
                """,
                (horario.hora_inicio, horario.hora_fin, horario.id_horario),
            )
            conexion.commit()
            print(f"✅ Horario actualizado correctamente (ID {horario.id_horario})")
        except sqlite3.IntegrityError as e:
            print(f"❌ Error de integridad: {e}")
            raise ValueError("Horario duplicado o datos inválidos.")
        except sqlite3.Error as e:
            print(f"❌ Error al actualizar horario: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def eliminar(id_horario):
        """Elimina un horario por ID y resetea el autoincrement."""
        from dao.conexion import resetear_autoincrement
        
        conexion = obtener_conexion()
        try:
            conexion.execute("DELETE FROM horario WHERE id_horario = ?", (id_horario,))
            conexion.commit()
            print(f"✅ Horario eliminado correctamente (ID {id_horario})")
            
            # Resetear el autoincrement
            resetear_autoincrement("horario")
        except sqlite3.IntegrityError as e:
            print(f"❌ Error de integridad: {e}")
            raise ValueError("No se puede eliminar: este horario tiene reservas asociadas.")
        except sqlite3.Error as e:
            print(f"❌ Error al eliminar horario: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()
