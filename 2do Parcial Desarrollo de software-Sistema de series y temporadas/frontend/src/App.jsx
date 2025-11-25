import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Temporadas from "./pages/Temporada";
import FormularioTemporada from "./pages/FormularioTemporada";
import Encabezado from "./components/Encabezado";
import PiePagina from "./components/PieDePagina";

function App() {
  return (
    <BrowserRouter>
      <div className="container-fluid mt-4 px-4">
        <Encabezado />

        <Routes>
          {/* Redirección inicial */}
          <Route path="/" element={<Navigate to="/temporadas" />} />

          {/* Listado de temporadas */}
          <Route path="/temporadas" element={<Temporadas />} />

          {/* Crear nueva temporada */}
          <Route path="/temporadas/nueva" element={<FormularioTemporada />} />

          {/* Editar temporada existente */}
          <Route path="/temporadas/editar/:id" element={<FormularioTemporada />} />
        </Routes>

        <PiePagina />
      </div>
    </BrowserRouter>
  );
}

export default App;
