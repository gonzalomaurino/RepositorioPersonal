import flet as ft
from services.cliente_service import ClienteService



class ClientesView(ft.Column):
    """Vista de gestión de clientes (ABMC)."""

    def __init__(self, page: ft.Page):
        super().__init__(expand=True, scroll="adaptive")
        self.page = page
        self.alinear_contenido()

    def alinear_contenido(self):
        # Campos de entrada
        self.id_cliente_editando = None  # Para saber si estamos editando
        self.nombre = ft.TextField(label="Nombre", width=200)
        self.apellido = ft.TextField(label="Apellido", width=200)
        self.telefono = ft.TextField(label="Teléfono", width=200)
        self.email = ft.TextField(label="Email", width=250)

        # Botón guardar
        self.btn_guardar = ft.ElevatedButton(
            "Registrar cliente", icon=ft.Icons.SAVE, on_click=self.guardar_cliente
        )

        # Tabla de clientes
        self.tabla = ft.DataTable(
            columns=[
                ft.DataColumn(ft.Text("ID")),
                ft.DataColumn(ft.Text("Nombre")),
                ft.DataColumn(ft.Text("Apellido")),
                ft.DataColumn(ft.Text("Teléfono")),
                ft.DataColumn(ft.Text("Email")),
                ft.DataColumn(ft.Text("Acciones")),
            ],
            rows=[],
        )

        self.refrescar_tabla()

        self.controls = [
            ft.Text("Gestión de Clientes", size=20, weight=ft.FontWeight.BOLD),
            ft.Row([self.nombre, self.apellido, self.telefono, self.email, self.btn_guardar], spacing=10),
            ft.Divider(height=5),
            self.tabla,
        ]
        self.spacing = 15
        self.tight = True

    def guardar_cliente(self, e):
        """Guarda o actualiza un cliente según si estamos editando."""
        try:
            # Validar que los campos no estén vacíos
            if not self.nombre.value or not self.nombre.value.strip():
                self.mostrar_error("❌ El campo NOMBRE es obligatorio")
                return
            if not self.apellido.value or not self.apellido.value.strip():
                self.mostrar_error("❌ El campo APELLIDO es obligatorio")
                return
            if not self.telefono.value or not self.telefono.value.strip():
                self.mostrar_error("❌ El campo TELÉFONO es obligatorio")
                return
            if not self.email.value or not self.email.value.strip():
                self.mostrar_error("❌ El campo EMAIL es obligatorio")
                return
            
            # Validar formato de teléfono (solo números, espacios, guiones y paréntesis)
            telefono = self.telefono.value.strip()
            telefono_limpio = telefono.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
            
            if not telefono_limpio.isdigit():
                self.mostrar_error("❌ El TELÉFONO solo debe contener números (ej: 1234567890, 123-456-7890)")
                return
            
            if len(telefono_limpio) < 8 or len(telefono_limpio) > 15:
                self.mostrar_error("❌ El TELÉFONO debe tener entre 8 y 15 dígitos")
                return
            
            if self.id_cliente_editando:
                # Modo edición
                ClienteService.modificar_cliente(
                    self.id_cliente_editando,
                    self.nombre.value,
                    self.apellido.value,
                    self.telefono.value,
                    self.email.value,
                )
                self.mostrar_exito("✅ Cliente modificado correctamente")
                self.id_cliente_editando = None
                self.btn_guardar.text = "Registrar cliente"
                self.btn_guardar.icon = ft.Icons.SAVE
            else:
                # Modo creación
                ClienteService.registrar_cliente(
                    self.nombre.value,
                    self.apellido.value,
                    self.telefono.value,
                    self.email.value,
                )
                self.mostrar_exito("✅ Cliente registrado correctamente")
            
            # Limpiar campos
            self.nombre.value = ""
            self.apellido.value = ""
            self.telefono.value = ""
            self.email.value = ""
            
            self.page.update()
            self.refrescar_tabla()
        except ValueError as err:
            self.mostrar_error(str(err))
        except Exception as err:
            self.mostrar_error(f"❌ Error: {err}")

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

    def registrar_cliente(self, e):
        """Método legacy - redirige a guardar_cliente."""
        self.guardar_cliente(e)

    def refrescar_tabla(self):
        clientes = ClienteService.listar_clientes()
        self.tabla.rows.clear()
        for c in clientes:
            fila = ft.DataRow(
                cells=[
                    ft.DataCell(ft.Text(str(c.id_cliente))),
                    ft.DataCell(ft.Text(c.nombre)),
                    ft.DataCell(ft.Text(c.apellido)),
                    ft.DataCell(ft.Text(c.telefono)),
                    ft.DataCell(ft.Text(c.email)),
                    ft.DataCell(
                        ft.Row([
                            ft.IconButton(
                                icon=ft.Icons.EDIT, tooltip="Editar",
                                icon_color=ft.Colors.BLUE,
                                on_click=lambda e, cliente=c: self.editar_cliente(cliente),
                            ),
                            ft.IconButton(
                                icon=ft.Icons.DELETE, tooltip="Eliminar",
                                icon_color=ft.Colors.RED,
                                on_click=lambda e, id=c.id_cliente: self.eliminar_cliente(id),
                            ),
                        ])
                    ),
                ]
            )
            self.tabla.rows.append(fila)
        self.page.update()

    def editar_cliente(self, cliente):
        """Carga los datos del cliente en el formulario para edición."""
        self.id_cliente_editando = cliente.id_cliente
        self.nombre.value = cliente.nombre
        self.apellido.value = cliente.apellido
        self.telefono.value = cliente.telefono
        self.email.value = cliente.email
        self.btn_guardar.text = "Actualizar cliente"
        self.btn_guardar.icon = ft.Icons.UPDATE
        self.page.update()

    def eliminar_cliente(self, id_cliente):
        """Elimina un cliente y opcionalmente sus reservas."""
        try:
            from dao.conexion import obtener_conexion
            from dao.reserva_dao import ReservaDAO
            
            # Verificar si el cliente tiene reservas
            conexion = obtener_conexion()
            cursor = conexion.cursor()
            cursor.execute("SELECT id_reserva FROM reserva WHERE id_cliente = ?", (id_cliente,))
            reservas = cursor.fetchall()
            conexion.close()
            
            if reservas:
                # Verificar si alguna reserva tiene pagos
                conexion = obtener_conexion()
                cursor = conexion.cursor()
                cursor.execute("""
                    SELECT COUNT(*) FROM reserva r
                    INNER JOIN pago p ON r.id_reserva = p.id_reserva
                    WHERE r.id_cliente = ? AND LOWER(r.estado) IN ('confirmada', 'seña')
                """, (id_cliente,))
                reservas_con_pagos = cursor.fetchone()[0]
                conexion.close()
                
                if reservas_con_pagos > 0:
                    self.mostrar_advertencia(
                        f"⚠️ No se puede eliminar este cliente. "
                        f"Tiene {reservas_con_pagos} reserva(s) confirmada(s) o en seña con pagos. "
                        f"Primero debes cancelar esas reservas."
                    )
                    return
                
                # Crear diálogo de confirmación
                def confirmar_eliminacion(e):
                    try:
                        # Eliminar todas las reservas del cliente (esto eliminará pagos en cascada)
                        for reserva in reservas:
                            ReservaDAO.eliminar(reserva[0])
                        
                        # Ahora eliminar el cliente
                        ClienteService.eliminar_cliente(id_cliente)
                        
                        self.mostrar_exito(f"✅ Cliente y sus {len(reservas)} reserva(s) eliminadas correctamente")
                        dialog.open = False
                        self.page.update()
                        self.refrescar_tabla()
                    except ValueError as err:
                        # Error de validación (reserva confirmada con pagos)
                        self.mostrar_advertencia(str(err))
                        dialog.open = False
                        self.page.update()
                    except Exception as err:
                        self.mostrar_error(f"❌ Error: {err}")
                        dialog.open = False
                        self.page.update()
                
                def cancelar_eliminacion(e):
                    dialog.open = False
                    self.page.update()
                
                dialog = ft.AlertDialog(
                    modal=True,
                    title=ft.Text("⚠️ Confirmar eliminación"),
                    content=ft.Text(
                        f"Este cliente tiene {len(reservas)} reserva(s) asociada(s).\n\n"
                        "¿Deseas eliminar el cliente y TODAS sus reservas (incluyendo pagos)?"
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
                # Si no tiene reservas, eliminar directamente
                ClienteService.eliminar_cliente(id_cliente)
                
                self.mostrar_exito(f"✅ Cliente eliminado correctamente (ID {id_cliente})")
                self.refrescar_tabla()
            
        except ValueError as ve:
            self.mostrar_advertencia(str(ve))
            
        except Exception as err:
            self.mostrar_error(f"❌ Error al eliminar: {err}")
