import RepositorioBase from "./repositorioBase.js";
import Temporada from "../models/temporada.js";
import Serie from "../models/Serie.js";
import { Op } from "sequelize";

class TemporadaRepository extends RepositorioBase {
  constructor() {
    super(Temporada);
  }

  // Trae todas las temporadas con los datos de su serie (paginado)
  async obtenerTodos({ pagina = 1, limite = 10 } = {}) {
    const offset = (pagina - 1) * limite;
    return this.modelo.findAll({
      order: [["id", "DESC"]],
      limit: limite,
      offset,
      include: { model: Serie, as: "serie" },
    });
  }

  // Trae una temporada por ID con la serie incluida
  async obtenerPorId(id) {
    return this.modelo.findByPk(id, {
      include: { model: Serie, as: "serie" },
    });
  }

  // Verifica si ya existe una temporada con ese número para esa serie (para validar unicidad)

  async existeNumeroTemporada(idSerie, numero, idIgnorar = null) {
    const where = {
      idSerie,
      numero,
    };

    if (idIgnorar) {
      where.id = { [Op.ne]: idIgnorar };
    }

    const existente = await this.modelo.findOne({ where });
    return !!existente;
  }

  //

  async buscarFiltrado({ tituloSerie, plataforma, genero, estrellas, soloOriginales } = {}) {
    const condicionesTemporada = [];
    const condicionesSerie = [];

    // 🎬 Filtro por género
    if (genero?.trim()) {
      condicionesTemporada.push({
        genero: { [Op.like]: `%${genero.trim()}%` },
      });
    }

    // 🎯 Filtro por estrellas
    if (estrellas) {
      const minPuntaje = Math.max((parseInt(estrellas) - 1) * 2, 0); // 1 estrella = 0-2, 2 = >2-4, etc.
      condicionesTemporada.push({
        puntuacionIMDb: { [Op.gte]: minPuntaje },
      });
    }

    // 🟢 Filtro por originalidad
    if (soloOriginales === "true") {
      condicionesTemporada.push({ esOriginal: 1 });
    }

    // 🎞️ Filtro por serie (nombre y plataforma)
    if (tituloSerie?.trim()) {
      condicionesSerie.push({
        titulo: { [Op.like]: `%${tituloSerie.trim()}%` },
      });
    }

    if (plataforma?.trim()) {
      condicionesSerie.push({
        plataforma: plataforma.trim(),
      });
    }

    const sinFiltros = condicionesTemporada.length === 0 && condicionesSerie.length === 0;

    const includeSerie = {
      model: Serie,
      as: "serie",
      required: condicionesSerie.length > 0,
      ...(condicionesSerie.length > 0 && {
        where: { [Op.and]: condicionesSerie },
      }),
    };

    return this.modelo.findAll({
      ...(condicionesTemporada.length > 0 && {
        where: { [Op.and]: condicionesTemporada },
      }),
      include: includeSerie,
      order: sinFiltros
        ? [["id", "DESC"]]
        : [
          [{ model: Serie, as: "serie" }, "titulo", "ASC"],
          ["numero", "ASC"],
        ],
      limit: 50,
    });
  }
}

export default new TemporadaRepository();
