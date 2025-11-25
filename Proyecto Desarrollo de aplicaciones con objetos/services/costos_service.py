"""
Servicio de negocio para reportes de costos y presupuestos.
"""
from dao.costos_dao import CostosDAO


class CostosService:
    """Servicio para la gestión de reportes de costos y presupuestos."""

    @staticmethod
    def obtener_ingresos_por_cancha():
        """Retorna los ingresos por cancha."""
        try:
            return CostosDAO.ingresos_por_cancha()
        except Exception as e:
            print(f"❌ Error: {e}")
            return []

    @staticmethod
    def obtener_pagos_por_metodo():
        """Retorna los pagos agrupados por método."""
        try:
            return CostosDAO.pagos_por_metodo()
        except Exception as e:
            print(f"❌ Error: {e}")
            return []

    @staticmethod
    def obtener_estado_pagos():
        """Retorna el estado general de pagos e ingresos."""
        try:
            return CostosDAO.estado_pagos()
        except Exception as e:
            print(f"❌ Error: {e}")
            return None

    @staticmethod
    def obtener_presupuesto_mensual():
        """Retorna el presupuesto e ingresos reales mensualmente."""
        try:
            return CostosDAO.presupuesto_mensual()
        except Exception as e:
            print(f"❌ Error: {e}")
            return []

    @staticmethod
    def obtener_ingresos_por_cliente():
        """Retorna los top clientes por inversión."""
        try:
            return CostosDAO.ingresos_por_cliente_top()
        except Exception as e:
            print(f"❌ Error: {e}")
            return []

    @staticmethod
    def obtener_proyeccion():
        """Retorna la proyección de presupuesto."""
        try:
            return CostosDAO.proyeccion_presupuesto()
        except Exception as e:
            print(f"❌ Error: {e}")
            return None

    @staticmethod
    def obtener_resumen_cobranza():
        """Retorna el resumen de cobranza por período."""
        try:
            return CostosDAO.resumen_cobranza()
        except Exception as e:
            print(f"❌ Error: {e}")
            return []
