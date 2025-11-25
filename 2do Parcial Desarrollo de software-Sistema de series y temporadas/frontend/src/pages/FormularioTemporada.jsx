import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate, useParams } from "react-router";
import seriesService from "../services/serie.service";
import temporadasService from "../services/temporada.service";

const FormularioTemporada = () => {
  const [series, setSeries] = useState([]);
  const { id } = useParams();
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm();

  useEffect(() => {
    seriesService.obtenerTodas().then(setSeries);
    if (id) {
      temporadasService.obtenerPorId(id).then((temporada) => {
        // Ajustamos valores booleanos para los inputs
        reset({
          ...temporada,
          esOriginal: Boolean(temporada.esOriginal),
        });
      });
    }
  }, [id, reset]);

  const onSubmit = async (data) => {
    // 🧠 Conversión de datos a tipos esperados por el backend
    data.idSerie = Number(data.idSerie);
    data.numero = Number(data.numero);
    data.episodios = Number(data.episodios);
    data.anioEstreno = Number(data.anioEstreno);
    data.puntuacionIMDb = Number(data.puntuacionIMDb);
    data.esOriginal = !!data.esOriginal;

    try {
      if (id) {
        await temporadasService.actualizar(id, data);
      } else {
        await temporadasService.crear(data);
      }
      navigate("/temporadas");
    } catch (err) {
      alert(err.response?.data?.error || "Error al guardar la temporada");
    }
  };

  return (
    <main className="container mt-5">
      <h3 className="mb-4">{id ? "Editar Temporada" : "Nueva Temporada"}</h3>
      <form onSubmit={handleSubmit(onSubmit)} className="row g-3">
        <div className="col-md-6">
          <label>Serie:</label>
          <select {...register("idSerie", { required: true })} className="form-select">
            <option value="">Seleccione</option>
            {series.map((s) => (
              <option key={s.id} value={s.id}>
                {s.titulo}
              </option>
            ))}
          </select>
          {errors.idSerie && <span className="text-danger">Campo obligatorio</span>}
        </div>

        <div className="col-md-6">
          <label>Número de Temporada:</label>
          <input type="number" {...register("numero", { required: true, min: 1 })} className="form-control" />
          {errors.numero && <span className="text-danger">Debe ser mayor a 0</span>}
        </div>

        <div className="col-md-6">
          <label>Episodios:</label>
          <input type="number" {...register("episodios", { required: true, min: 1 })} className="form-control" />
          {errors.episodios && <span className="text-danger">Debe ser mayor a 0</span>}
        </div>

        <div className="col-md-6">
          <label>Año de estreno:</label>
          <input
            type="number"
            {...register("anioEstreno", {
              required: true,
              min: 1900,
              max: new Date().getFullYear(),
            })}
            className="form-control"
          />
          {errors.anioEstreno && (
            <span className="text-danger">
              Año entre 1900 y {new Date().getFullYear()}
            </span>
          )}
        </div>

        <div className="col-md-6">
          <label>Género:</label>
          <input {...register("genero", { required: true })} className="form-control" />
          {errors.genero && <span className="text-danger">Campo obligatorio</span>}
        </div>

        <div className="col-md-6">
          <label>Creador:</label>
          <input {...register("creador", { required: true })} className="form-control" />
          {errors.creador && <span className="text-danger">Campo obligatorio</span>}
        </div>

        {/* ⭐ Campo nuevo: puntuación IMDb */}
        <div className="col-md-6">
          <label>Puntuación IMDb (0 a 10):</label>
          <input
            type="number"
            step="0.1"
            min="0"
            max="10"
            {...register("puntuacionIMDb", { min: 0, max: 10 })}
            className="form-control"
          />
          {errors.puntuacionIMDb && (
            <span className="text-danger">Debe estar entre 0 y 10</span>
          )}
        </div>

        {/* 🔄 Campo nuevo: switch de originalidad */}
        <div className="col-md-6 d-flex align-items-center">
          <div className="form-check form-switch mt-4">
            <input
              type="checkbox"
              className="form-check-input"
              {...register("esOriginal")}
              id="switchOriginal"
            />
            <label className="form-check-label" htmlFor="switchOriginal">
              ¿Es una producción original?
            </label>
          </div>
        </div>

        <div className="col-12">
          <button type="submit" className="btn btn-success">Guardar</button>
        </div>
      </form>
    </main>
  );
};

export default FormularioTemporada;
