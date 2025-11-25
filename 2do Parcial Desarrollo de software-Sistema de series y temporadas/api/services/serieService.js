import serieRepository from "../repositories/serieRepository.js";

class SerieService {
  async obtenerTodas() {
    return await serieRepository.obtenerTodos();
  }
}

export default new SerieService();
