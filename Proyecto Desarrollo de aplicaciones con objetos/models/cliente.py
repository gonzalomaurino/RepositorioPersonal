class Cliente:
    """Representa a un cliente que puede realizar reservas."""

    def __init__(self, id_cliente=None, nombre="", apellido="", telefono="", email=""):
        self.id_cliente = id_cliente
        self.nombre = nombre
        self.apellido = apellido
        self.telefono = telefono
        self.email = email

    def __str__(self):
        return f"{self.nombre} {self.apellido} ({self.email})"

    def es_valido(self):
        """Valida que tenga nombre y email (reglas básicas de negocio)."""
        return bool(self.nombre and self.apellido and self.email)
