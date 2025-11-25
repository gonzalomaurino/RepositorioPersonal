import flet as ft
from utils.reportes import ReporteService
from utils.graficos import GraficosService
from utils.pdf_reportes import PDFReportService
import os


class ReportesView(ft.Column):
    """Vista de reportes y estadísticas del sistema."""

    def __init__(self, page: ft.Page):
        super().__init__(expand=True, scroll="adaptive", spacing=20)
        self.page = page
        self.datos_cargados = False
        self.ultimo_reporte = None  # Guarda el tipo y datos del último reporte generado
        self.crear_ui()

    def crear_ui(self):
        # --- Título ---
        titulo = ft.Text("Reportes y Estadísticas", size=22, weight=ft.FontWeight.BOLD)

        # --- Botones de acción ---
        btn_clientes = ft.ElevatedButton("📊 Reservas por Cliente", on_click=self.mostrar_reservas_por_cliente)
        btn_canchas = ft.ElevatedButton("🏟️ Reservas por Cancha", on_click=self.mostrar_reservas_por_cancha)
        btn_mensual = ft.ElevatedButton("📅 Utilización Mensual", on_click=self.mostrar_utilizacion_mensual)
        
        # --- Botón para exportar a PDF ---
        self.btn_exportar_pdf = ft.ElevatedButton(
            "📄 Exportar a PDF",
            icon=ft.Icons.PICTURE_AS_PDF,
            on_click=self.exportar_a_pdf,
            disabled=True,
            bgcolor=ft.Colors.RED_700,
            color=ft.Colors.WHITE
        )

        # Inicializar tabla vacía
        self.area_resultado = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("Información")),
                ft.DataColumn(ft.Text("Total")),
            ],
            rows=[]
        )

        # Contenedor central con mensaje de carga inicial
        self.contenedor_central = ft.Container(
            content=ft.Column(
                [
                    ft.Icon(name=ft.Icons.SCHEDULE, size=80, color=ft.Colors.BLUE_400),
                    ft.Text("⏳ Generando Estadísticas...", 
                          size=18, weight=ft.FontWeight.BOLD, color=ft.Colors.BLUE_700,
                          text_align=ft.TextAlign.CENTER),
                    ft.Text("Selecciona una categoría para comenzar", 
                          size=12, color=ft.Colors.GREY_600,
                          text_align=ft.TextAlign.CENTER),
                ],
                alignment=ft.MainAxisAlignment.CENTER,
                horizontal_alignment=ft.CrossAxisAlignment.CENTER,
                spacing=20,
            ),
            expand=True,
            bgcolor=ft.Colors.GREY_100,
            border_radius=10,
            alignment=ft.alignment.center
        )

        # Layout principal
        self.controls = [
            titulo,
            ft.Row([btn_clientes, btn_canchas, btn_mensual], wrap=True, spacing=10),
            self.btn_exportar_pdf,
            ft.Divider(),
            self.contenedor_central,
        ]

    # =======================================================
    # FUNCIONES DE REPORTE
    # =======================================================

    def mostrar_reservas_por_cliente(self, e):
        """Muestra cantidad de reservas agrupadas por cliente con gráfico de barras."""
        try:
            # Mostrar loading
            self._mostrar_cargando()
            
            datos = ReporteService.reservas_por_cliente()
            
            # Guardar para exportar
            self.ultimo_reporte = {"tipo": "clientes", "datos": datos}
            self.btn_exportar_pdf.disabled = False
            
            # Actualizar tabla
            self.area_resultado.columns = [
                ft.DataColumn(ft.Text("Cliente")),
                ft.DataColumn(ft.Text("Total Reservas")),
            ]
            self.area_resultado.rows = [
                ft.DataRow(cells=[ft.DataCell(ft.Text(d[0])), ft.DataCell(ft.Text(str(d[1])))])
                for d in datos
            ]
            
            # Generar gráfico de barras
            if datos:
                grafico_src = GraficosService.generar_grafico_barras(
                    datos,
                    "Reservas por Cliente",
                    "Cliente",
                    "Cantidad de Reservas"
                )
                self._mostrar_datos_y_grafico(grafico_src)
            
            self.page.snack_bar = ft.SnackBar(
                ft.Text("📊 Reporte de reservas por cliente cargado"),
                bgcolor=ft.Colors.GREEN
            )
            self.page.snack_bar.open = True
            self.page.update()
        except Exception as err:
            self.page.snack_bar = ft.SnackBar(
                ft.Text(f"❌ Error: {err}"),
                bgcolor=ft.Colors.RED_300
            )
            self.page.snack_bar.open = True
            self.page.update()

    def mostrar_reservas_por_cancha(self, e):
        """Muestra cantidad de reservas por cancha con gráfico de pastel."""
        try:
            # Mostrar loading
            self._mostrar_cargando()
            
            datos = ReporteService.reservas_por_cancha()
            
            # Guardar para exportar
            self.ultimo_reporte = {"tipo": "canchas", "datos": datos}
            self.btn_exportar_pdf.disabled = False
            
            # Actualizar tabla
            self.area_resultado.columns = [
                ft.DataColumn(ft.Text("Cancha")),
                ft.DataColumn(ft.Text("Total Reservas")),
            ]
            self.area_resultado.rows = [
                ft.DataRow(cells=[ft.DataCell(ft.Text(d[0])), ft.DataCell(ft.Text(str(d[1])))])
                for d in datos
            ]
            
            # Generar gráfico de pastel
            if datos:
                grafico_src = GraficosService.generar_grafico_pastel(
                    datos,
                    "Reservas por Cancha"
                )
                self._mostrar_datos_y_grafico(grafico_src)
            
            self.page.snack_bar = ft.SnackBar(
                ft.Text("🏟️ Reporte de reservas por cancha cargado"),
                bgcolor=ft.Colors.GREEN
            )
            self.page.snack_bar.open = True
            self.page.update()
        except Exception as err:
            self.page.snack_bar = ft.SnackBar(
                ft.Text(f"❌ Error: {err}"),
                bgcolor=ft.Colors.RED_300
            )
            self.page.snack_bar.open = True
            self.page.update()

    def mostrar_utilizacion_mensual(self, e):
        """Muestra la cantidad de reservas agrupadas por mes con gráfico de líneas."""
        try:
            # Mostrar loading
            self._mostrar_cargando()
            
            datos = ReporteService.utilizacion_mensual()
            
            # Guardar para exportar
            self.ultimo_reporte = {"tipo": "mensual", "datos": datos}
            self.btn_exportar_pdf.disabled = False
            
            # Actualizar tabla
            self.area_resultado.columns = [
                ft.DataColumn(ft.Text("Mes")),
                ft.DataColumn(ft.Text("Total Reservas")),
            ]
            self.area_resultado.rows = [
                ft.DataRow(cells=[ft.DataCell(ft.Text(d[0])), ft.DataCell(ft.Text(str(d[1])))])
                for d in datos
            ]
            
            # Generar gráfico de líneas
            if datos:
                grafico_src = GraficosService.generar_grafico_lineas(
                    datos,
                    "Utilización Mensual",
                    "Mes",
                    "Cantidad de Reservas"
                )
                self._mostrar_datos_y_grafico(grafico_src)
            
            self.page.snack_bar = ft.SnackBar(
                ft.Text("📅 Reporte de utilización mensual cargado"),
                bgcolor=ft.Colors.GREEN
            )
            self.page.snack_bar.open = True
            self.page.update()
        except Exception as err:
            self.page.snack_bar = ft.SnackBar(
                ft.Text(f"❌ Error: {err}"),
                bgcolor=ft.Colors.RED_300
            )
            self.page.snack_bar.open = True
            self.page.update()

    def _mostrar_cargando(self):
        """Muestra mensaje de carga."""
        self.contenedor_central.content = ft.Column(
            [
                ft.Icon(name=ft.Icons.SCHEDULE, size=80, color=ft.Colors.BLUE_400),
                ft.Text("⏳ Generando Estadísticas...", 
                      size=18, weight=ft.FontWeight.BOLD, color=ft.Colors.BLUE_700,
                      text_align=ft.TextAlign.CENTER),
            ],
            alignment=ft.MainAxisAlignment.CENTER,
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            spacing=20,
        )
        self.page.update()

    def _mostrar_datos_y_grafico(self, grafico_src):
        """Muestra tabla y gráfico lado a lado."""
        area_grafico = ft.Image(
            src_base64=grafico_src,
            width=700,
            height=500,
            fit=ft.ImageFit.CONTAIN
        )
        
        # Reemplazar contenedor central con layout tabla + gráfico
        self.contenedor_central.content = ft.Row(
            [
                ft.Column(
                    [ft.Text("Tabla de Datos", weight=ft.FontWeight.BOLD), self.area_resultado], 
                    width=350,
                    alignment=ft.MainAxisAlignment.START
                ),
                ft.Column(
                    [ft.Text("Gráfico", weight=ft.FontWeight.BOLD), area_grafico], 
                    expand=True,
                    alignment=ft.MainAxisAlignment.START
                ),
            ],
            spacing=20,
            expand=True,
            alignment=ft.MainAxisAlignment.START,
            vertical_alignment=ft.CrossAxisAlignment.START
        )
        self.page.update()

    def exportar_a_pdf(self, e):
        """Exporta el último reporte mostrado a PDF usando ReportLab."""
        if not self.ultimo_reporte:
            self.page.snack_bar = ft.SnackBar(
                ft.Text("⚠️ Primero genera un reporte"),
                bgcolor=ft.Colors.ORANGE
            )
            self.page.snack_bar.open = True
            self.page.update()
            return
        
        try:
            tipo = self.ultimo_reporte["tipo"]
            datos = self.ultimo_reporte["datos"]
            
            # Generar PDF según el tipo
            if tipo == "clientes":
                ruta = PDFReportService.generar_reporte_reservas_cliente(datos)
                nombre = "Reservas por Cliente"
            elif tipo == "canchas":
                ruta = PDFReportService.generar_reporte_reservas_cancha(datos)
                nombre = "Reservas por Cancha"
            elif tipo == "mensual":
                ruta = PDFReportService.generar_reporte_utilizacion_mensual(datos)
                nombre = "Utilización Mensual"
            else:
                raise ValueError("Tipo de reporte desconocido")
            
            # Convertir a ruta absoluta para abrir correctamente
            ruta_absoluta = os.path.abspath(ruta)
            
            # Abrir PDF automáticamente
            os.startfile(ruta_absoluta)
            
            self.page.snack_bar = ft.SnackBar(
                ft.Text(f"✅ PDF generado: {nombre}"),
                bgcolor=ft.Colors.GREEN,
                duration=3000
            )
            self.page.snack_bar.open = True
            self.page.update()
            
        except Exception as err:
            self.page.snack_bar = ft.SnackBar(
                ft.Text(f"❌ Error al generar PDF: {err}"),
                bgcolor=ft.Colors.RED_300
            )
            self.page.snack_bar.open = True
            self.page.update()
            print(f"Error en exportar_a_pdf: {err}")
