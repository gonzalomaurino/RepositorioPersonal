from datetime import datetime

class Reserva:
    """Representa una reserva de cancha realizada por un cliente."""

    def __init__(self, id_reserva=None, fecha=None, cliente=None, cancha=None,
                 horario=None, monto=0.0, estado="pendiente"):
        self.id_reserva = id_reserva
        self.fecha = fecha or datetime.now().strftime("%Y-%m-%d")
        self.cliente = cliente
        self.cancha = cancha
        self.horario = horario
        self.monto = monto
        self.estado = estado

    def __str__(self):
        return f"Reserva #{self.id_reserva} - {self.cliente} - {self.cancha} - {self.fecha}"

    def confirmar(self):
        """Confirma la reserva."""
        self.estado = "confirmada"

    def cancelar(self):
        """Cancela la reserva."""
        self.estado = "cancelada"
