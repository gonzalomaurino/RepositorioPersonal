import sqlite3
from dao.conexion import obtener_conexion
from models.reserva import Reserva
from models.cliente import Cliente
from models.cancha import Cancha
from models.horario import Horario


class ReservaDAO:
    """DAO para la entidad Reserva."""

    @staticmethod
    def crear(reserva: Reserva):
        """Inserta una nueva reserva en la BD."""
        conexion = obtener_conexion()
        try:
            conexion.execute(
                """
                INSERT INTO reserva (fecha, id_cliente, id_cancha, id_horario, monto, estado)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                (
                    reserva.fecha,
                    reserva.cliente.id_cliente,
                    reserva.cancha.id_cancha,
                    reserva.horario.id_horario,
                    reserva.monto,
                    reserva.estado,
                ),
            )
            conexion.commit()
            print(f"✅ Reserva insertada correctamente.")
        except sqlite3.IntegrityError as e:
            print(f"⚠️ Error de integridad al crear reserva: {e}")
            raise ValueError("Ya existe una reserva en ese horario y cancha.")
        except sqlite3.Error as e:
            print(f"❌ Error al crear reserva: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def obtener_todas():
        """Retorna todas las reservas con sus objetos relacionados."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT r.id_reserva, r.fecha, r.id_cliente, r.id_cancha, r.id_horario, 
                       r.monto, r.estado,
                       c.id_cliente, c.nombre, c.apellido, c.telefono, c.email,
                       ca.id_cancha, ca.nombre, ca.precio_hora,
                       h.id_horario, h.hora_inicio, h.hora_fin
                FROM reserva r
                LEFT JOIN cliente c ON r.id_cliente = c.id_cliente
                LEFT JOIN cancha ca ON r.id_cancha = ca.id_cancha
                LEFT JOIN horario h ON r.id_horario = h.id_horario
                ORDER BY r.id_reserva DESC
            """)
            
            reservas = []
            for fila in cursor.fetchall():
                cliente = Cliente(
                    id_cliente=fila[7],
                    nombre=fila[8],
                    apellido=fila[9],
                    telefono=fila[10],
                    email=fila[11]
                )
                cancha = Cancha(
                    id_cancha=fila[12],
                    nombre=fila[13],
                    precio_hora=fila[14]
                )
                horario = Horario(
                    id_horario=fila[15],
                    hora_inicio=fila[16],
                    hora_fin=fila[17]
                )
                reserva = Reserva(
                    id_reserva=fila[0],
                    fecha=fila[1],
                    cliente=cliente,
                    cancha=cancha,
                    horario=horario,
                    monto=fila[5],
                    estado=fila[6]
                )
                reservas.append(reserva)
            
            return reservas
        except sqlite3.Error as e:
            print(f"❌ Error al obtener reservas: {e}")
            return []
        finally:
            conexion.close()

    @staticmethod
    def obtener_por_id(id_reserva):
        """Obtiene una reserva específica por ID."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT r.id_reserva, r.fecha, r.id_cliente, r.id_cancha, r.id_horario, 
                       r.monto, r.estado,
                       c.id_cliente, c.nombre, c.apellido, c.telefono, c.email,
                       ca.id_cancha, ca.nombre, ca.precio_hora,
                       h.id_horario, h.hora_inicio, h.hora_fin
                FROM reserva r
                LEFT JOIN cliente c ON r.id_cliente = c.id_cliente
                LEFT JOIN cancha ca ON r.id_cancha = ca.id_cancha
                LEFT JOIN horario h ON r.id_horario = h.id_horario
                WHERE r.id_reserva = ?
            """, (id_reserva,))
            
            fila = cursor.fetchone()
            if fila:
                cliente = Cliente(
                    id_cliente=fila[7],
                    nombre=fila[8],
                    apellido=fila[9],
                    telefono=fila[10],
                    email=fila[11]
                )
                cancha = Cancha(
                    id_cancha=fila[12],
                    nombre=fila[13],
                    precio_hora=fila[14]
                )
                horario = Horario(
                    id_horario=fila[15],
                    hora_inicio=fila[16],
                    hora_fin=fila[17]
                )
                return Reserva(
                    id_reserva=fila[0],
                    fecha=fila[1],
                    cliente=cliente,
                    cancha=cancha,
                    horario=horario,
                    monto=fila[5],
                    estado=fila[6]
                )
            return None
        except sqlite3.Error as e:
            print(f"❌ Error al obtener reserva: {e}")
            return None
        finally:
            conexion.close()

    @staticmethod
    def actualizar(reserva: Reserva):
        """Actualiza una reserva existente."""
        conexion = obtener_conexion()
        try:
            conexion.execute(
                """
                UPDATE reserva 
                SET fecha = ?, id_cliente = ?, id_cancha = ?, id_horario = ?, monto = ?, estado = ?
                WHERE id_reserva = ?
                """,
                (
                    reserva.fecha,
                    reserva.cliente.id_cliente,
                    reserva.cancha.id_cancha,
                    reserva.horario.id_horario,
                    reserva.monto,
                    reserva.estado,
                    reserva.id_reserva,
                ),
            )
            conexion.commit()
            print(f"✅ Reserva actualizada correctamente.")
        except sqlite3.Error as e:
            print(f"❌ Error al actualizar reserva: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def eliminar(id_reserva):
        """Elimina una reserva por ID y resetea el autoincrement.
        
        NO se puede eliminar una reserva CONFIRMADA que tenga pagos completados.
        Esto protege la integridad de los registros financieros.
        """
        from dao.conexion import resetear_autoincrement
        
        conexion = obtener_conexion()
        try:
            # 1. Verificar el estado de la reserva
            cursor = conexion.cursor()
            cursor.execute("SELECT estado FROM reserva WHERE id_reserva = ?", (id_reserva,))
            fila = cursor.fetchone()
            
            if not fila:
                raise ValueError(f"No existe la reserva con ID {id_reserva}")
            
            estado = fila[0]
            
            # 2. Verificar si tiene pagos
            cursor.execute(
                "SELECT COUNT(*) FROM pago WHERE id_reserva = ?",
                (id_reserva,)
            )
            cantidad_pagos = cursor.fetchone()[0]
            
            # 3. Si tiene pagos (confirmada o seña), no se puede eliminar
            if cantidad_pagos > 0:
                if estado and estado.lower() == "confirmada":
                    raise ValueError(
                        f"⚠️ No se puede eliminar una reserva CONFIRMADA con pagos registrados. "
                        f"Primero debes cambiar el estado a 'Cancelada'."
                    )
                elif estado and estado.lower() == "seña":
                    raise ValueError(
                        f"⚠️ No se puede eliminar una reserva en SEÑA con pagos registrados. "
                        f"Primero debes cambiar el estado a 'Cancelada'."
                    )
            
            # 4. Si es seguro eliminar (pendiente o cancelada sin pagos), proceder
            # Primero eliminar asociaciones en torneo_reserva si existen
            conexion.execute("DELETE FROM torneo_reserva WHERE id_reserva = ?", (id_reserva,))
            
            # Luego eliminar pagos asociados (ya tiene CASCADE pero por si acaso)
            conexion.execute("DELETE FROM pago WHERE id_reserva = ?", (id_reserva,))
            
            # Finalmente eliminar la reserva
            conexion.execute("DELETE FROM reserva WHERE id_reserva = ?", (id_reserva,))
            conexion.commit()
            print(f"✅ Reserva eliminada correctamente.")
            
            # Resetear el autoincrement
            resetear_autoincrement("reserva")
        except ValueError as e:
            # Re-lanzar errores de validación sin envolver
            raise
        except sqlite3.Error as e:
            print(f"❌ Error al eliminar reserva: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()
