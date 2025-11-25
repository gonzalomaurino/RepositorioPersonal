import { DataTypes, Model } from "sequelize";
import sequelize from "../db.js";
import Serie from "./Serie.js";

class Temporada extends Model {}

Temporada.init(
  {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
      field: "id"
    },
    idSerie: {
      type: DataTypes.INTEGER,
      allowNull: false,
      field: "idSerie"
    },
    numero: {
      type: DataTypes.INTEGER,
      allowNull: false,
      field: "numero"
    },
    episodios: {
      type: DataTypes.INTEGER,
      allowNull: false,
      field: "episodios"
    },
    anioEstreno: {
      type: DataTypes.INTEGER,
      allowNull: false,
      field: "anioEstreno"
    },
    genero: {
      type: DataTypes.TEXT,
      allowNull: false,
      field: "genero"
    },
    creador: {
      type: DataTypes.TEXT,
      allowNull: false,
      field: "creador"
    },
    puntuacionIMDb: {
      type: DataTypes.REAL,
      field: "puntuacionIMDb",
      allowNull: true // puede venir nulo si no hay puntuación cargada
    },
    esOriginal: {
      type: DataTypes.BOOLEAN,
      field: "esOriginal",
      defaultValue: false
    }
  },
  {
    sequelize,
    modelName: "Temporada",
    tableName: "Temporadas",
    timestamps: false
  }
);

// Relaciones
Temporada.belongsTo(Serie, { foreignKey: "idSerie", as: "serie" });
Serie.hasMany(Temporada, { foreignKey: "idSerie", as: "temporadas" });

export default Temporada;
