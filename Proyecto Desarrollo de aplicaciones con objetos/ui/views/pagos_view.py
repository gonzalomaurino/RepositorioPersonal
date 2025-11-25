import flet as ft
from datetime import date
from services.pago_service import PagoService
from dao.reserva_dao import ReservaDAO
from dao.pago_dao import PagoDAO
from dao.conexion import obtener_conexion
from models.reserva import Reserva
import sqlite3


class PagosView(ft.Column):
    """Vista para registrar pagos de reservas con saldo dinámico."""

    def __init__(self, page: ft.Page):
        super().__init__(expand=True, scroll="adaptive", spacing=15)
        self.page = page
        self.crear_ui()

    def crear_ui(self):
        # --- Combobox de reservas ---
        self.cmb_reserva = ft.Dropdown(label="Reserva", width=350, on_change=self.al_cambiar_reserva)

        # --- Panel de información de saldo ---
        self.lbl_monto_total = ft.Text("Monto total: $0.00", size=14, weight=ft.FontWeight.BOLD)
        self.lbl_total_pagado = ft.Text("Total pagado: $0.00", size=14, color=ft.Colors.GREEN)
        self.lbl_saldo_pendiente = ft.Text("Saldo pendiente: $0.00", size=14, color=ft.Colors.RED, weight=ft.FontWeight.BOLD)

        # --- Campos de pago ---
        self.fecha_pago = ft.TextField(label="Fecha de pago", width=180, value=str(date.today()))
        
        # Campo para monto pagado EN ESTA TRANSACCION
        self.monto_pagado_transaccion = ft.TextField(
            label="Monto pagado ahora ($)", 
            width=150, 
            on_change=self.actualizar_monto_pagado
        )
        
        # Campo para sugerir el saldo pendiente
        self.monto_saldo = ft.TextField(
            label="Saldo a pagar ($)", 
            width=150,
            read_only=True
        )
        
        self.metodo = ft.Dropdown(
            label="Método de pago",
            width=200,
            options=[
                ft.dropdown.Option("Efectivo"),
                ft.dropdown.Option("Transferencia"),
                ft.dropdown.Option("Tarjeta de crédito"),
                ft.dropdown.Option("Tarjeta de débito"),
            ],
        )

        # --- Label para mostrar el monto a pagar en esta transaccion ---
        self.lbl_monto_transaccion = ft.Text("Monto de esta transacción: $0.00", size=12, weight=ft.FontWeight.BOLD, color=ft.Colors.BLUE)

        # --- Botón guardar ---
        btn_guardar = ft.ElevatedButton(
            "Registrar pago", icon=ft.Icons.PAYMENTS, on_click=self.registrar_pago
        )

        # --- Filtros para tabla de pagos ---
        self.cmb_filtro_cancha = ft.Dropdown(label="Filtrar por cancha", width=200, on_change=self.aplicar_filtros_tabla)
        self.cmb_filtro_metodo = ft.Dropdown(
            label="Filtrar por método",
            width=200,
            options=[
                ft.dropdown.Option("Todos"),
                ft.dropdown.Option("Efectivo"),
                ft.dropdown.Option("Transferencia"),
                ft.dropdown.Option("Tarjeta de crédito"),
                ft.dropdown.Option("Tarjeta de débito"),
            ],
            value="Todos",
            on_change=self.aplicar_filtros_tabla
        )
        btn_limpiar_filtros = ft.ElevatedButton(
            "Limpiar filtros", icon=ft.Icons.CLEAR, on_click=self.limpiar_filtros_tabla
        )

        # --- Tabla de pagos ---
        self.tabla = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("ID Pago")),
                ft.DataColumn(ft.Text("Reserva")),
                ft.DataColumn(ft.Text("Monto")),
                ft.DataColumn(ft.Text("Método")),
                ft.DataColumn(ft.Text("Fecha")),
                ft.DataColumn(ft.Text("Acciones")),
            ],
            rows=[],
        )

        # --- Cargar datos iniciales ---
        self.cargar_reservas()
        self.cargar_filtro_canchas()
        self.refrescar_tabla()

        # --- Layout visual ---
        self.controls = [
            ft.Text("Registro de Pagos", size=20, weight=ft.FontWeight.BOLD),
            ft.Row([self.cmb_reserva, self.monto_pagado_transaccion, self.monto_saldo, self.metodo, self.fecha_pago, btn_guardar],
                   wrap=True, spacing=10),
            self.lbl_monto_transaccion,
            # Panel de saldo
            ft.Container(
                content=ft.Column([
                    self.lbl_monto_total,
                    self.lbl_total_pagado,
                    self.lbl_saldo_pendiente,
                ], spacing=5),
                bgcolor=ft.Colors.BLUE_GREY_100,
                padding=15,
                border_radius=10
            ),
            ft.Divider(height=5),
            ft.Text("Filtros de pagos registrados", size=16, weight=ft.FontWeight.BOLD),
            ft.Row([self.cmb_filtro_cancha, self.cmb_filtro_metodo, btn_limpiar_filtros], wrap=True, spacing=10),
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

    def cargar_reservas(self):
        """Carga reservas disponibles (pendientes o con seña)."""
        try:
            reservas = ReservaDAO.obtener_todas()
            # Ordenar por ID de reserva
            reservas_ordenadas = sorted(reservas, key=lambda r: r.id_reserva)
            self.cmb_reserva.options = [
                ft.dropdown.Option(f"{r.id_reserva} - {r.cliente.nombre} / {r.cancha.nombre} / {r.fecha}")
                for r in reservas_ordenadas
            ]
            self.page.update()
        except Exception as e:
            print(f"Error al cargar reservas: {e}")

    def al_cambiar_reserva(self, e):
        """Se ejecuta cuando cambia la reserva seleccionada y actualiza el saldo."""
        if not self.cmb_reserva.value:
            return
        
        try:
            id_reserva = int(self.cmb_reserva.value.split("-")[0])
            detalles = PagoService.obtener_detalles_pago(id_reserva)
            
            # Actualizar labels
            self.lbl_monto_total.value = f"Monto total: ${detalles['monto_total']:.2f}"
            self.lbl_total_pagado.value = f"Total pagado: ${detalles['total_pagado']:.2f}"
            self.lbl_saldo_pendiente.value = f"Saldo pendiente: ${detalles['saldo_pendiente']:.2f}"
            
            # Mostrar el saldo pendiente (read-only)
            self.monto_saldo.value = f"{detalles['saldo_pendiente']:.2f}"
            
            # Limpiar campo de monto pagado en esta transacción
            self.monto_pagado_transaccion.value = ""
            self.lbl_monto_transaccion.value = "Monto de esta transacción: $0.00"
            
            self.page.update()
        except Exception as e:
            print(f"Error al cambiar reserva: {e}")

    def actualizar_monto_pagado(self, e):
        """Actualiza el label de monto pagado cuando cambia el valor."""
        try:
            if self.monto_pagado_transaccion.value:
                monto = float(self.monto_pagado_transaccion.value)
                self.lbl_monto_transaccion.value = f"Monto de esta transacción: ${monto:.2f}"
            else:
                self.lbl_monto_transaccion.value = "Monto de esta transacción: $0.00"
            self.page.update()
        except ValueError:
            self.lbl_monto_transaccion.value = "Monto de esta transacción: Ingresa un valor válido"
            self.page.update()

    def registrar_pago(self, e):
        """Registra un pago asociado a una reserva con validaciones exhaustivas."""
        try:
            # ========== VALIDACIÓN 1: Reserva seleccionada ==========
            if not self.cmb_reserva.value:
                self.mostrar_error("❌ Debe seleccionar una reserva de la lista")
                return
            
            # ========== VALIDACIÓN 2: Monto ingresado ==========
            if not self.monto_pagado_transaccion.value or not self.monto_pagado_transaccion.value.strip():
                self.mostrar_error("❌ Debe ingresar el monto pagado en esta transacción")
                return
            
            # ========== VALIDACIÓN 3: Monto es número válido ==========
            try:
                monto = float(self.monto_pagado_transaccion.value)
            except ValueError:
                self.mostrar_error("❌ El MONTO debe ser un número válido (ej: 1000, 1500.50)")
                return
            
            # ========== VALIDACIÓN 4: Monto mayor a cero ==========
            if monto <= 0:
                self.mostrar_error("❌ El MONTO debe ser mayor a cero ($0.01 mínimo)")
                return
            
            # ========== VALIDACIÓN 5: Método de pago seleccionado ==========
            if not self.metodo.value:
                self.mostrar_error("❌ Debe seleccionar un método de pago")
                return
            
            # ========== VALIDACIÓN 6: Fecha válida ==========
            if not self.fecha_pago.value:
                self.mostrar_error("❌ La fecha de pago es requerida")
                return
            
            try:
                fecha_ingresada = self.fecha_pago.value
                # Validar formato AAAA-MM-DD
                if len(fecha_ingresada.split("-")) != 3:
                    raise ValueError()
            except:
                self.mostrar_error("❌ Formato de fecha inválido. Use AAAA-MM-DD (ej: 2025-11-02)")
                return
            
            # ========== VALIDACIÓN 7: Obtener reserva ==========
            try:
                id_reserva = int(self.cmb_reserva.value.split("-")[0])
            except (ValueError, IndexError):
                self.mostrar_error("❌ Error al procesar la reserva seleccionada")
                return
            
            reserva = ReservaDAO.obtener_por_id(id_reserva)
            if not reserva:
                self.mostrar_error("❌ La reserva seleccionada no existe")
                return
            
            # ========== VALIDACIÓN 8: Saldo disponible ==========
            saldo_pendiente = PagoService.calcular_saldo_pendiente(id_reserva)
            if monto > saldo_pendiente:
                self.mostrar_advertencia(f"⚠️ El monto excede el saldo pendiente (${saldo_pendiente:.2f})")
                return
            
            # ========== VALIDACIÓN 9: Reserva no está cancelada ==========
            if reserva.estado == "confirmada":
                self.mostrar_advertencia("⚠️ Esta reserva ya está completamente pagada")
                return
            
            # ========== Si todas las validaciones pasan, registrar pago ==========
            PagoService.registrar_pago(reserva, monto, self.metodo.value)

            self.mostrar_exito(f"✅ Pago de ${monto:.2f} registrado correctamente")
            
            # Actualizar saldo dinámicamente desde la BD
            nuevo_saldo = PagoService.calcular_saldo_pendiente(id_reserva)
            detalles = PagoService.obtener_detalles_pago(id_reserva)
            
            # Actualizar todos los indicadores en tiempo real
            self.monto_saldo.value = f"{nuevo_saldo:.2f}"
            self.lbl_monto_total.value = f"Monto total: ${detalles['monto_total']:.2f}"
            self.lbl_total_pagado.value = f"Total pagado: ${detalles['total_pagado']:.2f}"
            self.lbl_saldo_pendiente.value = f"Saldo pendiente: ${detalles['saldo_pendiente']:.2f}"
            
            # Limpiar campo de monto pagado en esta transacción
            self.monto_pagado_transaccion.value = ""
            self.lbl_monto_transaccion.value = "Monto de esta transacción: $0.00"
            
            # Recargar tabla de pagos para mostrar el nuevo pago
            self.refrescar_tabla()
            self.page.update()

        except ValueError as err:
            self.mostrar_error(str(err))
        except Exception as err:
            self.mostrar_error(f"❌ Error inesperado: {str(err)}")
            print(f"Error en registrar_pago: {err}")

    def eliminar_pago(self, id_pago):
        """Elimina un pago y revierte la reserva a pendiente."""
        try:
            PagoService.eliminar_pago(id_pago)
            
            self.mostrar_exito("🗑️ Pago eliminado correctamente")
            
            # Recargar reservas y tabla de pagos
            self.cargar_reservas()
            
            # Actualizar saldo si hay una reserva seleccionada
            if self.cmb_reserva.value:
                id_reserva = int(self.cmb_reserva.value.split("-")[0])
                detalles = PagoService.obtener_detalles_pago(id_reserva)
                self.lbl_monto_total.value = f"Monto total: ${detalles['monto_total']:.2f}"
                self.lbl_total_pagado.value = f"Total pagado: ${detalles['total_pagado']:.2f}"
                self.lbl_saldo_pendiente.value = f"Saldo pendiente: ${detalles['saldo_pendiente']:.2f}"
            
            self.refrescar_tabla()
            
        except Exception as err:
            self.mostrar_error(f"❌ Error al eliminar: {err}")
            print(f"Error en eliminar_pago: {err}")

    def refrescar_tabla(self):
        """Lista todos los pagos registrados ordenados por ID descendente (últimos primero)."""
        try:
            pagos = PagoService.obtener_pagos_ordenados()

            self.tabla.rows.clear()
            for p in pagos:
                self.tabla.rows.append(
                    ft.DataRow(
                        cells=[
                            ft.DataCell(ft.Text(str(p[0]))),
                            ft.DataCell(ft.Text(str(p[4]))),
                            ft.DataCell(ft.Text(f"${p[2]:.2f}")),
                            ft.DataCell(ft.Text(p[3])),
                            ft.DataCell(ft.Text(p[1])),
                            ft.DataCell(
                                ft.IconButton(
                                    icon=ft.Icons.DELETE,
                                    tooltip="Eliminar pago",
                                    icon_color=ft.Colors.RED,
                                    on_click=lambda e, id_pago=p[0]: self.eliminar_pago(id_pago),
                                )
                            ),
                        ]
                    )
                )
            self.page.update()
        except Exception as e:
            print(f"❌ Error inesperado en refrescar_tabla: {e}")

    def cargar_filtro_canchas(self):
        """Carga las canchas para el filtro."""
        try:
            from services.cancha_service import CanchaService
            canchas = CanchaService.listar_canchas()
            canchas_ordenadas = sorted(canchas, key=lambda c: c.id_cancha)
            self.cmb_filtro_cancha.options = [
                ft.dropdown.Option(f"Todas")
            ] + [
                ft.dropdown.Option(f"{c.id_cancha} - {c.nombre}")
                for c in canchas_ordenadas
            ]
            self.cmb_filtro_cancha.value = "Todas"
            self.page.update()
        except Exception as e:
            print(f"Error al cargar canchas para filtro: {e}")

    def aplicar_filtros_tabla(self, e):
        """Aplica los filtros de cancha y método a la tabla de pagos."""
        try:
            cancha_filtro = self.cmb_filtro_cancha.value
            metodo_filtro = self.cmb_filtro_metodo.value
            
            # Obtener pagos según los filtros
            if cancha_filtro == "Todas" and metodo_filtro == "Todos":
                # Mostrar todos los pagos
                pagos = PagoService.obtener_pagos_ordenados()
            elif cancha_filtro == "Todas" and metodo_filtro != "Todos":
                # Filtrar solo por método
                pagos = PagoService.filtrar_por_metodo(metodo_filtro)
            elif cancha_filtro != "Todas" and metodo_filtro == "Todos":
                # Filtrar solo por cancha
                id_cancha = int(cancha_filtro.split("-")[0])
                pagos = PagoService.filtrar_por_cancha(id_cancha)
            else:
                # Filtrar por cancha y método
                id_cancha = int(cancha_filtro.split("-")[0])
                pagos = PagoService.filtrar_por_cancha_y_metodo(id_cancha, metodo_filtro)
            
            # Actualizar tabla
            self.tabla.rows.clear()
            for p in pagos:
                self.tabla.rows.append(
                    ft.DataRow(
                        cells=[
                            ft.DataCell(ft.Text(str(p[0]))),
                            ft.DataCell(ft.Text(str(p[4]))),
                            ft.DataCell(ft.Text(f"${p[2]:.2f}")),
                            ft.DataCell(ft.Text(p[3])),
                            ft.DataCell(ft.Text(p[1])),
                            ft.DataCell(
                                ft.IconButton(
                                    icon=ft.Icons.DELETE,
                                    tooltip="Eliminar pago",
                                    icon_color=ft.Colors.RED,
                                    on_click=lambda e, id_pago=p[0]: self.eliminar_pago(id_pago),
                                )
                            ),
                        ]
                    )
                )
            self.page.update()
        except Exception as e:
            self.mostrar_error(f"❌ Error al aplicar filtros: {e}")
            print(f"Error en aplicar_filtros_tabla: {e}")

    def limpiar_filtros_tabla(self, e):
        """Limpia los filtros y muestra todos los pagos."""
        try:
            self.cmb_filtro_cancha.value = "Todas"
            self.cmb_filtro_metodo.value = "Todos"
            self.refrescar_tabla()
            self.page.update()
        except Exception as e:
            self.mostrar_error(f"❌ Error al limpiar filtros: {e}")
