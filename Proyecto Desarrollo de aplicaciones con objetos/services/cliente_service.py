from dao.cliente_dao import ClienteDAO
from models.cliente import Cliente

class ClienteService:
    """Lógica de negocio asociada a los clientes."""

    @staticmethod
    def registrar_cliente(nombre, apellido, telefono, email):
        # Verificar que el email no esté duplicado
        clientes_existentes = ClienteDAO.obtener_todos()
        for cliente in clientes_existentes:
            if cliente.email.lower() == email.lower():
                raise ValueError("Ya existe un cliente con ese email.")
        
        nuevo = Cliente(None, nombre, apellido, telefono, email)
        if not nuevo.es_valido():
            raise ValueError("Datos inválidos del cliente.")
        ClienteDAO.crear(nuevo)
        print(f"✅ Cliente registrado: {nuevo}")

    @staticmethod
    def listar_clientes():
        return ClienteDAO.obtener_todos()

    @staticmethod
    def modificar_cliente(id_cliente, nombre, apellido, telefono, email):
        """Modifica los datos de un cliente existente."""
        # Verificar que el email no esté duplicado (excepto si es el mismo cliente)
        clientes_existentes = ClienteDAO.obtener_todos()
        for cliente in clientes_existentes:
            if cliente.email.lower() == email.lower() and cliente.id_cliente != id_cliente:
                raise ValueError("Ya existe otro cliente con ese email.")
        
        cliente_actualizado = Cliente(id_cliente, nombre, apellido, telefono, email)
        if not cliente_actualizado.es_valido():
            raise ValueError("Datos inválidos del cliente.")
        ClienteDAO.actualizar(cliente_actualizado)
        print(f"✅ Cliente modificado: {cliente_actualizado}")

    @staticmethod
    def eliminar_cliente(id_cliente):
        ClienteDAO.eliminar(id_cliente)
        print(f"🗑️ Cliente eliminado (ID {id_cliente})")
