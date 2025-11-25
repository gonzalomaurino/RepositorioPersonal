import sqlite3
from dao.conexion import obtener_conexion
from models.torneo import Torneo


class TorneoDAO:
    """DAO para la entidad Torneo.
    
    ⚠️ IMPORTANTE:
    Las reservas NO se hardcodean en la clase Torneo.
    Se guardan en tabla intermedia: torneo_reserva (id_torneo, id_reserva)
    Cada relación es un registro en BD, escalable a N reservas.
    """

    @staticmethod
    def crear(torneo: Torneo):
        """Inserta un nuevo torneo en la tabla torneo."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.execute(
                """
                INSERT INTO torneo (nombre, fecha_inicio, fecha_fin, categoria)
                VALUES (?, ?, ?, ?)
                """,
                (torneo.nombre, torneo.fecha_inicio, torneo.fecha_fin, torneo.categoria),
            )
            conexion.commit()
            return cursor.lastrowid
        except sqlite3.Error as e:
            print(f"❌ Error al crear torneo: {e}")
            return None
        finally:
            conexion.close()

    @staticmethod
    def obtener_todos():
        """Obtiene todos los torneos (sin reservas, tabla torneo solo)."""
        conexion = obtener_conexion()
        torneos = []
        try:
            cursor = conexion.execute("SELECT * FROM torneo")
            for fila in cursor.fetchall():
                torneos.append(Torneo(*fila))
        except sqlite3.Error as e:
            print(f"❌ Error al obtener torneos: {e}")
        finally:
            conexion.close()
        return torneos

    @staticmethod
    def asignar_reserva(id_torneo, id_reserva):
        """INSERTA una relación en tabla intermedia torneo_reserva.
        
        VALIDACIONES:
        - No permite asignar una reserva a múltiples torneos.
        - Una reserva solo puede estar en UN torneo.
        
        ESTO NO ES HARDCODE.
        Cada llamada = una fila en la BD.
        Escalable: puedes asignar 1, 10, 100 reservas.
        """
        conexion = obtener_conexion()
        try:
            # Validar que la reserva no esté ya asignada a otro torneo
            cursor = conexion.cursor()
            cursor.execute(
                "SELECT id_torneo FROM torneo_reserva WHERE id_reserva = ?",
                (id_reserva,)
            )
            resultado = cursor.fetchone()
            
            if resultado:
                torneo_existente = resultado[0]
                if torneo_existente != id_torneo:
                    raise ValueError(
                        f"⚠️ La reserva #{id_reserva} ya está asignada al torneo #{torneo_existente}. "
                        f"Una reserva solo puede pertenecer a UN torneo."
                    )
                # Si ya está en el mismo torneo, es un duplicado
                else:
                    raise ValueError(
                        f"⚠️ La reserva #{id_reserva} ya está asignada a este torneo."
                    )
            
            conexion.execute(
                "INSERT INTO torneo_reserva (id_torneo, id_reserva) VALUES (?, ?)",
                (id_torneo, id_reserva),
            )
            conexion.commit()
            print(f"✅ Relación guardada en BD: torneo_reserva({id_torneo}, {id_reserva})")
        except ValueError as e:
            # Re-lanzar errores de validación sin envolver
            raise
        except sqlite3.IntegrityError:
            print(f"⚠️ La reserva {id_reserva} ya está asignada al torneo {id_torneo}")
            raise ValueError(f"⚠️ Error de integridad: la reserva ya está asignada.")
        except sqlite3.Error as e:
            print(f"❌ Error al asignar reserva a torneo: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def desasignar_reserva(id_torneo, id_reserva):
        """ELIMINA una relación de tabla intermedia torneo_reserva."""
        conexion = obtener_conexion()
        try:
            conexion.execute(
                "DELETE FROM torneo_reserva WHERE id_torneo = ? AND id_reserva = ?",
                (id_torneo, id_reserva),
            )
            conexion.commit()
            print(f"✅ Relación eliminada de BD: torneo_reserva({id_torneo}, {id_reserva})")
        except sqlite3.Error as e:
            print(f"❌ Error al desasignar reserva: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def obtener_reservas_de_torneo(id_torneo):
        """CONSULTA tabla intermedia + detalles de reservas.
        
        Hace JOIN de 5 tablas para obtener TODO.
        Resultado: Lista de objetos Reserva completos.
        
        NOTA: No hardcodea nada. Consulta dinámicamente la BD.
        """
        from models.reserva import Reserva
        from models.cliente import Cliente
        from models.cancha import Cancha
        from models.horario import Horario
        
        conexion = obtener_conexion()
        reservas = []
        try:
            cursor = conexion.execute(
                """
                SELECT r.id_reserva, r.fecha, r.monto, r.estado,
                       c.id_cliente, c.nombre, c.apellido, c.telefono, c.email,
                       ca.id_cancha, ca.nombre, ca.precio_hora,
                       h.id_horario, h.hora_inicio, h.hora_fin
                FROM reserva r
                INNER JOIN torneo_reserva tr ON r.id_reserva = tr.id_reserva
                INNER JOIN cliente c ON r.id_cliente = c.id_cliente
                INNER JOIN cancha ca ON r.id_cancha = ca.id_cancha
                INNER JOIN horario h ON r.id_horario = h.id_horario
                WHERE tr.id_torneo = ?
                ORDER BY r.fecha
                """,
                (id_torneo,),
            )
            
            for fila in cursor.fetchall():
                cliente = Cliente(fila[4], fila[5], fila[6], fila[7], fila[8])
                cancha = Cancha(fila[9], fila[10], None, None, fila[11])
                horario = Horario(fila[12], fila[13], fila[14])
                reserva = Reserva(fila[0], fila[1], cliente, cancha, horario, fila[2], fila[3])
                reservas.append(reserva)
            
            print(f"✅ Consultadas {len(reservas)} reservas del torneo {id_torneo} desde BD")
        except sqlite3.Error as e:
            print(f"❌ Error al obtener reservas del torneo: {e}")
        finally:
            conexion.close()
        return reservas

    @staticmethod
    def eliminar(id_torneo):
        """Elimina un torneo.
        
        VALIDACIÓN:
        - No se puede eliminar un torneo que tenga reservas asignadas.
        - Primero debe desasignar todas las reservas.
        
        Si cumple validación:
        CASCADE automáticamente elimina:
        - Filas de torneo_reserva (tabla intermedia)
        - NO toca reservas (solo elimina la relación)
        """
        from dao.conexion import resetear_autoincrement
        
        conexion = obtener_conexion()
        try:
            # Validar que el torneo no tenga reservas asignadas
            cursor = conexion.cursor()
            cursor.execute(
                "SELECT COUNT(*) FROM torneo_reserva WHERE id_torneo = ?",
                (id_torneo,)
            )
            cantidad_reservas = cursor.fetchone()[0]
            
            if cantidad_reservas > 0:
                raise ValueError(
                    f"⚠️ No se puede eliminar el torneo #{id_torneo} porque tiene {cantidad_reservas} reserva(s) asignada(s). "
                    f"Primero debes desasignar todas las reservas."
                )
            
            # Primero eliminar las asociaciones en torneo_reserva (por claridad, aunque CASCADE lo haría)
            conexion.execute("DELETE FROM torneo_reserva WHERE id_torneo = ?", (id_torneo,))
            # Luego eliminar el torneo
            conexion.execute("DELETE FROM torneo WHERE id_torneo = ?", (id_torneo,))
            conexion.commit()
            print(f"✅ Torneo {id_torneo} eliminado + sus relaciones (CASCADE)")
            resetear_autoincrement("torneo")
        except ValueError as e:
            # Re-lanzar errores de validación sin envolver
            raise
        except sqlite3.Error as e:
            print(f"❌ Error al eliminar torneo: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()
