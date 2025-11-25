import sqlite3
from dao.conexion import obtener_conexion
from models.servicio import Servicio


class ServicioDAO:
    """DAO para la entidad Servicio."""

    @staticmethod
    def crear(servicio: Servicio):
        """Crea un nuevo servicio."""
        conexion = obtener_conexion()
        try:
            conexion.execute(
                "INSERT INTO servicio (nombre, costo) VALUES (?, ?)",
                (servicio.nombre, servicio.costo),
            )
            conexion.commit()
            print(f"✅ Servicio creado: {servicio.nombre}")
        except sqlite3.IntegrityError as e:
            print(f"❌ Error de integridad: {e}")
            raise ValueError("Servicio duplicado.")
        except sqlite3.Error as e:
            print(f"❌ Error al crear servicio: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def obtener_todos():
        """Obtiene todos los servicios."""
        conexion = obtener_conexion()
        servicios = []
        try:
            cursor = conexion.execute("SELECT * FROM servicio")
            for fila in cursor.fetchall():
                servicios.append(Servicio(*fila))
        except sqlite3.Error as e:
            print(f"❌ Error al obtener servicios: {e}")
        finally:
            conexion.close()
        return servicios

    @staticmethod
    def actualizar(servicio: Servicio):
        """Actualiza un servicio existente."""
        conexion = obtener_conexion()
        try:
            conexion.execute(
                "UPDATE servicio SET nombre = ?, costo = ? WHERE id_servicio = ?",
                (servicio.nombre, servicio.costo, servicio.id_servicio),
            )
            conexion.commit()
            print(f"✅ Servicio actualizado (ID {servicio.id_servicio})")
        except sqlite3.IntegrityError as e:
            print(f"❌ Error de integridad: {e}")
            raise ValueError("Servicio duplicado.")
        except sqlite3.Error as e:
            print(f"❌ Error al actualizar servicio: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def eliminar(id_servicio):
        """Elimina un servicio por ID."""
        from dao.conexion import resetear_autoincrement
        
        conexion = obtener_conexion()
        try:
            conexion.execute("DELETE FROM servicio WHERE id_servicio = ?", (id_servicio,))
            conexion.commit()
            print(f"✅ Servicio eliminado (ID {id_servicio})")
            resetear_autoincrement("servicio")
        except sqlite3.IntegrityError as e:
            print(f"❌ Error de integridad: {e}")
            raise ValueError("No se puede eliminar: servicio asociado a canchas.")
        except sqlite3.Error as e:
            print(f"❌ Error al eliminar servicio: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def asignar_servicio_a_cancha(id_cancha, id_servicio):
        """Asocia un servicio a una cancha."""
        conexion = obtener_conexion()
        try:
            conexion.execute(
                "INSERT INTO cancha_servicio (id_cancha, id_servicio) VALUES (?, ?)",
                (id_cancha, id_servicio),
            )
            conexion.commit()
            print(f"✅ Servicio {id_servicio} asignado a cancha {id_cancha}")
        except sqlite3.IntegrityError:
            print(f"⚠️ El servicio ya está asignado a esta cancha")
        except sqlite3.Error as e:
            print(f"❌ Error al asignar servicio: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def desasignar_servicio_de_cancha(id_cancha, id_servicio):
        """Desasocia un servicio de una cancha."""
        conexion = obtener_conexion()
        try:
            conexion.execute(
                "DELETE FROM cancha_servicio WHERE id_cancha = ? AND id_servicio = ?",
                (id_cancha, id_servicio),
            )
            conexion.commit()
            print(f"✅ Servicio {id_servicio} desasignado de cancha {id_cancha}")
        except sqlite3.Error as e:
            print(f"❌ Error al desasignar servicio: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def obtener_servicios_de_cancha(id_cancha):
        """Obtiene todos los servicios asociados a una cancha."""
        conexion = obtener_conexion()
        servicios = []
        try:
            cursor = conexion.execute(
                """
                SELECT s.id_servicio, s.nombre, s.costo
                FROM servicio s
                INNER JOIN cancha_servicio cs ON s.id_servicio = cs.id_servicio
                WHERE cs.id_cancha = ?
                """,
                (id_cancha,),
            )
            for fila in cursor.fetchall():
                servicios.append(Servicio(*fila))
        except sqlite3.Error as e:
            print(f"❌ Error al obtener servicios de cancha: {e}")
        finally:
            conexion.close()
        return servicios
