import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import temporadasService from "../services/temporada.service";
import seriesService from "../services/serie.service";
import '../assets/css/temporada.css';

// ⭐ Utilidad para convertir puntuación IMDb (0-10) a estrellas (1-5)
const obtenerEstrellas = (puntuacion) => {
    if (puntuacion <= 2) return 1;
    if (puntuacion <= 4) return 2;
    if (puntuacion <= 6) return 3;
    if (puntuacion <= 8) return 4;
    return 5;
};

// 🎬 Devuelve ícono según la plataforma (usado en columna original y en columna plataforma)
const iconoPlataforma = (plataforma) => {
    switch (plataforma?.toLowerCase()) {
        case "netflix":
            return <i className="fas fa-film text-danger" title="Netflix"></i>;
        case "hbo max":
            return <i className="fas fa-tv text-purple" title="HBO Max"></i>;
        case "amc":
            return <i className="fas fa-bomb text-danger" title="AMC"></i>;
        case "nbc":
            return <i className="fas fa-feather text-warning" title="NBC"></i>;
        default:
            return <i className="fas fa-question-circle text-muted" title="Otra plataforma"></i>;
    }
};

const Temporadas = () => {
    const [temporadas, setTemporadas] = useState([]);
    const [series, setSeries] = useState([]);
    const [filtros, setFiltros] = useState({
        tituloSerie: "",
        plataforma: "",
        genero: "",
        estrellas: "", // ⭐ filtro por estrellas
        originales: "" // ✅ filtro por originalidad
    });

    const navigate = useNavigate();

    const buscar = async () => {
        try {
            const filtrosBackend = { ...filtros };
            if (filtros.estrellas) filtrosBackend.estrellas = Number(filtros.estrellas);

            if (filtros.originales === "1") {
                filtrosBackend.soloOriginales = "true";
            }
            const data = await temporadasService.buscarFiltrado(filtrosBackend);
            setTemporadas(data);
        } catch (error) {
            alert("Ocurrió un error al buscar las temporadas.");
        }
    };

    const limpiar = async () => {
        setFiltros({
            tituloSerie: "",
            plataforma: "",
            genero: "",
            estrellas: "",
            originales: ""
        });
        const data = await temporadasService.buscarFiltrado({});
        setTemporadas(data);
    };

    const cargarSeries = async () => {
        const data = await seriesService.obtenerTodas();
        setSeries(data);
    };

    const eliminar = async (id) => {
        if (confirm("¿Seguro que deseas eliminar esta temporada?")) {
            await temporadasService.eliminar(id);
            buscar();
        }
    };

    useEffect(() => {
        cargarSeries();
        buscar();
    }, []);

    return (
        <div className="container my-4">
            <h2 className="mb-4">Listado de Temporadas</h2>

            {/* 🎯 Filtros */}
            <form className="row g-3 mb-4">
                <div className="col-md-4">
                    <input
                        type="text"
                        className="form-control"
                        placeholder="Buscar por título de serie"
                        value={filtros.tituloSerie}
                        onChange={(e) => setFiltros({ ...filtros, tituloSerie: e.target.value })}
                    />
                </div>
                <div className="col-md-4">
                    <input
                        type="text"
                        className="form-control"
                        placeholder="Buscar por plataforma"
                        value={filtros.plataforma}
                        onChange={(e) => setFiltros({ ...filtros, plataforma: e.target.value })}
                    />
                </div>
                <div className="col-md-4">
                    <input
                        type="text"
                        className="form-control"
                        placeholder="Buscar por género"
                        value={filtros.genero}
                        onChange={(e) => setFiltros({ ...filtros, genero: e.target.value })}
                    />
                </div>

                {/* ⭐ Filtro de estrellas */}
                <div className="col-md-4">
                    <label>Puntuación mínima (estrellas)</label>
                    <select
                        className="form-select"
                        value={filtros.estrellas}
                        onChange={(e) => setFiltros({ ...filtros, estrellas: e.target.value })}
                    >
                        <option value="">Todas</option>
                        {[1, 2, 3, 4, 5].map((s) => (
                            <option key={s} value={s}>{s} estrella{s > 1 ? 's' : ''} o más</option>
                        ))}
                    </select>
                </div>

                {/* ✅ Filtro de originalidad */}
                <div className="col-md-4">
                    <label>¿Solo originales?</label>
                    <select
                        className="form-select"
                        value={filtros.originales}
                        onChange={(e) => setFiltros({ ...filtros, originales: e.target.value })}
                    >
                        <option value="">Todas</option>
                        <option value="1">Solo originales</option>
                        <option value="0">Todas (incluye no originales)</option>
                    </select>
                </div>

                <div className="col-12 d-flex justify-content-end gap-2">
                    <button type="button" className="btn btn-primary" onClick={buscar}>
                        Filtrar
                    </button>
                    <button type="button" className="btn btn-secondary" onClick={limpiar}>
                        Limpiar
                    </button>
                </div>
            </form>

            {/* 📋 Tabla de resultados */}
            <table className="table table-striped table-bordered align-middle">
                <thead className="table-dark">
                    <tr>
                        <th>🌟</th>
                        <th>Serie</th>
                        <th>Plataforma</th>
                        <th>Temporada N°</th>
                        <th>Episodios</th>
                        <th>Año Estreno</th>
                        <th>Género</th>
                        <th>Creador</th>
                        <th>Puntuación</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    {temporadas.map((temp) => (
                        <tr key={temp.id}>
                            {/* Columna ícono plataforma solo si es original */}
                            <td className="text-center">
                                {temp.esOriginal ? iconoPlataforma(temp.serie?.plataforma) : null}
                            </td>

                            <td>{temp.serie?.titulo}</td>

                            {/* Columna plataforma con icono */}
                            <td className="text-center">
                                {iconoPlataforma(temp.serie?.plataforma)}
                            </td>

                            <td>{temp.numero}</td>
                            <td>{temp.episodios}</td>
                            <td>{temp.anioEstreno}</td>
                            <td>{temp.genero}</td>
                            <td>{temp.creador}</td>

                            {/* Puntuación con estrellas */}
                            <td>
                                {Array.from({ length: obtenerEstrellas(temp.puntuacionIMDb) }, (_, i) => (
                                    <i key={i} className="fas fa-star text-warning"></i>
                                ))}
                            </td>

                            <td className="text-nowrap">
                                <button
                                    className="btn btn-sm btn-outline-primary me-2"
                                    onClick={() => navigate(`/temporadas/editar/${temp.id}`)}
                                >
                                    Editar
                                </button>
                                <button
                                    className="btn btn-sm btn-outline-danger"
                                    onClick={() => eliminar(temp.id)}
                                >
                                    Eliminar
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default Temporadas;
