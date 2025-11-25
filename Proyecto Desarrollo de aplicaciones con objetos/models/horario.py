class Horario:
    """Representa un bloque horario disponible para reservas."""

    def __init__(self, id_horario=None, hora_inicio="", hora_fin=""):
        self.id_horario = id_horario
        self.hora_inicio = hora_inicio
        self.hora_fin = hora_fin

    def __str__(self):
        return f"{self.hora_inicio} - {self.hora_fin}"
