import sqlite3
from dao.conexion import obtener_conexion
from models.cliente import Cliente


class ClienteDAO:
    """DAO para la entidad Cliente: CRUD completo"""

    @staticmethod
    def crear(cliente: Cliente):
        conexion = obtener_conexion()
        try:
            conexion.execute(
                """
                INSERT INTO cliente (nombre, apellido, telefono, email)
                VALUES (?, ?, ?, ?)
                """,
                (cliente.nombre, cliente.apellido, cliente.telefono, cliente.email),
            )
            conexion.commit()
        except sqlite3.IntegrityError as e:
            print(f"❌ Error de integridad: {e}")
            raise ValueError("Email duplicado o datos inválidos.")
        except sqlite3.Error as e:
            print(f"❌ Error al crear cliente: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def obtener_todos():
        conexion = obtener_conexion()
        clientes = []
        try:
            cursor = conexion.execute("SELECT * FROM cliente")
            for fila in cursor.fetchall():
                clientes.append(Cliente(*fila))
        except sqlite3.Error as e:
            print(f"❌ Error al obtener clientes: {e}")
        finally:
            conexion.close()
        return clientes

    @staticmethod
    def actualizar(cliente: Cliente):
        """Actualiza los datos de un cliente existente."""
        conexion = obtener_conexion()
        try:
            conexion.execute(
                """
                UPDATE cliente 
                SET nombre = ?, apellido = ?, telefono = ?, email = ?
                WHERE id_cliente = ?
                """,
                (cliente.nombre, cliente.apellido, cliente.telefono, cliente.email, cliente.id_cliente),
            )
            conexion.commit()
            print(f"✅ Cliente actualizado correctamente (ID {cliente.id_cliente})")
        except sqlite3.IntegrityError as e:
            print(f"❌ Error de integridad: {e}")
            raise ValueError("Email duplicado o datos inválidos.")
        except sqlite3.Error as e:
            print(f"❌ Error al actualizar cliente: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()

    @staticmethod
    def eliminar(id_cliente):
        from dao.conexion import resetear_autoincrement
        
        conexion = obtener_conexion()
        try:
            conexion.execute("DELETE FROM cliente WHERE id_cliente = ?", (id_cliente,))
            conexion.commit()
            print(f"✅ Cliente eliminado correctamente (ID {id_cliente})")
            
            # Resetear el autoincrement
            resetear_autoincrement("cliente")
        except sqlite3.IntegrityError as e:
            print(f"❌ Error de integridad: {e}")
            raise ValueError("No se puede eliminar: este cliente tiene reservas asociadas.")
        except sqlite3.Error as e:
            print(f"❌ Error al eliminar cliente: {e}")
            raise Exception(f"Error en BD: {e}")
        finally:
            conexion.close()
