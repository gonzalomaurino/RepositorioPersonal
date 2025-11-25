import axios from "./axios.config.js";

const buscarFiltrado = async (filtros) => {
    try {
        const params = new URLSearchParams(filtros).toString();
        const response = await axios.get(`/temporadas/filtrar?${params}`);
        return response.data;
    } catch (error) {
        console.error("Error al buscar temporadas:", error);
        throw error;
    }
};

const eliminar = async (id) => {
    try {
        await axios.delete(`/temporadas/${id}`);
    } catch (error) {
        console.error("Error al eliminar temporada:", error);
        throw error;
    }
};

const obtenerPorId = async (id) => {
    try {
        const response = await axios.get(`/temporadas/${id}`);
        return response.data;
    } catch (error) {
        console.error("Error al obtener temporada por ID:", error);
        throw error;
    }
};

const crear = async (temporada) => {
    try {
        const response = await axios.post("/temporadas", temporada);
        return response.data;
    } catch (error) {
        console.error("Error al crear temporada:", error);
        throw error;
    }
};

const actualizar = async (id, temporada) => {
    try {
        const response = await axios.put(`/temporadas/${id}`, temporada);
        return response.data;
    } catch (error) {
        console.error("Error al actualizar temporada:", error);
        throw error;
    }
};

export default {
    buscarFiltrado,
    eliminar,
    obtenerPorId,
    crear,
    actualizar
};
