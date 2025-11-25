import flet as ft
from datetime import date
from services.reserva_service import ReservaService
from services.cliente_service import ClienteService
from services.cancha_service import CanchaService
from services.horario_service import HorarioService
from models.reserva import Reserva


class ReservasView(ft.Column):
    """Vista de gestión de reservas deportivas."""

    def __init__(self, page: ft.Page):
        super().__init__(expand=True, scroll="adaptive", spacing=15)
        self.page = page
        self.crear_ui()

    def crear_ui(self):
        # --- Combos dinámicos ---
        self.cmb_cliente = ft.Dropdown(label="Cliente", width=200)
        self.cmb_cancha = ft.Dropdown(label="Cancha", width=200, on_change=self.al_cambiar_cancha)
        self.cmb_horario = ft.Dropdown(label="Horario", width=200)

        # --- Fecha y monto ---
        self.fecha = ft.TextField(label="Fecha (AAAA-MM-DD)", width=180, value=str(date.today()))
        self.monto = ft.TextField(label="Monto ($)", width=120, read_only=True)

        # --- Botón registrar ---
        btn_guardar = ft.ElevatedButton(
            text="Registrar reserva",
            icon=ft.Icons.SAVE,
            on_click=self.registrar_reserva,
        )

        # --- Filtros de búsqueda ---
        self.cmb_filtro_estado = ft.Dropdown(
            label="Filtrar por estado",
            width=180,
            options=[
                ft.dropdown.Option("Todos"),
                ft.dropdown.Option("Pendiente"),
                ft.dropdown.Option("Seña"),
                ft.dropdown.Option("Confirmada"),
                ft.dropdown.Option("Cancelada"),
            ],
            value="Todos",
            on_change=self.aplicar_filtros
        )

        self.cmb_filtro_cancha = ft.Dropdown(
            label="Filtrar por cancha",
            width=180,
            value="Todas",
            on_change=self.aplicar_filtros
        )

        btn_limpiar_filtros = ft.IconButton(
            icon=ft.Icons.CLEAR,
            tooltip="Limpiar filtros",
            on_click=self.limpiar_filtros
        )

        # --- Tabla de reservas ---
        self.tabla = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("ID")),
                ft.DataColumn(ft.Text("Cliente")),
                ft.DataColumn(ft.Text("Cancha")),
                ft.DataColumn(ft.Text("Horario")),
                ft.DataColumn(ft.Text("Fecha")),
                ft.DataColumn(ft.Text("Monto")),
                ft.DataColumn(ft.Text("Estado")),
                ft.DataColumn(ft.Text("Acciones")),
            ],
            rows=[],
        )

        # --- Cargar datos iniciales ---
        self.cargar_combobox()
        self.refrescar_tabla()

        # --- Layout visual ---
        self.controls = [
            ft.Text("Gestión de Reservas", size=20, weight=ft.FontWeight.BOLD),
            ft.Row([self.cmb_cliente, self.cmb_cancha, self.cmb_horario, self.fecha, self.monto, btn_guardar],
                   wrap=True, spacing=10),
            ft.Divider(),
            ft.Text("Filtros de búsqueda", size=14, weight=ft.FontWeight.BOLD),
            ft.Row([self.cmb_filtro_estado, self.cmb_filtro_cancha, btn_limpiar_filtros],
                   wrap=True, spacing=10),
            ft.Divider(),
            self.tabla,
        ]

    # =======================================================
    # FUNCIONES DE NEGOCIO
    # =======================================================
    
    def mostrar_error(self, mensaje):
        """Muestra un mensaje de error con SnackBar."""
        snack = ft.SnackBar(
            content=ft.Text(mensaje, color=ft.Colors.WHITE, size=14),
            bgcolor=ft.Colors.RED_700,
            duration=4000
        )
        self.page.overlay.append(snack)
        snack.open = True
        self.page.update()
    
    def mostrar_exito(self, mensaje):
        """Muestra un mensaje de éxito con SnackBar."""
        snack = ft.SnackBar(
            content=ft.Text(mensaje, color=ft.Colors.WHITE, size=14),
            bgcolor=ft.Colors.GREEN_700,
            duration=3000
        )
        self.page.overlay.append(snack)
        snack.open = True
        self.page.update()
    
    def mostrar_advertencia(self, mensaje):
        """Muestra un mensaje de advertencia con SnackBar."""
        snack = ft.SnackBar(
            content=ft.Text(mensaje, color=ft.Colors.WHITE, size=14),
            bgcolor=ft.Colors.ORANGE_700,
            duration=5000
        )
        self.page.overlay.append(snack)
        snack.open = True
        self.page.update()

    def cargar_combobox(self):
        """Carga clientes, canchas y horarios desde los servicios."""
        clientes = sorted(ClienteService.listar_clientes(), key=lambda c: c.id_cliente)
        self.cmb_cliente.options = [
            ft.dropdown.Option(f"{c.id_cliente}-{c.nombre} {c.apellido}") for c in clientes
        ]
        
        canchas = sorted(CanchaService.listar_canchas(), key=lambda c: c.id_cancha)
        self.cmb_cancha.options = [
            ft.dropdown.Option(f"{c.id_cancha}-{c.nombre}") for c in canchas
        ]

        # Cargar opciones para el filtro de canchas
        self.cmb_filtro_cancha.options = [ft.dropdown.Option("Todas")] + [
            ft.dropdown.Option(f"{c.id_cancha}-{c.nombre}") for c in canchas
        ]
        
        horarios = sorted(HorarioService.listar_horarios(), key=lambda h: h.id_horario)
        self.cmb_horario.options = [
            ft.dropdown.Option(f"{h.id_horario}-{h.hora_inicio}-{h.hora_fin}") for h in horarios
        ]
        self.page.update()

    def al_cambiar_cancha(self, e):
        """Se ejecuta cuando cambia la cancha seleccionada y carga el monto dinámicamente."""
        if not self.cmb_cancha.value:
            self.monto.value = ""
            self.page.update()
            return
        
        try:
            id_cancha = int(self.cmb_cancha.value.split("-")[0])
            cancha = [c for c in CanchaService.listar_canchas() if c.id_cancha == id_cancha][0]
            self.monto.value = str(cancha.precio_hora)
            self.page.update()
        except Exception as err:
            print(f"Error al cambiar cancha: {err}")

    def registrar_reserva(self, e):
        """Registra una nueva reserva si hay disponibilidad."""
        try:
            # Validar que haya selecciones
            if not self.cmb_cliente.value:
                self.mostrar_error("❌ Debe seleccionar un cliente")
                return
            if not self.cmb_cancha.value:
                self.mostrar_error("❌ Debe seleccionar una cancha")
                return
            if not self.cmb_horario.value:
                self.mostrar_error("❌ Debe seleccionar un horario")
                return
            
            # --- Parseo de selección ---
            id_cliente = int(self.cmb_cliente.value.split("-")[0])
            id_cancha = int(self.cmb_cancha.value.split("-")[0])
            id_horario = int(self.cmb_horario.value.split("-")[0])
            fecha = self.fecha.value
            monto = float(self.monto.value)

            # --- Construcción de objetos base ---
            cliente = [c for c in ClienteService.listar_clientes() if c.id_cliente == id_cliente][0]
            cancha = [c for c in CanchaService.listar_canchas() if c.id_cancha == id_cancha][0]
            horario = [h for h in HorarioService.listar_horarios() if h.id_horario == id_horario][0]

            # --- Validación de disponibilidad ---
            disponible = ReservaService.validar_disponibilidad(cancha, fecha, horario)
            if not disponible:
                self.mostrar_advertencia("⚠️ Ya existe una reserva para esa cancha y horario")
                return

            # --- Creación de reserva ---
            ReservaService.crear_reserva(fecha, cliente, cancha, horario, monto)

            self.mostrar_exito("✅ Reserva registrada correctamente")
            
            # Limpiar campos
            self.cmb_cliente.value = None
            self.cmb_cancha.value = None
            self.cmb_horario.value = None
            self.monto.value = ""
            self.fecha.value = str(date.today())
            
            self.refrescar_tabla()

        except ValueError as err:
            self.mostrar_error(str(err))
        except Exception as err:
            self.mostrar_error(f"❌ Error: {err}")
            print(f"Error en registrar_reserva: {err}")  # Log para debugging

    def refrescar_tabla(self):
        """Muestra todas las reservas existentes con opción de eliminar."""
        from dao.reserva_dao import ReservaDAO
        self.tabla.rows.clear()
        try:
            conexion = ReservaDAO.obtener_todas()
        except Exception:
            return
        reservas = conexion
        for r in reservas:
            self.tabla.rows.append(
                ft.DataRow(
                    cells=[
                        ft.DataCell(ft.Text(str(r.id_reserva))),
                        ft.DataCell(ft.Text(r.cliente.nombre)),
                        ft.DataCell(ft.Text(r.cancha.nombre)),
                        ft.DataCell(ft.Text(r.horario.hora_inicio + "-" + r.horario.hora_fin)),
                        ft.DataCell(ft.Text(r.fecha)),
                        ft.DataCell(ft.Text(f"${r.monto:.2f}")),
                        ft.DataCell(ft.Text(r.estado)),
                        ft.DataCell(
                            ft.Row([
                                ft.IconButton(
                                    icon=ft.Icons.CANCEL,
                                    tooltip="Cancelar reserva",
                                    icon_color=ft.Colors.ORANGE,
                                    on_click=lambda e, id_reserva=r.id_reserva: self.cancelar_reserva(id_reserva),
                                ),
                                ft.IconButton(
                                    icon=ft.Icons.DELETE,
                                    tooltip="Eliminar reserva",
                                    icon_color=ft.Colors.RED,
                                    on_click=lambda e, id_reserva=r.id_reserva: self.eliminar_reserva(id_reserva),
                                ),
                            ], spacing=0)
                        ),
                    ]
                )
            )
        self.page.update()

    def aplicar_filtros(self, e):
        """Aplica los filtros de estado y/o cancha a la tabla."""
        self.tabla.rows.clear()
        try:
            estado_filtro = self.cmb_filtro_estado.value if self.cmb_filtro_estado.value else "Todos"
            cancha_filtro = self.cmb_filtro_cancha.value if self.cmb_filtro_cancha.value else "Todas"
            
            # Obtener todas las reservas
            reservas = ReservaService.listar_todas()
            
            # Filtrar por estado
            if estado_filtro != "Todos":
                reservas = [r for r in reservas if r.estado.lower() == estado_filtro.lower()]
            
            # Filtrar por cancha
            if cancha_filtro != "Todas":
                id_cancha = int(cancha_filtro.split("-")[0])
                reservas = [r for r in reservas if r.cancha.id_cancha == id_cancha]
            
            # Mostrar reservas filtradas
            for r in reservas:
                self.tabla.rows.append(
                    ft.DataRow(
                        cells=[
                            ft.DataCell(ft.Text(str(r.id_reserva))),
                            ft.DataCell(ft.Text(r.cliente.nombre)),
                            ft.DataCell(ft.Text(r.cancha.nombre)),
                            ft.DataCell(ft.Text(r.horario.hora_inicio + "-" + r.horario.hora_fin)),
                            ft.DataCell(ft.Text(r.fecha)),
                            ft.DataCell(ft.Text(f"${r.monto:.2f}")),
                            ft.DataCell(ft.Text(r.estado)),
                            ft.DataCell(
                                ft.Row([
                                    ft.IconButton(
                                        icon=ft.Icons.CANCEL,
                                        tooltip="Cancelar reserva",
                                        icon_color=ft.Colors.ORANGE,
                                        on_click=lambda e, id_reserva=r.id_reserva: self.cancelar_reserva(id_reserva),
                                    ),
                                    ft.IconButton(
                                        icon=ft.Icons.DELETE,
                                        tooltip="Eliminar reserva",
                                        icon_color=ft.Colors.RED,
                                        on_click=lambda e, id_reserva=r.id_reserva: self.eliminar_reserva(id_reserva),
                                    ),
                                ], spacing=0)
                            ),
                        ]
                    )
                )
            self.page.update()
        except Exception as err:
            print(f"Error al aplicar filtros: {err}")

    def limpiar_filtros(self, e):
        """Limpia los filtros y muestra todas las reservas."""
        self.cmb_filtro_estado.value = "Todos"
        self.cmb_filtro_cancha.value = "Todas"
        self.refrescar_tabla()
        self.page.update()

    def eliminar_reserva(self, id_reserva):
        """Elimina una reserva si cumple las reglas de negocio."""
        try:
            from dao.reserva_dao import ReservaDAO
            
            # Intentar eliminar directamente - el DAO tiene la validación
            ReservaDAO.eliminar(id_reserva)
            
            self.mostrar_exito(f"🗑️ Reserva #{id_reserva} eliminada correctamente")
            
            # Refrescar tabla
            self.refrescar_tabla()
            
        except ValueError as err:
            # Errores de validación (reserva confirmada con pagos)
            self.mostrar_advertencia(str(err))
        except Exception as err:
            # Otros errores
            self.mostrar_error(f"❌ Error al eliminar: {err}")
            print(f"Error en eliminar_reserva: {err}")
    
    def cancelar_reserva(self, id_reserva):
        """Cambia el estado de una reserva a 'cancelada'."""
        try:
            from dao.reserva_dao import ReservaDAO
            
            # Obtener la reserva
            reserva = ReservaDAO.obtener_por_id(id_reserva)
            if not reserva:
                self.mostrar_error("No se encontró la reserva")
                return
            
            # Cambiar estado a cancelada
            reserva.estado = "cancelada"
            ReservaDAO.actualizar(reserva)
            
            self.mostrar_exito(f"✅ Reserva #{id_reserva} cancelada correctamente")
            self.refrescar_tabla()
            
        except Exception as err:
            self.mostrar_error(f"❌ Error al cancelar: {err}")
            print(f"Error en cancelar_reserva: {err}")

