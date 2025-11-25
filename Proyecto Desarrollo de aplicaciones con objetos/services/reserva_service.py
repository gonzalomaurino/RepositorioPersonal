from dao.reserva_dao import ReservaDAO
from dao.cancha_dao import CanchaDAO
from dao.horario_dao import HorarioDAO
from models.reserva import Reserva
from datetime import datetime, date


class ReservaService:
    """Reglas de negocio para la gestión de reservas."""

    @staticmethod
    def crear_reserva(fecha, cliente, cancha, horario, monto):
        """Crea una nueva reserva con validaciones."""
        # Validar datos mínimos
        if not (cliente and cancha and horario):
            raise ValueError("❌ Datos incompletos para crear la reserva.")
        
        if not fecha or not monto:
            raise ValueError("❌ Fecha y monto son obligatorios.")
        
        # Validar que la fecha no sea anterior a hoy
        try:
            # Convertir string de fecha a objeto date - esperamos formato AAAA-MM-DD
            if isinstance(fecha, str):
                fecha_obj = datetime.strptime(fecha, "%Y-%m-%d").date()
            else:
                fecha_obj = fecha if isinstance(fecha, date) else datetime.strptime(str(fecha), "%Y-%m-%d").date()
            
            fecha_hoy = date.today()
            if fecha_obj < fecha_hoy:
                raise ValueError("❌ No se pueden hacer reservas para fechas anteriores a hoy.")
        except ValueError as e:
            if "No se pueden hacer reservas" in str(e):
                raise
            raise ValueError("❌ Formato de fecha inválido. Use AAAA-MM-DD")
        
        try:
            monto = float(monto)
            if monto <= 0:
                raise ValueError("❌ El monto debe ser mayor a cero.")
        except ValueError:
            raise ValueError("❌ El monto debe ser un número válido.")
        
        # Crear el objeto reserva
        nueva_reserva = Reserva(
            fecha=fecha,
            cliente=cliente,
            cancha=cancha,
            horario=horario,
            monto=monto,
            estado="pendiente"
        )

        # Intentar insertarla
        ReservaDAO.crear(nueva_reserva)
        print(f"✅ Reserva creada para {cliente.nombre} en {cancha.nombre} ({fecha})")

    @staticmethod
    def validar_disponibilidad(cancha, fecha, horario):
        """Verifica si existe una reserva para la misma cancha y horario."""
        try:
            reservas = ReservaDAO.obtener_todas()
            for r in reservas:
                if (
                    r.cancha.id_cancha == cancha.id_cancha and
                    r.fecha == fecha and
                    r.horario.id_horario == horario.id_horario
                ):
                    return False
            return True
        except Exception as e:
            print(f"Error al validar disponibilidad: {e}")
            return True  # Si hay error, asumimos disponible para no bloquear

    @staticmethod
    def listar_todas():
        """Retorna todas las reservas."""
        try:
            return ReservaDAO.obtener_todas()
        except Exception as e:
            print(f"Error al listar reservas: {e}")
            return []

    @staticmethod
    def filtrar_por_estado(estado):
        """Filtra reservas por estado: pendiente, seña, confirmada, cancelada."""
        try:
            todas = ReservaDAO.obtener_todas()
            return [r for r in todas if r.estado.lower() == estado.lower()]
        except Exception as e:
            print(f"Error al filtrar por estado: {e}")
            return []

    @staticmethod
    def filtrar_por_cancha(id_cancha):
        """Filtra reservas por cancha."""
        try:
            todas = ReservaDAO.obtener_todas()
            return [r for r in todas if r.cancha.id_cancha == id_cancha]
        except Exception as e:
            print(f"Error al filtrar por cancha: {e}")
            return []

    @staticmethod
    def filtrar_por_estado_y_cancha(estado, id_cancha):
        """Filtra reservas por estado y cancha."""
        try:
            todas = ReservaDAO.obtener_todas()
            return [r for r in todas if r.estado.lower() == estado.lower() and r.cancha.id_cancha == id_cancha]
        except Exception as e:
            print(f"Error al filtrar por estado y cancha: {e}")
            return []
