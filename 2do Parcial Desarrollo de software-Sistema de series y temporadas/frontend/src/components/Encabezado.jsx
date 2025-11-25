import { NavLink } from "react-router-dom";

export default function Encabezado() {
  return (
    <header className="bg-dark text-white py-3 mb-4">
      <div className="container py-5 h-100 position-relative">
        <div className="usuario-pill">
          <i className="bi bi-person-circle"></i>
          <span>Gonzalo Maurino Medina</span>
        </div>
        <nav className="mt-3">
          <ul className="nav nav-pills nav-fill gap-2 p-1 small bg-primary rounded-5 shadow-sm justify-content-center">
            <li className="nav-item">
              <NavLink
                to="/temporadas"
                className={({ isActive }) =>
                  "nav-link rounded-5" + (isActive ? " active text-white" : " text-white")
                }
              >
                Listado de Temporadas
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink
                to="/temporadas/nueva"
                className={({ isActive }) =>
                  "nav-link rounded-5" + (isActive ? " active text-white" : " text-white")
                }
              >
                Nueva Temporada
              </NavLink>
            </li>
          </ul>
        </nav>
      </div>
    </header>
  );
}
