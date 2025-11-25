import sqlite3
from dao.conexion import obtener_conexion
from models.cancha import Cancha


class CanchaDAO:
    """DAO para la entidad Cancha."""

    @staticmethod
    def crear(cancha: Cancha):
        conexion = obtener_conexion()
        try:
            conexion.execute(
                """
                INSERT INTO cancha (nombre, tipo_superficie, iluminacion, precio_hora, servicios)
                VALUES (?, ?, ?, ?, ?)
                """,
                (
                    cancha.nombre,
                    cancha.tipo_superficie,
                    int(cancha.iluminacion),
                    cancha.precio_hora,
                    cancha.servicios,
                ),
            )
            conexion.commit()
        except sqlite3.Error as e:
            print(f"❌ Error al crear cancha: {e}")
        finally:
            conexion.close()

    @staticmethod
    def obtener_todas():
        conexion = obtener_conexion()
        canchas = []
        try:
            cursor = conexion.execute("SELECT * FROM cancha")
            for fila in cursor.fetchall():
                canchas.append(Cancha(*fila))
        except sqlite3.Error as e:
            print(f"❌ Error al obtener canchas: {e}")
        finally:
            conexion.close()
        return canchas

    @staticmethod
    def actualizar(cancha: Cancha):
        """Actualiza los datos de una cancha existente."""
        conexion = obtener_conexion()
        try:
            conexion.execute(
                """
                UPDATE cancha 
                SET nombre = ?, tipo_superficie = ?, iluminacion = ?, precio_hora = ?, servicios = ?
                WHERE id_cancha = ?
                """,
                (
                    cancha.nombre,
                    cancha.tipo_superficie,
                    int(cancha.iluminacion),
                    cancha.precio_hora,
                    cancha.servicios,
                    cancha.id_cancha,
                ),
            )
            conexion.commit()
            print(f"✅ Cancha actualizada correctamente (ID {cancha.id_cancha})")
        except sqlite3.IntegrityError as e:
            print(f"❌ Error de integridad: {e}")
            raise ValueError("Nombre de cancha duplicado o datos inválidos.")
        except sqlite3.Error as e:
            print(f"❌ Error al actualizar cancha: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def eliminar(id_cancha):
        """Elimina una cancha por ID y resetea el autoincrement."""
        from dao.conexion import resetear_autoincrement
        
        conexion = obtener_conexion()
        try:
            conexion.execute("DELETE FROM cancha WHERE id_cancha = ?", (id_cancha,))
            conexion.commit()
            print(f"✅ Cancha eliminada correctamente (ID {id_cancha})")
            
            # Resetear el autoincrement
            resetear_autoincrement("cancha")
        except sqlite3.IntegrityError as e:
            print(f"❌ Error de integridad: {e}")
            raise ValueError("No se puede eliminar: esta cancha tiene reservas asociadas.")
        except sqlite3.Error as e:
            print(f"❌ Error al eliminar cancha: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

