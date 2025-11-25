class Torneo:
    """Representa un torneo compuesto por múltiples reservas.
    
    Las reservas se cargan dinámicamente desde la BD usando tabla intermedia
    torneo_reserva (relación N:M). NO son hardcodeadas.
    """

    def __init__(self, id_torneo=None, nombre="", fecha_inicio="", fecha_fin="", categoria=""):
        self.id_torneo = id_torneo
        self.nombre = nombre
        self.fecha_inicio = fecha_inicio
        self.fecha_fin = fecha_fin
        self.categoria = categoria
        self.reservas = []  # Se llena con TorneoDAO.obtener_reservas_de_torneo()

    def agregar_reserva_memoria(self, reserva):
        """Agrega una reserva a la lista EN MEMORIA (para UI, no persiste).
        
        ⚠️ NOTA: Para PERSISTIR en BD, usar TorneoService.asignar_reserva_a_torneo()
        """
        self.reservas.append(reserva)

    def cargar_reservas_desde_bd(self, reservas):
        """Carga dinámicamente reservas desde la BD.
        
        Esto es lo correcto: las reservas vienen de la tabla intermedia,
        no son hardcodeadas en código.
        """
        self.reservas = reservas

    def __str__(self):
        return f"Torneo {self.nombre} ({self.categoria}) - {len(self.reservas)} reservas"
