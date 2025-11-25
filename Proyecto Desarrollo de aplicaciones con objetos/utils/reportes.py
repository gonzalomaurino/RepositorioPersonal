import sqlite3
from dao.conexion import obtener_conexion

class ReporteService:
    """Consultas estadísticas sobre reservas."""

    @staticmethod
    def reservas_por_cliente():
        conexion = obtener_conexion()
        cursor = conexion.cursor()
        query = """
        SELECT c.nombre || ' ' || c.apellido AS cliente, COUNT(r.id_reserva) AS total
        FROM cliente c
        LEFT JOIN reserva r ON c.id_cliente = r.id_cliente
        GROUP BY c.id_cliente
        ORDER BY total DESC
        """
        cursor.execute(query)
        resultados = cursor.fetchall()
        conexion.close()
        return resultados

    @staticmethod
    def reservas_por_cancha():
        conexion = obtener_conexion()
        cursor = conexion.cursor()
        query = """
        SELECT ca.nombre, COUNT(r.id_reserva) AS total
        FROM cancha ca
        LEFT JOIN reserva r ON ca.id_cancha = r.id_cancha
        GROUP BY ca.id_cancha
        ORDER BY total DESC
        """
        cursor.execute(query)
        resultados = cursor.fetchall()
        conexion.close()
        return resultados

    @staticmethod
    def utilizacion_mensual():
        conexion = obtener_conexion()
        cursor = conexion.cursor()
        query = """
        SELECT 
            strftime('%Y-%m', fecha) AS mes, 
            COUNT(*) AS total_reservas
        FROM reserva
        GROUP BY mes
        ORDER BY mes
        """
        cursor.execute(query)
        resultados = cursor.fetchall()
        conexion.close()
        return resultados
