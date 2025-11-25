from datetime import datetime

class Pago:
    """Representa el pago de una reserva."""

    def __init__(self, id_pago=None, fecha_pago=None, monto=0.0, metodo="", reserva=None):
        self.id_pago = id_pago
        self.fecha_pago = fecha_pago or datetime.now().strftime("%Y-%m-%d")
        self.monto = monto
        self.metodo = metodo
        self.reserva = reserva

    def __str__(self):
        return f"Pago #{self.id_pago} - {self.metodo} - ${self.monto}"
