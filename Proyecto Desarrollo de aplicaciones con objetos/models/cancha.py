class Cancha:
    """Representa una cancha deportiva disponible para reserva."""

    def __init__(self, id_cancha=None, nombre="", tipo_superficie="", iluminacion=False,
                 precio_hora=0.0, servicios=""):
        self.id_cancha = id_cancha
        self.nombre = nombre
        self.tipo_superficie = tipo_superficie
        self.iluminacion = iluminacion
        self.precio_hora = precio_hora
        self.servicios = servicios

    def __str__(self):
        iluminacion_txt = "Sí" if self.iluminacion else "No"
        return f"{self.nombre} ({self.tipo_superficie}, Iluminación: {iluminacion_txt})"

    def calcular_costo(self, horas):
        """Devuelve el costo total según cantidad de horas."""
        return self.precio_hora * horas
