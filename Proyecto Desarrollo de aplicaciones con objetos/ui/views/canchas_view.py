import flet as ft
from services.cancha_service import CanchaService


class CanchasView(ft.Column):
    """Vista de gestión de canchas deportivas (ABMC)."""

    def __init__(self, page: ft.Page):
        super().__init__(expand=True, scroll="adaptive", spacing=15)
        self.page = page
        self.crear_ui()

    def crear_ui(self):
        # --- Campos de entrada ---
        self.id_cancha_editando = None  # Para saber si estamos editando
        self.nombre = ft.TextField(label="Nombre", width=180)
        self.tipo_superficie = ft.TextField(label="Tipo de superficie", width=180)
        self.precio_hora = ft.TextField(label="Precio por hora ($)", width=150)
        self.servicios = ft.TextField(label="Servicios (opcional)", width=200)

        self.iluminacion = ft.Dropdown(
            label="Iluminación",
            width=150,
            options=[
                ft.dropdown.Option("Sí"),
                ft.dropdown.Option("No"),
            ],
            value="No"
        )

        # --- Botón guardar ---
        self.btn_guardar = ft.ElevatedButton(
            "Registrar cancha", icon=ft.Icons.SAVE, on_click=self.guardar_cancha
        )

        # --- Tabla de canchas ---
        self.tabla = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("ID")),
                ft.DataColumn(ft.Text("Nombre")),
                ft.DataColumn(ft.Text("Superficie")),
                ft.DataColumn(ft.Text("Iluminación")),
                ft.DataColumn(ft.Text("Precio/Hora")),
                ft.DataColumn(ft.Text("Servicios")),
                ft.DataColumn(ft.Text("Acciones")),
            ],
            rows=[],
        )

        # --- Cargar canchas ---
        self.refrescar_tabla()

        # --- Layout visual ---
        self.controls = [
            ft.Text("Gestión de Canchas", size=20, weight=ft.FontWeight.BOLD),
            ft.Row(
                [
                    self.nombre,
                    self.tipo_superficie,
                    self.iluminacion,
                    self.precio_hora,
                    self.servicios,
                    self.btn_guardar,
                ],
                wrap=True,
                spacing=10
            ),
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

    def guardar_cancha(self, e):
        """Guarda o actualiza una cancha según si estamos editando."""
        try:
            # Validaciones
            if not self.nombre.value or not self.nombre.value.strip():
                self.mostrar_error("❌ El campo NOMBRE es obligatorio")
                return
            if not self.tipo_superficie.value or not self.tipo_superficie.value.strip():
                self.mostrar_error("❌ El campo TIPO DE SUPERFICIE es obligatorio")
                return
            if not self.precio_hora.value or not self.precio_hora.value.strip():
                self.mostrar_error("❌ El campo PRECIO POR HORA es obligatorio")
                return
            
            nombre = self.nombre.value.strip()
            tipo = self.tipo_superficie.value.strip()
            iluminacion = self.iluminacion.value == "Sí"
            
            try:
                precio = float(self.precio_hora.value)
                if precio <= 0:
                    self.mostrar_error("❌ El PRECIO debe ser mayor a cero")
                    return
            except ValueError:
                self.mostrar_error("❌ El PRECIO debe ser un número válido")
                return
            
            servicios = self.servicios.value.strip() if self.servicios.value else ""

            if self.id_cancha_editando:
                # Modo edición
                CanchaService.modificar_cancha(self.id_cancha_editando, nombre, tipo, iluminacion, precio, servicios)
                self.mostrar_exito("✅ Cancha modificada correctamente")
                self.id_cancha_editando = None
                self.btn_guardar.text = "Registrar cancha"
                self.btn_guardar.icon = ft.Icons.SAVE
            else:
                # Modo creación
                CanchaService.registrar_cancha(nombre, tipo, iluminacion, precio, servicios)
                self.mostrar_exito("✅ Cancha registrada correctamente")

            self.refrescar_tabla()

            # Limpiar campos
            self.nombre.value = ""
            self.tipo_superficie.value = ""
            self.precio_hora.value = ""
            self.servicios.value = ""
            self.iluminacion.value = "No"
            self.page.update()

        except ValueError as err:
            self.mostrar_error(str(err))
        except Exception as err:
            self.mostrar_error(f"❌ Error: {err}")

    def registrar_cancha(self, e):
        """Método legacy - redirige a guardar_cancha."""
        self.guardar_cancha(e)

    def refrescar_tabla(self):
        canchas = CanchaService.listar_canchas()
        self.tabla.rows.clear()
        for c in canchas:
            fila = ft.DataRow(
                cells=[
                    ft.DataCell(ft.Text(str(c.id_cancha))),
                    ft.DataCell(ft.Text(c.nombre)),
                    ft.DataCell(ft.Text(c.tipo_superficie)),
                    ft.DataCell(ft.Text("Sí" if c.iluminacion else "No")),
                    ft.DataCell(ft.Text(f"${c.precio_hora:.2f}")),
                    ft.DataCell(ft.Text(c.servicios or "-")),
                    ft.DataCell(
                        ft.Row([
                            ft.IconButton(
                                icon=ft.Icons.EDIT,
                                tooltip="Editar",
                                icon_color=ft.Colors.BLUE,
                                on_click=lambda e, cancha=c: self.editar_cancha(cancha),
                            ),
                            ft.IconButton(
                                icon=ft.Icons.DELETE,
                                icon_color=ft.Colors.RED,
                                tooltip="Eliminar",
                                on_click=lambda e, id=c.id_cancha: self.eliminar_cancha(id),
                            ),
                        ])
                    ),
                ]
            )
            self.tabla.rows.append(fila)
        self.page.update()

    def editar_cancha(self, cancha):
        """Carga los datos de la cancha en el formulario para edición."""
        self.id_cancha_editando = cancha.id_cancha
        self.nombre.value = cancha.nombre
        self.tipo_superficie.value = cancha.tipo_superficie
        self.iluminacion.value = "Sí" if cancha.iluminacion else "No"
        self.precio_hora.value = str(cancha.precio_hora)
        self.servicios.value = cancha.servicios or ""
        self.btn_guardar.text = "Actualizar cancha"
        self.btn_guardar.icon = ft.Icons.UPDATE
        self.page.update()

    def eliminar_cancha(self, id_cancha):
        """Elimina una cancha verificando que no tenga reservas confirmadas con pagos."""
        try:
            from dao.cancha_dao import CanchaDAO
            from dao.conexion import obtener_conexion
            
            # Obtener nombre de la cancha
            conexion = obtener_conexion()
            cursor = conexion.cursor()
            cursor.execute("SELECT nombre FROM cancha WHERE id_cancha = ?", (id_cancha,))
            cancha_info = cursor.fetchone()
            nombre_cancha = cancha_info[0] if cancha_info else "Desconocida"
            
            # Verificar si tiene reservas confirmadas con pagos
            cursor.execute("""
                SELECT COUNT(*) FROM reserva r
                INNER JOIN pago p ON r.id_reserva = p.id_reserva
                WHERE r.id_cancha = ? AND LOWER(r.estado) = 'confirmada'
            """, (id_cancha,))
            reservas_confirmadas = cursor.fetchone()[0]
            
            # Contar total de reservas
            cursor.execute("SELECT COUNT(*) FROM reserva WHERE id_cancha = ?", (id_cancha,))
            total_reservas = cursor.fetchone()[0]
            conexion.close()
            
            if reservas_confirmadas > 0:
                self.mostrar_advertencia(
                    f"⚠️ No se puede eliminar la cancha '{nombre_cancha}'. "
                    f"Tiene {reservas_confirmadas} reserva(s) confirmada(s) con pagos."
                )
                return
            
            if total_reservas > 0:
                # Tiene reservas pero no confirmadas - mostrar diálogo de confirmación
                def confirmar_eliminacion(e):
                    try:
                        CanchaDAO.eliminar(id_cancha)
                        self.mostrar_exito(f"✅ Cancha '{nombre_cancha}' y sus {total_reservas} reserva(s) eliminadas correctamente")
                        dialog.open = False
                        self.page.update()
                        self.refrescar_tabla()
                    except Exception as err:
                        self.mostrar_error(f"❌ Error al eliminar: {err}")
                        dialog.open = False
                        self.page.update()
                
                def cancelar_eliminacion(e):
                    dialog.open = False
                    self.page.update()
                
                dialog = ft.AlertDialog(
                    modal=True,
                    title=ft.Text("⚠️ Confirmar eliminación"),
                    content=ft.Text(
                        f"La cancha '{nombre_cancha}' tiene {total_reservas} reserva(s) asociada(s).\n\n"
                        "¿Deseas eliminar la cancha y TODAS sus reservas?"
                    ),
                    actions=[
                        ft.TextButton("Cancelar", on_click=cancelar_eliminacion),
                        ft.TextButton("Eliminar todo", on_click=confirmar_eliminacion),
                    ],
                    actions_alignment=ft.MainAxisAlignment.END,
                )
                
                self.page.dialog = dialog
                dialog.open = True
                self.page.update()
            else:
                # No tiene reservas, eliminar directamente
                CanchaDAO.eliminar(id_cancha)
                self.mostrar_exito(f"✅ Cancha '{nombre_cancha}' eliminada correctamente")
                self.refrescar_tabla()
            
        except ValueError as err:
            self.mostrar_advertencia(str(err))
        except Exception as e:
            self.mostrar_error(f"❌ Error al eliminar: {e}")

