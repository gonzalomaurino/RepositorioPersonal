"""
DAO para obtener datos de costos, pagos e ingresos desde la base de datos.
"""
import sqlite3
from dao.conexion import obtener_conexion


class CostosDAO:
    """DAO para consultas de costos, presupuestos e ingresos."""

    @staticmethod
    def ingresos_por_cancha():
        """Obtiene los ingresos totales por cancha (suma de montos de reservas confirmadas)."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT ca.nombre, SUM(r.monto) AS ingresos_total, COUNT(r.id_reserva) AS total_reservas
                FROM cancha ca
                LEFT JOIN reserva r ON ca.id_cancha = r.id_cancha
                WHERE r.estado = 'confirmada'
                GROUP BY ca.id_cancha
                ORDER BY ingresos_total DESC
            """)
            resultados = cursor.fetchall()
            return resultados
        except sqlite3.Error as e:
            print(f"❌ Error al obtener ingresos por cancha: {e}")
            return []
        finally:
            conexion.close()

    @staticmethod
    def pagos_por_metodo():
        """Obtiene el total de pagos agrupados por método."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT metodo, COUNT(*) AS cantidad, SUM(monto) AS total
                FROM pago
                GROUP BY metodo
                ORDER BY total DESC
            """)
            resultados = cursor.fetchall()
            return resultados
        except sqlite3.Error as e:
            print(f"❌ Error al obtener pagos por método: {e}")
            return []
        finally:
            conexion.close()

    @staticmethod
    def estado_pagos():
        """Obtiene el resumen de pagos vs ingresos generados."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            
            # Ingresos totales (suma de montos de reservas confirmadas)
            cursor.execute("""
                SELECT COALESCE(SUM(monto), 0) FROM reserva WHERE estado = 'confirmada'
            """)
            ingresos_totales = cursor.fetchone()[0]
            
            # Pagos totales realizados
            cursor.execute("""
                SELECT COALESCE(SUM(monto), 0) FROM pago
            """)
            pagos_totales = cursor.fetchone()[0]
            
            # Reservas pendientes (ingresos pendientes)
            cursor.execute("""
                SELECT COALESCE(SUM(monto), 0) FROM reserva WHERE estado = 'pendiente'
            """)
            pendientes = cursor.fetchone()[0]
            
            # Reservas canceladas (ingresos perdidos)
            cursor.execute("""
                SELECT COALESCE(SUM(monto), 0) FROM reserva WHERE estado = 'cancelada'
            """)
            canceladas = cursor.fetchone()[0]
            
            return {
                'ingresos_confirmados': ingresos_totales,
                'pagos_realizados': pagos_totales,
                'pendientes': pendientes,
                'canceladas': canceladas,
                'balance': ingresos_totales - pagos_totales
            }
        except sqlite3.Error as e:
            print(f"❌ Error al obtener estado de pagos: {e}")
            return None
        finally:
            conexion.close()

    @staticmethod
    def presupuesto_mensual():
        """Obtiene el presupuesto e ingresos reales mensualmente."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT 
                    strftime('%Y-%m', r.fecha) AS mes,
                    COUNT(CASE WHEN r.estado = 'confirmada' THEN 1 END) AS reservas_confirmadas,
                    COALESCE(SUM(CASE WHEN r.estado = 'confirmada' THEN r.monto ELSE 0 END), 0) AS presupuesto,
                    COALESCE(SUM(CASE WHEN p.id_pago IS NOT NULL THEN p.monto ELSE 0 END), 0) AS pagos_realizados
                FROM reserva r
                LEFT JOIN pago p ON r.id_reserva = p.id_reserva
                GROUP BY mes
                ORDER BY mes DESC
            """)
            resultados = cursor.fetchall()
            return resultados
        except sqlite3.Error as e:
            print(f"❌ Error al obtener presupuesto mensual: {e}")
            return []
        finally:
            conexion.close()

    @staticmethod
    def ingresos_por_cliente_top():
        """Obtiene los top 10 clientes con mayor inversión en reservas."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT 
                    c.nombre || ' ' || c.apellido AS cliente,
                    COUNT(r.id_reserva) AS total_reservas,
                    COALESCE(SUM(r.monto), 0) AS total_invertido,
                    COALESCE(SUM(p.monto), 0) AS pagos_realizados
                FROM cliente c
                LEFT JOIN reserva r ON c.id_cliente = r.id_cliente
                LEFT JOIN pago p ON r.id_reserva = p.id_reserva
                GROUP BY c.id_cliente
                ORDER BY total_invertido DESC
                LIMIT 10
            """)
            resultados = cursor.fetchall()
            return resultados
        except sqlite3.Error as e:
            print(f"❌ Error al obtener ingresos por cliente: {e}")
            return []
        finally:
            conexion.close()

    @staticmethod
    def proyeccion_presupuesto():
        """Genera una proyección de presupuesto basada en datos históricos."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            
            # Promedio de ingresos mensuales
            cursor.execute("""
                SELECT 
                    AVG(monto_mensual) as promedio,
                    MAX(monto_mensual) as maximo,
                    MIN(monto_mensual) as minimo
                FROM (
                    SELECT 
                        strftime('%Y-%m', fecha) AS mes,
                        SUM(monto) AS monto_mensual
                    FROM reserva
                    WHERE estado = 'confirmada'
                    GROUP BY mes
                )
            """)
            resultado = cursor.fetchone()
            
            if resultado[0]:  # Si hay datos
                return {
                    'promedio_mensual': resultado[0],
                    'maximo_registrado': resultado[1],
                    'minimo_registrado': resultado[2],
                    'proyeccion_trimestral': resultado[0] * 3 if resultado[0] else 0
                }
            return None
        except sqlite3.Error as e:
            print(f"❌ Error al obtener proyección: {e}")
            return None
        finally:
            conexion.close()

    @staticmethod
    def resumen_cobranza():
        """Obtiene el resumen de cobranza por período."""
        conexion = obtener_conexion()
        try:
            cursor = conexion.cursor()
            cursor.execute("""
                SELECT 
                    strftime('%Y-%m', p.fecha_pago) AS periodo,
                    COUNT(*) AS cantidad_pagos,
                    SUM(p.monto) AS total_cobrado
                FROM pago p
                GROUP BY periodo
                ORDER BY periodo DESC
            """)
            resultados = cursor.fetchall()
            return resultados
        except sqlite3.Error as e:
            print(f"❌ Error al obtener resumen de cobranza: {e}")
            return []
        finally:
            conexion.close()
