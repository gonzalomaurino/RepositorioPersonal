import axios from "./axios.config.js";

const obtenerTodas = async () => {
  const response = await axios.get("/series");
  return response.data;
};

export default {
  obtenerTodas
};
