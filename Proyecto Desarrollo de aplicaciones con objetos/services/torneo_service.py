from dao.torneo_dao import TorneoDAO
from models.torneo import Torneo


class TorneoService:
    """Servicio para la gestión de torneos.
    
    IMPORTANTE: Las reservas se guardan en tabla intermedia torneo_reserva,
    NO se hardcodean. Cada relación es un registro en BD.
    """

    @staticmethod
    def crear_torneo(nombre, fecha_inicio, fecha_fin, categoria):
        """Crea un nuevo torneo (sin reservas inicialmente)."""
        if not nombre:
            raise ValueError("El torneo debe tener nombre.")
        
        torneo = Torneo(None, nombre, fecha_inicio, fecha_fin, categoria)
        id_torneo = TorneoDAO.crear(torneo)
        print(f"🏆 Torneo creado: {nombre} ({categoria})")
        return id_torneo

    @staticmethod
    def listar_torneos():
        """Lista todos los torneos (sin cargar reservas aún)."""
        return TorneoDAO.obtener_todos()

    @staticmethod
    def asignar_reserva_a_torneo(id_torneo, id_reserva):
        """Asigna una reserva a un torneo (PERSISTE en tabla intermedia).
        
        VALIDACIONES:
        - Una reserva solo puede estar en UN torneo.
        - Si la reserva ya está en otro torneo, lanza un ValueError.
        
        Esto inserta una fila en torneo_reserva (id_torneo, id_reserva).
        NO es hardcode, es un registro en la base de datos.
        """
        try:
            TorneoDAO.asignar_reserva(id_torneo, id_reserva)
            print(f"✅ Reserva #{id_reserva} asignada al torneo {id_torneo} (guardada en BD)")
            return True
        except ValueError as e:
            # Re-lanzar errores de validación
            print(f"❌ Validación: {e}")
            raise
        except Exception as e:
            print(f"❌ Error al asignar reserva: {e}")
            raise

    @staticmethod
    def desasignar_reserva_de_torneo(id_torneo, id_reserva):
        """Desasocia una reserva de un torneo (elimina de tabla intermedia)."""
        try:
            TorneoDAO.desasignar_reserva(id_torneo, id_reserva)
            print(f"✅ Reserva #{id_reserva} desasignada del torneo {id_torneo}")
            return True
        except Exception as e:
            print(f"❌ Error al desasignar reserva: {e}")
            raise

    @staticmethod
    def obtener_reservas_torneo(id_torneo):
        """Obtiene TODAS las reservas de un torneo (consulta tabla intermedia).
        
        Retorna: Lista de objetos Reserva cargados desde la BD.
        ESCALA a N reservas sin cambiar código.
        """
        try:
            reservas = TorneoDAO.obtener_reservas_de_torneo(id_torneo)
            print(f"📋 Torneo {id_torneo} tiene {len(reservas)} reservas")
            return reservas
        except Exception as e:
            print(f"❌ Error al obtener reservas del torneo: {e}")
            return []

    @staticmethod
    def eliminar_torneo(id_torneo):
        """Elimina un torneo (CASCADE elimina sus relaciones automáticamente).
        
        VALIDACIÓN:
        - No se puede eliminar un torneo con reservas asignadas.
        - Lanza ValueError si el torneo tiene reservas.
        """
        try:
            TorneoDAO.eliminar(id_torneo)
            print(f"🗑️ Torneo eliminado (ID {id_torneo}) + sus relaciones (CASCADE)")
            return True
        except ValueError as e:
            # Re-lanzar errores de validación
            print(f"❌ Validación: {e}")
            raise
        except Exception as e:
            print(f"❌ Error al eliminar torneo: {e}")
            raise
