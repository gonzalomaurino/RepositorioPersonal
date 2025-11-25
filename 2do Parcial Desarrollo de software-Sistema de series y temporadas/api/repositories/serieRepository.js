import RepositorioBase from "./repositorioBase.js";
import Serie from "../models/serie.js";

class SerieRepository extends RepositorioBase {
  constructor() {
    super(Serie);
  }

  // Sobrescribo obtenerTodos para ordenar por título ascendente
  async obtenerTodos() {
    return this.modelo.findAll({
      order: [["titulo", "ASC"]] // usa el nombre de la propiedad, no el campo DB
    });
  }
}

export default new SerieRepository();
