from dao.horario_dao import HorarioDAO
from models.horario import Horario


class HorarioService:
    """Servicio para registrar y listar horarios disponibles."""

    @staticmethod
    def registrar_horario(hora_inicio, hora_fin):
        if not (hora_inicio and hora_fin):
            raise ValueError("Ambas horas son requeridas.")
        
        nuevo = Horario(None, hora_inicio, hora_fin)
        HorarioDAO.crear(nuevo)
        print(f"🕒 Horario registrado: {hora_inicio} - {hora_fin}")

    @staticmethod
    def listar_horarios():
        return HorarioDAO.obtener_todos()

    @staticmethod
    def modificar_horario(id_horario, hora_inicio, hora_fin):
        """Modifica un horario existente."""
        if not (hora_inicio and hora_fin):
            raise ValueError("Ambas horas son requeridas.")
        
        horario_actualizado = Horario(id_horario, hora_inicio, hora_fin)
        HorarioDAO.actualizar(horario_actualizado)
        print(f"✅ Horario modificado: {hora_inicio} - {hora_fin}")

    @staticmethod
    def eliminar_horario(id_horario):
        HorarioDAO.eliminar(id_horario)
        print(f"🗑️ Horario eliminado (ID {id_horario})")
