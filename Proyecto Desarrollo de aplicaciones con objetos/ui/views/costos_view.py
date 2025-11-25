"""
Vista de reportes de costos y presupuestos.
"""
import flet as ft
from services.costos_service import CostosService
from utils.pdf_reportes import PDFReportService


class CostosView(ft.Column):
    """Vista para gestión de costos y presupuestos."""

    def __init__(self, page: ft.Page):
        super().__init__(expand=True, scroll="adaptive", spacing=15)
        self.page = page
        self.crear_ui()

    def crear_ui(self):
        """Construye la interfaz de costos."""
        
        # --- TABLA: ESTADO GENERAL DE PAGOS ---
        self.contenedor_estado = ft.Container(
            content=ft.Column(spacing=10),
            padding=15,
            bgcolor=ft.Colors.BLUE_50,
            border_radius=10,
        )
        
        # --- TABLA: INGRESOS POR CANCHA ---
        self.tabla_ingresos_cancha = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("Cancha")),
                ft.DataColumn(ft.Text("Reservas")),
                ft.DataColumn(ft.Text("Ingresos ($)")),
            ],
            rows=[],
        )
        
        # --- TABLA: PAGOS POR MÉTODO ---
        self.tabla_pagos_metodo = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("Método de Pago")),
                ft.DataColumn(ft.Text("Cantidad")),
                ft.DataColumn(ft.Text("Total ($)")),
            ],
            rows=[],
        )
        
        # --- TABLA: PRESUPUESTO MENSUAL ---
        self.tabla_presupuesto = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("Mes")),
                ft.DataColumn(ft.Text("Reservas")),
                ft.DataColumn(ft.Text("Presupuesto ($)")),
                ft.DataColumn(ft.Text("Cobrado ($)")),
                ft.DataColumn(ft.Text("Diferencia ($)")),
            ],
            rows=[],
        )
        
        # --- TABLA: TOP CLIENTES ---
        self.tabla_top_clientes = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("Cliente")),
                ft.DataColumn(ft.Text("Reservas")),
                ft.DataColumn(ft.Text("Invertido ($)")),
                ft.DataColumn(ft.Text("Pagado ($)")),
            ],
            rows=[],
        )
        
        # --- TABLA: COBRANZA ---
        self.tabla_cobranza = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("Período")),
                ft.DataColumn(ft.Text("Pagos")),
                ft.DataColumn(ft.Text("Total Cobrado ($)")),
            ],
            rows=[],
        )
        
        # --- BOTONES ---
        btn_actualizar = ft.ElevatedButton(
            "Actualizar",
            icon=ft.Icons.REFRESH,
            on_click=lambda e: self.refrescar_datos()
        )
        
        btn_exportar_pdf = ft.ElevatedButton(
            "Exportar a PDF",
            icon=ft.Icons.PICTURE_AS_PDF,
            on_click=self.exportar_pdf
        )
        
        # --- LAYOUT ---
        self.controls = [
            ft.Text("Reportes de Costos y Presupuestos", size=20, weight=ft.FontWeight.BOLD),
            
            ft.Row([btn_actualizar, btn_exportar_pdf], spacing=10),
            ft.Divider(),
            
            # Estado general
            ft.Text("Estado General de Pagos e Ingresos", size=14, weight=ft.FontWeight.BOLD),
            self.contenedor_estado,
            ft.Divider(),
            
            # Ingresos por cancha
            ft.Text("Ingresos por Cancha", size=14, weight=ft.FontWeight.BOLD),
            self.tabla_ingresos_cancha,
            ft.Divider(),
            
            # Pagos por método
            ft.Text("Pagos Realizados por Método", size=14, weight=ft.FontWeight.BOLD),
            self.tabla_pagos_metodo,
            ft.Divider(),
            
            # Presupuesto mensual
            ft.Text("Presupuesto vs Cobrado Mensualmente", size=14, weight=ft.FontWeight.BOLD),
            self.tabla_presupuesto,
            ft.Divider(),
            
            # Top clientes
            ft.Text("Top 10 Clientes por Inversión", size=14, weight=ft.FontWeight.BOLD),
            self.tabla_top_clientes,
            ft.Divider(),
            
            # Cobranza
            ft.Text("Resumen de Cobranza", size=14, weight=ft.FontWeight.BOLD),
            self.tabla_cobranza,
        ]
        
        # Cargar datos iniciales
        self.refrescar_datos()

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

    def refrescar_datos(self):
        """Recarga todos los datos de costos."""
        try:
            # Estado general
            self.refrescar_estado()
            
            # Ingresos por cancha
            self.refrescar_ingresos_cancha()
            
            # Pagos por método
            self.refrescar_pagos_metodo()
            
            # Presupuesto mensual
            self.refrescar_presupuesto()
            
            # Top clientes
            self.refrescar_top_clientes()
            
            # Cobranza
            self.refrescar_cobranza()
            
            self.mostrar_exito("✅ Reportes actualizados")
            
        except Exception as ex:
            self.mostrar_error(f"❌ Error: {str(ex)}")

    def refrescar_estado(self):
        """Actualiza el estado general de pagos e ingresos."""
        try:
            estado = CostosService.obtener_estado_pagos()
            
            if estado:
                ingresos = estado.get('ingresos_confirmados', 0)
                pagos = estado.get('pagos_realizados', 0)
                pendientes = estado.get('pendientes', 0)
                canceladas = estado.get('canceladas', 0)
                balance = estado.get('balance', 0)
                
                # Calcular porcentaje de cobranza
                porcentaje_cobranza = (pagos / ingresos * 100) if ingresos > 0 else 0
                
                contenido = ft.Column([
                    ft.Text("💰 Resumen Financiero", weight=ft.FontWeight.BOLD, size=12),
                    ft.Row([
                        ft.Column([
                            ft.Text("Ingresos Confirmados", size=10, weight=ft.FontWeight.BOLD),
                            ft.Text(f"${ingresos:,.2f}", size=14, color=ft.Colors.GREEN),
                        ]),
                        ft.Column([
                            ft.Text("Pagos Realizados", size=10, weight=ft.FontWeight.BOLD),
                            ft.Text(f"${pagos:,.2f}", size=14, color=ft.Colors.BLUE),
                        ]),
                        ft.Column([
                            ft.Text("Pendientes", size=10, weight=ft.FontWeight.BOLD),
                            ft.Text(f"${pendientes:,.2f}", size=14, color=ft.Colors.ORANGE),
                        ]),
                        ft.Column([
                            ft.Text("Canceladas", size=10, weight=ft.FontWeight.BOLD),
                            ft.Text(f"${canceladas:,.2f}", size=14, color=ft.Colors.RED),
                        ]),
                    ], spacing=20, wrap=True),
                    ft.Row([
                        ft.Column([
                            ft.Text("Balance", size=10, weight=ft.FontWeight.BOLD),
                            ft.Text(f"${balance:,.2f}", size=14, 
                                   color=ft.Colors.GREEN if balance >= 0 else ft.Colors.RED),
                        ]),
                        ft.Column([
                            ft.Text("Cobranza", size=10, weight=ft.FontWeight.BOLD),
                            ft.Text(f"{porcentaje_cobranza:.1f}%", size=14, color=ft.Colors.PURPLE),
                        ]),
                    ], spacing=20),
                ])
                
                self.contenedor_estado.content = contenido
            
            self.page.update()
        except Exception as ex:
            print(f"Error refrescando estado: {ex}")

    def refrescar_ingresos_cancha(self):
        """Actualiza tabla de ingresos por cancha."""
        try:
            datos = CostosService.obtener_ingresos_por_cancha()
            self.tabla_ingresos_cancha.rows.clear()
            
            for cancha, ingresos, reservas in datos:
                if ingresos is None:
                    ingresos = 0
                fila = ft.DataRow(cells=[
                    ft.DataCell(ft.Text(str(cancha))),
                    ft.DataCell(ft.Text(str(reservas))),
                    ft.DataCell(ft.Text(f"${ingresos:,.2f}")),
                ])
                self.tabla_ingresos_cancha.rows.append(fila)
            
            self.page.update()
        except Exception as ex:
            print(f"Error: {ex}")

    def refrescar_pagos_metodo(self):
        """Actualiza tabla de pagos por método."""
        try:
            datos = CostosService.obtener_pagos_por_metodo()
            self.tabla_pagos_metodo.rows.clear()
            
            for metodo, cantidad, total in datos:
                if total is None:
                    total = 0
                fila = ft.DataRow(cells=[
                    ft.DataCell(ft.Text(str(metodo))),
                    ft.DataCell(ft.Text(str(cantidad))),
                    ft.DataCell(ft.Text(f"${total:,.2f}")),
                ])
                self.tabla_pagos_metodo.rows.append(fila)
            
            self.page.update()
        except Exception as ex:
            print(f"Error: {ex}")

    def refrescar_presupuesto(self):
        """Actualiza tabla de presupuesto mensual."""
        try:
            datos = CostosService.obtener_presupuesto_mensual()
            self.tabla_presupuesto.rows.clear()
            
            for mes, reservas, presupuesto, pagos in datos:
                if presupuesto is None:
                    presupuesto = 0
                if pagos is None:
                    pagos = 0
                diferencia = presupuesto - pagos
                
                fila = ft.DataRow(cells=[
                    ft.DataCell(ft.Text(str(mes))),
                    ft.DataCell(ft.Text(str(reservas))),
                    ft.DataCell(ft.Text(f"${presupuesto:,.2f}")),
                    ft.DataCell(ft.Text(f"${pagos:,.2f}")),
                    ft.DataCell(ft.Text(f"${diferencia:,.2f}", 
                                       color=ft.Colors.RED if diferencia > 0 else ft.Colors.GREEN)),
                ])
                self.tabla_presupuesto.rows.append(fila)
            
            self.page.update()
        except Exception as ex:
            print(f"Error: {ex}")

    def refrescar_top_clientes(self):
        """Actualiza tabla de top clientes."""
        try:
            datos = CostosService.obtener_ingresos_por_cliente()
            self.tabla_top_clientes.rows.clear()
            
            for i, (cliente, reservas, invertido, pagado) in enumerate(datos, 1):
                if invertido is None:
                    invertido = 0
                if pagado is None:
                    pagado = 0
                
                fila = ft.DataRow(cells=[
                    ft.DataCell(ft.Text(f"{i}. {cliente}")),
                    ft.DataCell(ft.Text(str(reservas))),
                    ft.DataCell(ft.Text(f"${invertido:,.2f}")),
                    ft.DataCell(ft.Text(f"${pagado:,.2f}")),
                ])
                self.tabla_top_clientes.rows.append(fila)
            
            self.page.update()
        except Exception as ex:
            print(f"Error: {ex}")

    def refrescar_cobranza(self):
        """Actualiza tabla de cobranza."""
        try:
            datos = CostosService.obtener_resumen_cobranza()
            self.tabla_cobranza.rows.clear()
            
            for periodo, cantidad, total in datos:
                if total is None:
                    total = 0
                fila = ft.DataRow(cells=[
                    ft.DataCell(ft.Text(str(periodo))),
                    ft.DataCell(ft.Text(str(cantidad))),
                    ft.DataCell(ft.Text(f"${total:,.2f}")),
                ])
                self.tabla_cobranza.rows.append(fila)
            
            self.page.update()
        except Exception as ex:
            print(f"Error: {ex}")

    def exportar_pdf(self, e):
        """Exporta el reporte a PDF."""
        try:
            # Datos para el PDF
            estado = CostosService.obtener_estado_pagos()
            ingresos_cancha = CostosService.obtener_ingresos_por_cancha()
            pagos_metodo = CostosService.obtener_pagos_por_metodo()
            presupuesto = CostosService.obtener_presupuesto_mensual()
            clientes = CostosService.obtener_ingresos_por_cliente()
            
            # Generar PDF
            ruta = PDFReportService.generar_reporte_costos(
                estado, ingresos_cancha, pagos_metodo, presupuesto, clientes
            )
            
            self.mostrar_exito(f"✅ PDF generado: {ruta}")
        except Exception as ex:
            self.mostrar_error(f"❌ Error al generar PDF: {str(ex)}")
