import sqlite3
from dao.conexion import obtener_conexion
from models.pago import Pago


class PagoDAO:
    """DAO para la entidad Pago."""

    @staticmethod
    def registrar(pago: Pago):
        """Inserta un nuevo pago en la BD."""
        conexion = obtener_conexion()
        try:
            conexion.execute(
                """
                INSERT INTO pago (fecha_pago, monto, metodo, id_reserva)
                VALUES (?, ?, ?, ?)
                """,
                (pago.fecha_pago, pago.monto, pago.metodo, pago.reserva.id_reserva),
            )
            conexion.commit()
            print(f"✅ Pago registrado correctamente.")
        except sqlite3.Error as e:
            print(f"❌ Error al registrar pago: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def obtener_todos():
        """Obtiene todos los pagos registrados."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT id_pago, fecha_pago, monto, metodo, id_reserva 
                FROM pago 
                ORDER BY id_pago DESC
            """)
            pagos = cursor.fetchall()
            return pagos
        except sqlite3.Error as e:
            print(f"❌ Error al obtener pagos: {e}")
            return []
        finally:
            conexion.close()

    @staticmethod
    def filtrar_por_cancha(id_cancha):
        """Obtiene pagos filtrados por cancha."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT p.id_pago, p.fecha_pago, p.monto, p.metodo, p.id_reserva
                FROM pago p
                JOIN reserva r ON p.id_reserva = r.id_reserva
                WHERE r.id_cancha = ?
                ORDER BY p.id_pago DESC
            """, (id_cancha,))
            pagos = cursor.fetchall()
            return pagos
        except sqlite3.Error as e:
            print(f"❌ Error al filtrar pagos por cancha: {e}")
            return []
        finally:
            conexion.close()

    @staticmethod
    def filtrar_por_metodo(metodo):
        """Obtiene pagos filtrados por método de pago."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT id_pago, fecha_pago, monto, metodo, id_reserva
                FROM pago
                WHERE metodo = ?
                ORDER BY id_pago DESC
            """, (metodo,))
            pagos = cursor.fetchall()
            return pagos
        except sqlite3.Error as e:
            print(f"❌ Error al filtrar pagos por método: {e}")
            return []
        finally:
            conexion.close()

    @staticmethod
    def filtrar_por_cancha_y_metodo(id_cancha, metodo):
        """Obtiene pagos filtrados por cancha y método de pago."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT p.id_pago, p.fecha_pago, p.monto, p.metodo, p.id_reserva
                FROM pago p
                JOIN reserva r ON p.id_reserva = r.id_reserva
                WHERE r.id_cancha = ? AND p.metodo = ?
                ORDER BY p.id_pago DESC
            """, (id_cancha, metodo))
            pagos = cursor.fetchall()
            return pagos
        except sqlite3.Error as e:
            print(f"❌ Error al filtrar pagos: {e}")
            return []
        finally:
            conexion.close()

    @staticmethod
    def obtener_por_id(id_pago):
        """Obtiene un pago específico por ID."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT id_pago, fecha_pago, monto, metodo, id_reserva 
                FROM pago 
                WHERE id_pago = ?
            """, (id_pago,))
            pago = cursor.fetchone()
            return pago
        except sqlite3.Error as e:
            print(f"❌ Error al obtener pago: {e}")
            return None
        finally:
            conexion.close()

    @staticmethod
    def eliminar(id_pago):
        """Elimina un pago por ID y resetea el autoincrement."""
        from dao.conexion import resetear_autoincrement
        
        conexion = obtener_conexion()
        try:
            conexion.execute("DELETE FROM pago WHERE id_pago = ?", (id_pago,))
            conexion.commit()
            print(f"✅ Pago eliminado correctamente.")
            
            # Resetear el autoincrement
            resetear_autoincrement("pago")
        except sqlite3.Error as e:
            print(f"❌ Error al eliminar pago: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()
