class Servicio:
    """Representa un servicio que puede estar asociado a una cancha."""

    def __init__(self, id_servicio=None, nombre="", costo=0.0):
        self.id_servicio = id_servicio
        self.nombre = nombre
        self.costo = costo

    def __str__(self):
        return f"{self.nombre} (${self.costo})"

    def es_valido(self):
        """Valida que tenga nombre y costo válido."""
        return bool(self.nombre and self.costo >= 0)
