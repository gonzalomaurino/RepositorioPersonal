import flet as ft
from ui.views.clientes_view import ClientesView
from ui.views.reservas_view import ReservasView
from ui.views.pagos_view import PagosView
from ui.views.canchas_view import CanchasView
from ui.views.reportes_view import ReportesView
from ui.views.torneos_view import TorneosView
from ui.views.costos_view import CostosView


def main(page: ft.Page):
    page.title = "Sistema de Reservas de Canchas Deportivas"
    page.theme_mode = ft.ThemeMode.LIGHT
    page.window_width = 950
    page.window_height = 600
    page.scroll = "adaptive"

    # Contenedor principal con padding superior
    contenido = ft.Container(expand=True, content=ClientesView(page), padding=20)

    # Función para cambiar la vista
    def navegar(ruta):
        if ruta == "clientes":
            contenido.content = ClientesView(page)
        elif ruta == "canchas":
            contenido.content = CanchasView(page)
        elif ruta == "reservas":
            contenido.content = ReservasView(page)
        elif ruta == "pagos":
            contenido.content = PagosView(page)
        elif ruta == "torneos":
            contenido.content = TorneosView(page)
        elif ruta == "reportes":
            contenido.content = ReportesView(page)
        elif ruta == "costos":
            contenido.content = CostosView(page)
        page.update()

    # Menú lateral
    menu = ft.NavigationRail(
        selected_index=0,
        label_type=ft.NavigationRailLabelType.ALL,
        destinations=[
            ft.NavigationRailDestination(icon=ft.Icons.PERSON, label="Clientes"),
            ft.NavigationRailDestination(icon=ft.Icons.STADIUM, label="Canchas"),
            ft.NavigationRailDestination(icon=ft.Icons.CALENDAR_MONTH, label="Reservas"),
            ft.NavigationRailDestination(icon=ft.Icons.EMOJI_EVENTS, label="Torneos"),
            ft.NavigationRailDestination(icon=ft.Icons.PAYMENTS, label="Pagos"),
            ft.NavigationRailDestination(icon=ft.Icons.ANALYTICS, label="Reportes"),
            ft.NavigationRailDestination(icon=ft.Icons.ATTACH_MONEY, label="Costos"),
        ],
        on_change=lambda e: navegar(["clientes", "canchas", "reservas", "torneos", "pagos", "reportes", "costos"][e.control.selected_index]),
    )

    # Layout principal
    layout = ft.Row(
        controls=[
            ft.Container(content=menu, width=100, height=600, alignment=ft.alignment.top_left),
            ft.VerticalDivider(width=1),
            contenido
        ],
        expand=True,
        vertical_alignment=ft.CrossAxisAlignment.START,
        spacing=0
    )

    page.add(layout)


if __name__ == "__main__":
    ft.app(target=main)
