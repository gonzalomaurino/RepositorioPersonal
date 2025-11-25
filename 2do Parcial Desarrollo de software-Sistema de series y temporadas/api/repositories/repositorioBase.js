export default class RepositorioBase {
  constructor(modelo) {
    this.modelo = modelo;
  }

  async obtenerTodos({ pagina = 1, limite = 10 } = {}) {
    const offset = (pagina - 1) * limite;
    return this.modelo.findAll({ limit: limite, offset });
  }

  async obtenerPorId(id) {
    return this.modelo.findByPk(id);
  }

  async crear(datos) {
    return this.modelo.create(datos);
  }

  async actualizar(id, datos) {
    const instancia = await this.modelo.findByPk(id);
    if (!instancia) throw new Error(`Error: Instancia con id: ${id} no encontrada`);

    return instancia.update(datos);
  }

  async eliminar(id) {
    const instancia = await this.modelo.findByPk(id);
    if (!instancia) throw new Error(`Error: Instancia con id: ${id} no encontrada`);
    await instancia.destroy();
    return instancia;
  }

  async contarTodos() {
    return this.modelo.count();
  }
}
