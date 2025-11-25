import temporadaRepository from "../repositories/temporadaRepository.js";
import serieRepository from "../repositories/serieRepository.js"; // por si querés validar la existencia de la serie

class TemporadaService {

    async obtenerTodos({ pagina = 1, limite = 10 } = {}) {
        const temporadas = await temporadaRepository.obtenerTodos({ pagina, limite });
        return temporadas.map(this.#convertirSalida);
    }

    async obtenerPorId(id) {
        const temporada = await temporadaRepository.obtenerPorId(id);
        return temporada ? this.#convertirSalida(temporada) : null;
    }

    async crear(datos) {
        await this.#validar(datos);
        const creada = await temporadaRepository.crear(datos);
        return this.#convertirSalida(creada);
    }

    async actualizar(id, datos) {
        await this.#validar(datos, id);
        const actualizada = await temporadaRepository.actualizar(id, datos);
        return this.#convertirSalida(actualizada);
    }

    async eliminar(id) {
        return await temporadaRepository.eliminar(id);
    }

    async contarTodos() {
        return await temporadaRepository.contarTodos();
    }

    #convertirSalida(temporada) {
        return temporada.toJSON();
    }

    // services/temporadaService.js
    async #validar(datos, idActual = null) {
        const anioActual = new Date().getFullYear();

        // Año entre 1900 y actual
        if (datos.anioEstreno < 1900 || datos.anioEstreno > anioActual) {
            throw new Error(`El año de estreno debe estar entre 1900 y ${anioActual}.`);
        }

        // Episodios > 0
        if (datos.episodios <= 0) {
            throw new Error("El número de episodios debe ser mayor a cero.");
        }

        // Validar que puntuacionIMDb esté entre 0 y 10
        if (
            typeof datos.puntuacionIMDb !== "undefined" &&
            (datos.puntuacionIMDb < 0 || datos.puntuacionIMDb > 10)
        ) {
            throw new Error("La puntuación IMDb debe estar entre 0 y 10.");
        }

        // Validar que no se repita el número de temporada para la misma serie
        const repetida = await temporadaRepository.existeNumeroTemporada(datos.idSerie, datos.numero, idActual);
        if (repetida) {
            throw new Error(`Ya existe la temporada N°${datos.numero} para esta serie.`);
        }
    }


    async buscarFiltrado(filtros) {
        const resultados = await temporadaRepository.buscarFiltrado(filtros);
        return resultados.map(this.#convertirSalida);
    }
}

export default new TemporadaService();
