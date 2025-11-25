import flet as ft
from services.torneo_service import TorneoService
from services.reserva_service import ReservaService
from models.torneo import Torneo


class TorneosView(ft.Column):
    """Vista para gestión de TORNEOS y asignación de RESERVAS."""

    def __init__(self, page: ft.Page):
        super().__init__(expand=True, scroll="adaptive", spacing=15)
        self.page = page
        self.crear_ui()

    def crear_ui(self):
        """Construye la interfaz de torneos."""
        
        # --- SECCIÓN 1: CREAR TORNEO ---
        self.nombre_torneo = ft.TextField(label="Nombre del torneo", width=200)
        self.fecha_inicio_torneo = ft.TextField(label="Fecha inicio (YYYY-MM-DD)", width=200)
        self.fecha_fin_torneo = ft.TextField(label="Fecha fin (YYYY-MM-DD)", width=200)
        self.categoria_torneo = ft.TextField(label="Categoría (Ej: Fútbol 11)", width=200)
        
        self.btn_crear_torneo = ft.ElevatedButton(
            "Crear Torneo", 
            icon=ft.Icons.EMOJI_EVENTS,
            on_click=self.crear_torneo
        )
        
        # --- TABLA DE TORNEOS ---
        self.tabla_torneos = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("ID")),
                ft.DataColumn(ft.Text("Nombre")),
                ft.DataColumn(ft.Text("Categoría")),
                ft.DataColumn(ft.Text("Fecha Inicio")),
                ft.DataColumn(ft.Text("Fecha Fin")),
                ft.DataColumn(ft.Text("Reservas")),
                ft.DataColumn(ft.Text("Acciones")),
            ],
            rows=[],
        )
        
        # --- SECCIÓN 2: ASIGNAR RESERVAS ---
        self.dropdown_torneos = ft.Dropdown(
            label="Seleccionar Torneo",
            width=200,
            on_change=self.torneo_seleccionado_cambio
        )
        
        self.dropdown_reservas = ft.Dropdown(
            label="Seleccionar Reserva",
            width=200,
        )
        
        self.btn_asignar = ft.ElevatedButton(
            "Asignar Reserva",
            icon=ft.Icons.ADD,
            on_click=self.asignar_reserva_a_torneo
        )
        
        # --- TABLA DE RESERVAS DEL TORNEO ---
        self.tabla_reservas_torneo = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("ID Reserva")),
                ft.DataColumn(ft.Text("Cliente")),
                ft.DataColumn(ft.Text("Cancha")),
                ft.DataColumn(ft.Text("Fecha")),
                ft.DataColumn(ft.Text("Horario")),
                ft.DataColumn(ft.Text("Monto")),
                ft.DataColumn(ft.Text("Acciones")),
            ],
            rows=[],
        )
        
        # --- LAYOUT VISUAL ---
        self.controls = [
            ft.Text("Gestión de Torneos", size=20, weight=ft.FontWeight.BOLD),
            
            # SECCIÓN: CREAR TORNEO
            ft.Container(
                content=ft.Column([
                    ft.Text("Crear Nuevo Torneo", size=14, weight=ft.FontWeight.BOLD),
                    ft.Row([
                        self.nombre_torneo,
                        self.categoria_torneo,
                        self.fecha_inicio_torneo,
                        self.fecha_fin_torneo,
                        self.btn_crear_torneo,
                    ], wrap=True, spacing=10),
                ]),
                padding=15,
                border_radius=10,
            ),
            
            # SECCIÓN: LISTA DE TORNEOS
            ft.Text("Torneos Disponibles", size=14, weight=ft.FontWeight.BOLD),
            self.tabla_torneos,
            
            ft.Divider(),
            
            # SECCIÓN: ASIGNAR RESERVAS
            ft.Container(
                content=ft.Column([
                    ft.Text("Asignar Reservas a Torneo", size=14, weight=ft.FontWeight.BOLD),
                    ft.Row([
                        self.dropdown_torneos,
                        self.dropdown_reservas,
                        self.btn_asignar,
                    ], spacing=10),
                ]),
                padding=15,
                border_radius=10,
            ),
            
            # SECCIÓN: RESERVAS DEL TORNEO SELECCIONADO
            ft.Text("Reservas del Torneo Seleccionado", size=14, weight=ft.FontWeight.BOLD),
            self.tabla_reservas_torneo,
        ]
        
        # Cargar datos iniciales
        self.refrescar_torneos()

    def mostrar_error(self, mensaje):
        """Muestra error en SnackBar rojo."""
        snack = ft.SnackBar(
            content=ft.Text(mensaje, color=ft.Colors.WHITE, size=14),
            bgcolor=ft.Colors.RED_700,
            duration=4000
        )
        self.page.overlay.append(snack)
        snack.open = True
        self.page.update()

    def mostrar_exito(self, mensaje):
        """Muestra éxito en SnackBar verde."""
        snack = ft.SnackBar(
            content=ft.Text(mensaje, color=ft.Colors.WHITE, size=14),
            bgcolor=ft.Colors.GREEN_700,
            duration=3000
        )
        self.page.overlay.append(snack)
        snack.open = True
        self.page.update()

    def mostrar_advertencia(self, mensaje):
        """Muestra advertencia en SnackBar naranja."""
        snack = ft.SnackBar(
            content=ft.Text(mensaje, color=ft.Colors.WHITE, size=14),
            bgcolor=ft.Colors.ORANGE_700,
            duration=3000
        )
        self.page.overlay.append(snack)
        snack.open = True
        self.page.update()

    def crear_torneo(self, e):
        """Crea un nuevo torneo."""
        try:
            nombre = self.nombre_torneo.value.strip()
            categoria = self.categoria_torneo.value.strip()
            fecha_inicio = self.fecha_inicio_torneo.value.strip()
            fecha_fin = self.fecha_fin_torneo.value.strip()
            
            if not nombre or not categoria or not fecha_inicio or not fecha_fin:
                self.mostrar_error("❌ Todos los campos son obligatorios")
                return
            
            TorneoService.crear_torneo(nombre, fecha_inicio, fecha_fin, categoria)
            
            # Limpiar campos
            self.nombre_torneo.value = ""
            self.categoria_torneo.value = ""
            self.fecha_inicio_torneo.value = ""
            self.fecha_fin_torneo.value = ""
            
            self.mostrar_exito(f"✅ Torneo '{nombre}' creado exitosamente")
            self.refrescar_torneos()
            
        except Exception as ex:
            self.mostrar_error(f"❌ Error: {str(ex)}")

    def refrescar_torneos(self):
        """Recarga la tabla de torneos."""
        try:
            torneos = TorneoService.listar_torneos()
            
            # Limpiar tabla
            self.tabla_torneos.rows.clear()
            
            # Llenar tabla
            for torneo in torneos:
                # Obtener cantidad de reservas del torneo
                reservas_torneo = TorneoService.obtener_reservas_torneo(torneo.id_torneo)
                cantidad_reservas = len(reservas_torneo)
                
                # Botones de acción
                btn_ver = ft.IconButton(
                    icon=ft.Icons.VISIBILITY,
                    tooltip="Ver reservas",
                    on_click=lambda e, t=torneo: self.ver_reservas_torneo(t.id_torneo)
                )
                
                btn_eliminar = ft.IconButton(
                    icon=ft.Icons.DELETE,
                    icon_color=ft.Colors.RED,
                    tooltip="Eliminar torneo",
                    on_click=lambda e, t=torneo: self.eliminar_torneo(t.id_torneo, t.nombre)
                )
                
                fila = ft.DataRow(cells=[
                    ft.DataCell(ft.Text(str(torneo.id_torneo))),
                    ft.DataCell(ft.Text(torneo.nombre)),
                    ft.DataCell(ft.Text(torneo.categoria)),
                    ft.DataCell(ft.Text(torneo.fecha_inicio)),
                    ft.DataCell(ft.Text(torneo.fecha_fin)),
                    ft.DataCell(ft.Text(str(cantidad_reservas))),
                    ft.DataCell(ft.Row([btn_ver, btn_eliminar], spacing=0)),
                ])
                self.tabla_torneos.rows.append(fila)
            
            # Actualizar dropdown de torneos
            self.dropdown_torneos.options = [
                ft.dropdown.Option(str(t.id_torneo), f"{t.nombre} ({t.categoria})")
                for t in torneos
            ]
            
            self.page.update()
            
        except Exception as ex:
            self.mostrar_error(f"❌ Error al cargar torneos: {str(ex)}")

    def torneo_seleccionado_cambio(self, e):
        """Cuando cambia el torneo seleccionado, actualiza las reservas disponibles."""
        try:
            if not self.dropdown_torneos.value:
                self.dropdown_reservas.options = []
                self.tabla_reservas_torneo.rows.clear()
                self.page.update()
                return
            
            id_torneo = int(self.dropdown_torneos.value)
            
            # Obtener reservas del torneo usando Service
            reservas_torneo = TorneoService.obtener_reservas_torneo(id_torneo)
            self.mostrar_reservas_torneo(reservas_torneo)
            
            # Cargar reservas disponibles (que no estén en el torneo)
            todas_reservas = ReservaService.listar_todas()
            ids_en_torneo = {r.id_reserva for r in reservas_torneo}
            
            reservas_disponibles = [
                r for r in todas_reservas 
                if r.id_reserva not in ids_en_torneo
            ]
            
            # Ordenar por ID de reserva (número)
            reservas_disponibles.sort(key=lambda r: r.id_reserva)
            
            self.dropdown_reservas.options = [
                ft.dropdown.Option(
                    str(r.id_reserva), 
                    f"Reserva {r.id_reserva} - {r.cliente.nombre} - {r.cancha.nombre}"
                )
                for r in reservas_disponibles
            ]
            
            self.page.update()
            
        except Exception as ex:
            self.mostrar_error(f"❌ Error: {str(ex)}")

    def mostrar_reservas_torneo(self, reservas):
        """Muestra las reservas del torneo seleccionado en la tabla."""
        self.tabla_reservas_torneo.rows.clear()
        
        for reserva in reservas:
            btn_desasignar = ft.IconButton(
                icon=ft.Icons.REMOVE,
                icon_color=ft.Colors.RED,
                tooltip="Desasignar reserva",
                on_click=lambda e, t=int(self.dropdown_torneos.value), r=reserva: 
                    self.desasignar_reserva(t, r.id_reserva)
            )
            
            fila = ft.DataRow(cells=[
                ft.DataCell(ft.Text(str(reserva.id_reserva))),
                ft.DataCell(ft.Text(f"{reserva.cliente.nombre} {reserva.cliente.apellido}")),
                ft.DataCell(ft.Text(reserva.cancha.nombre)),
                ft.DataCell(ft.Text(reserva.fecha)),
                ft.DataCell(ft.Text(f"{reserva.horario.hora_inicio}-{reserva.horario.hora_fin}")),
                ft.DataCell(ft.Text(f"${reserva.monto:.2f}")),
                ft.DataCell(btn_desasignar),
            ])
            self.tabla_reservas_torneo.rows.append(fila)
        
        self.page.update()

    def asignar_reserva_a_torneo(self, e):
        """Asigna una reserva al torneo seleccionado."""
        try:
            if not self.dropdown_torneos.value:
                self.mostrar_advertencia("⚠️ Selecciona un torneo primero")
                return
            
            if not self.dropdown_reservas.value:
                self.mostrar_advertencia("⚠️ Selecciona una reserva")
                return
            
            id_torneo = int(self.dropdown_torneos.value)
            id_reserva = int(self.dropdown_reservas.value)
            
            TorneoService.asignar_reserva_a_torneo(id_torneo, id_reserva)
            
            self.mostrar_exito(f"✅ Reserva {id_reserva} asignada al torneo")
            
            # Refrescar
            self.torneo_seleccionado_cambio(None)
            self.refrescar_torneos()
            
        except ValueError as ex:
            # Errores de validación (reserva ya asignada a otro torneo)
            self.mostrar_advertencia(str(ex))
        except Exception as ex:
            self.mostrar_error(f"❌ Error: {str(ex)}")

    def desasignar_reserva(self, id_torneo, id_reserva):
        """Desasocia una reserva del torneo."""
        try:
            TorneoService.desasignar_reserva_de_torneo(id_torneo, id_reserva)
            self.mostrar_exito(f"✅ Reserva {id_reserva} desasignada")
            self.torneo_seleccionado_cambio(None)
            self.refrescar_torneos()
        except Exception as ex:
            self.mostrar_error(f"❌ Error: {str(ex)}")

    def ver_reservas_torneo(self, id_torneo):
        """Muestra las reservas de un torneo."""
        try:
            # Seleccionar el torneo en el dropdown
            self.dropdown_torneos.value = str(id_torneo)
            self.torneo_seleccionado_cambio(None)
        except Exception as ex:
            self.mostrar_error(f"❌ Error: {str(ex)}")

    def eliminar_torneo(self, id_torneo, nombre):
        """Elimina un torneo."""
        try:
            TorneoService.eliminar_torneo(id_torneo)
            self.mostrar_exito(f"✅ Torneo '{nombre}' eliminado")
            self.refrescar_torneos()
        except ValueError as ex:
            # Errores de validación (torneo tiene reservas)
            self.mostrar_advertencia(str(ex))
        except Exception as ex:
            self.mostrar_error(f"❌ Error: {str(ex)}")
